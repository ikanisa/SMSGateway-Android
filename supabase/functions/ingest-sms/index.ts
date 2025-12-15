import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "npm:@supabase/supabase-js@2";

type IngestRequest = {
  device_id: string;
  device_secret: string;
  sender?: string | null;
  body: string;
  received_at?: string | null; // ISO string preferred
  sim_slot?: number | null;
  device_label?: string | null;
  raw?: unknown;
  meta?: unknown;
};

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers":
    "authorization, x-client-info, apikey, content-type",
};

function jsonResponse(status: number, data: unknown) {
  return new Response(JSON.stringify(data), {
    status,
    headers: { ...corsHeaders, "Content-Type": "application/json" },
  });
}

function toHex(bytes: ArrayBuffer): string {
  const u8 = new Uint8Array(bytes);
  return Array.from(u8).map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function sha256Hex(input: string): Promise<string> {
  const data = new TextEncoder().encode(input);
  const hash = await crypto.subtle.digest("SHA-256", data);
  return toHex(hash);
}

function safeNumber(v: unknown): number | null {
  if (v === null || v === undefined) return null;
  if (typeof v === "number" && Number.isFinite(v)) return v;
  if (typeof v === "string") {
    const cleaned = v.replace(/,/g, "").trim();
    const n = Number(cleaned);
    return Number.isFinite(n) ? n : null;
  }
  return null;
}

function safeString(v: unknown): string | null {
  if (v === null || v === undefined) return null;
  if (typeof v === "string") return v.trim() || null;
  if (typeof v === "number" && Number.isFinite(v)) return String(Math.trunc(v));
  return null;
}

// Rwanda (Africa/Kigali) is UTC+02:00, no DST.
// Convert "YYYY-MM-DD HH:MM:SS" (or with "T") into ISO with +02:00.
function parseKigaliTimestampToISO(raw: unknown): string | null {
  if (typeof raw !== "string") return null;
  const s = raw.trim();
  // already ISO-ish
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}/.test(s)) return s;
  const m = s.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/);
  if (!m) return null;
  const [, y, mo, d, h, mi, sec0] = m;
  const sec = sec0 ?? "00";
  return `${y}-${mo}-${d}T${h}:${mi}:${sec}+02:00`;
}

async function callGeminiOnce(args: { model: string; sender?: string | null; body: string }) {
  const apiKey = Deno.env.get("GEMINI_API_KEY") ?? "";
  if (!apiKey) throw new Error("Missing GEMINI_API_KEY secret");

  const endpoint =
    `https://generativelanguage.googleapis.com/v1beta/models/${args.model}:generateContent?key=${apiKey}`;

  const responseSchema = {
    type: "object",
    properties: {
      provider: { type: "string", description: "e.g., MTN MoMo, Airtel Money, Bank, Other" },
      txn_type: { type: "string", description: "credit/received, debit/sent, cashout, payment, fee, unknown" },
      amount: { type: "number" },
      currency: { type: "string" },
      balance: { type: "number" },

      counterparty: { type: "string" },
      counterparty_phone_suffix: { type: "string", description: "Last digits if masked e.g. 235 from (*****235)" },

      reference: { type: "string" },

      txn_id: { type: "string", description: "Transaction ID if present (TxnId/TransId/etc)" },
      ft_id: { type: "string", description: "FT Id if present (e.g. 'FT Id: 2422...')" },

      fee: { type: "number" },
      fee_currency: { type: "string" },

      transaction_time_raw: { type: "string", description: "Timestamp text from SMS if present" },

      wallet: { type: "string" },
      confidence: { type: "number" },
      notes: { type: "string" },
    },
  };

  const prompt = [
    "You are a strict SMS-to-JSON extraction engine.",
    "Extract structured transaction info from the SMS below.",
    "Rules:",
    "- Output MUST be valid JSON only (no markdown, no backticks).",
    "- Be conservative: do not invent amounts, balances, IDs, or timestamps.",
    "- If the SMS contains 'FT Id', put it in ft_id.",
    "- If the SMS contains a masked number like (*****235), extract 235 into counterparty_phone_suffix.",
    "- If the SMS contains a timestamp like '2025-11-19 23:12:44', put that exact text into transaction_time_raw.",
    "",
    `SENDER: ${args.sender ?? ""}`,
    `SMS: ${args.body}`,
  ].join("\n");

  const payload = {
    contents: [{ parts: [{ text: prompt }] }],
    generationConfig: {
      responseMimeType: "application/json",
      responseSchema,
      temperature: 0.1,
    },
  };

  const res = await fetch(endpoint, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });

  if (!res.ok) {
    const t = await res.text();
    throw new Error(`Gemini ${args.model} error ${res.status}: ${t.slice(0, 500)}`);
  }

  const out = await res.json();
  const text = out?.candidates?.[0]?.content?.parts?.[0]?.text ?? "";
  if (!text || typeof text !== "string") throw new Error(`Gemini ${args.model} returned empty output`);

  let parsed: any;
  try {
    parsed = JSON.parse(text);
  } catch {
    const cleaned = text.trim().replace(/^```json\s*/i, "").replace(/```$/i, "");
    parsed = JSON.parse(cleaned);
  }

  return { parsed: parsed as Record<string, unknown>, model_used: args.model };
}

async function parseWithGemini(args: { sender?: string | null; body: string }) {
  const primary = (Deno.env.get("GEMINI_MODEL") ?? "gemini-2.0-flash").trim();
  const fallback = (Deno.env.get("GEMINI_MODEL_FALLBACK") ?? "").trim();

  try {
    return await callGeminiOnce({ model: primary, sender: args.sender, body: args.body });
  } catch (e1) {
    if (fallback && fallback !== primary) {
      try {
        return await callGeminiOnce({ model: fallback, sender: args.sender, body: args.body });
      } catch (e2) {
        const m1 = e1 instanceof Error ? e1.message : String(e1);
        const m2 = e2 instanceof Error ? e2.message : String(e2);
        throw new Error(`Gemini failed. Primary: ${m1} | Fallback: ${m2}`);
      }
    }
    throw e1;
  }
}

Deno.serve(async (req) => {
  try {
    if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
    if (req.method !== "POST") return jsonResponse(405, { error: "Method not allowed" });

    const body = (await req.json().catch(() => null)) as IngestRequest | null;
    if (!body) return jsonResponse(400, { error: "Invalid JSON body" });

    const device_id = (body.device_id ?? "").trim();
    const device_secret = (body.device_secret ?? "").trim();
    const smsBody = (body.body ?? "").toString();

    if (!device_id || !device_secret) return jsonResponse(401, { error: "Missing device_id/device_secret" });
    if (!smsBody) return jsonResponse(400, { error: "Missing SMS body" });

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    if (!supabaseUrl || !serviceKey) return jsonResponse(500, { error: "Missing default Supabase secrets in runtime" });

    const supabase = createClient(supabaseUrl, serviceKey);

    // Device auth
    const { data: dev, error: devErr } = await supabase
      .from("device_keys")
      .select("device_id, device_label, secret_hash, enabled")
      .eq("device_id", device_id)
      .single();

    if (devErr || !dev) return jsonResponse(401, { error: "Unknown device_id" });
    if (!dev.enabled) return jsonResponse(401, { error: "Device disabled" });

    const givenHash = await sha256Hex(device_secret);
    if (givenHash !== dev.secret_hash) return jsonResponse(401, { error: "Invalid device_secret" });

    // Insert raw SMS first
    const receivedAt = body.received_at ? new Date(body.received_at).toISOString() : new Date().toISOString();

    const { data: inserted, error: insErr } = await supabase
      .from("sms_messages")
      .insert({
        device_id,
        device_label: body.device_label ?? dev.device_label ?? null,
        sim_slot: body.sim_slot ?? null,
        direction: "in",
        sender: body.sender ?? null,
        body: smsBody,
        received_at: receivedAt,
        raw: body.raw ?? null,
        meta: body.meta ?? null,
        parse_status: "pending",
      })
      .select("id")
      .single();

    if (insErr || !inserted?.id) return jsonResponse(500, { error: "Insert failed", details: insErr?.message ?? "unknown" });

    const id = inserted.id as string;

    // Parse + update same row
    try {
      const { parsed, model_used } = await parseWithGemini({ sender: body.sender ?? null, body: smsBody });

      const ft_id = safeString((parsed as any).ft_id);
      const txn_id = safeString((parsed as any).txn_id);

      // Dedup guard (avoid violating unique indexes if already present)
      if (ft_id) {
        const { data: dupe } = await supabase
          .from("sms_messages")
          .select("id")
          .eq("ft_id", ft_id)
          .neq("id", id)
          .limit(1);
        if (dupe && dupe.length > 0) {
          await supabase.from("sms_messages").update({
            parse_status: "skipped",
            parse_error: "Duplicate ft_id detected",
            meta: { ...(body.meta ?? {}), model_used, duplicate_of: dupe[0].id },
          }).eq("id", id);
          return jsonResponse(200, { ok: true, id, parse_status: "skipped", model_used, reason: "duplicate ft_id" });
        }
      }

      const transaction_time_raw = safeString((parsed as any).transaction_time_raw);
      const iso = parseKigaliTimestampToISO(transaction_time_raw);

      const updatePatch: Record<string, unknown> = {
        parse_status: "parsed",
        parse_error: null,
        parsed,

        provider: safeString((parsed as any).provider),
        txn_type: safeString((parsed as any).txn_type),

        currency: safeString((parsed as any).currency),
        amount: safeNumber((parsed as any).amount),
        balance: safeNumber((parsed as any).balance),

        counterparty: safeString((parsed as any).counterparty),
        counterparty_phone_suffix: safeString((parsed as any).counterparty_phone_suffix),

        reference: safeString((parsed as any).reference),

        txn_id,
        ft_id,

        fee: safeNumber((parsed as any).fee),
        fee_currency: safeString((parsed as any).fee_currency),

        transaction_time_raw,
        transaction_time: iso ? iso : null,

        wallet: safeString((parsed as any).wallet),

        meta: { ...(body.meta ?? {}), model_used },
      };

      await supabase.from("sms_messages").update(updatePatch).eq("id", id);

      return jsonResponse(200, { ok: true, id, parse_status: "parsed", model_used });
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      await supabase.from("sms_messages").update({ parse_status: "failed", parse_error: msg }).eq("id", id);
      return jsonResponse(200, { ok: true, id, parse_status: "failed", parse_error: msg });
    }
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    return jsonResponse(500, { error: "Unhandled error", details: msg });
  }
});
