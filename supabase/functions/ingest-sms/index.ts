import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "npm:@supabase/supabase-js@2";

type IngestRequest = {
  momo_code: string;
  sender?: string | null;
  body: string;
  received_at?: string | null; // ISO string
  sim_slot?: number | null;
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

// Telco sender allowlist (must match Android app)
const TELCO_SENDER_ALLOWLIST = [
  "MTN",
  "MTN MoMo",
  "MOMO",
  "MTNMobileMoney",
  "MTN Mobile Money",
  "100",
  "456",
  "MTN-100",
  "MTN-456",
];

// MTN MoMo body patterns (regex - must match Android app)
const MTN_MOMO_BODY_PATTERNS = [
  /.*(?:received|credit|deposit).*\d+.*(?:UGX|USD|RWF|KES|TZS).*/i,
  /.*(?:sent|paid|transfer|withdraw).*\d+.*(?:UGX|USD|RWF|KES|TZS).*/i,
  /.*(?:balance|bal).*\d+.*(?:UGX|USD|RWF|KES|TZS).*/i,
  /.*(?:payment|paid|transaction).*(?:successful|completed|confirmed).*/i,
  /.*\d+.*(?:UGX|USD|RWF|KES|TZS).*/i,
];

function isAllowedSender(sender: string | null | undefined): boolean {
  if (!sender) return false;
  const normalized = sender.trim();
  return TELCO_SENDER_ALLOWLIST.some(
    (allowed) =>
      normalized.toLowerCase() === allowed.toLowerCase() ||
      normalized.toLowerCase().includes(allowed.toLowerCase())
  );
}

function matchesMomoPattern(body: string): boolean {
  const normalized = body.trim();
  if (!normalized) return false;
  return MTN_MOMO_BODY_PATTERNS.some((pattern) => pattern.test(normalized));
}

// Rwanda (Africa/Kigali) is UTC+02:00, no DST.
function parseKigaliTimestampToISO(raw: unknown): string | null {
  if (typeof raw !== "string") return null;
  const s = raw.trim();
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

    const momoCode = (body.momo_code ?? "").trim();
    const sender = safeString(body.sender);
    const smsBody = (body.body ?? "").toString().trim();

    // Validation
    if (!momoCode) return jsonResponse(400, { error: "Missing momo_code" });
    if (!smsBody) return jsonResponse(400, { error: "Missing SMS body" });

    // Filter 1: Check sender is in allowlist
    if (!isAllowedSender(sender)) {
      // Log unknown sender for review (optional - could insert to unknown_senders table)
      return jsonResponse(200, {
        ok: true,
        skipped: true,
        reason: "sender_not_in_allowlist",
        sender: sender ?? "null",
      });
    }

    // Filter 2: Check body matches MTN MoMo patterns
    if (!matchesMomoPattern(smsBody)) {
      return jsonResponse(200, {
        ok: true,
        skipped: true,
        reason: "body_pattern_not_matched",
      });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    if (!supabaseUrl || !serviceKey) {
      return jsonResponse(500, { error: "Missing Supabase secrets" });
    }

    const supabase = createClient(supabaseUrl, serviceKey);

    // Check momo_code exists and is active
    const { data: device, error: devErr } = await supabase
      .from("devices")
      .select("momo_code, device_label, enabled")
      .eq("momo_code", momoCode)
      .single();

    if (devErr || !device) {
      return jsonResponse(401, { error: "Unknown or invalid momo_code" });
    }
    if (!device.enabled) {
      return jsonResponse(401, { error: "Device disabled" });
    }

    // Compute fingerprint for deduplication
    const receivedAt = body.received_at
      ? new Date(body.received_at).toISOString()
      : new Date().toISOString();
    const fingerprintInput = `${momoCode}|${sender ?? ""}|${receivedAt}|${smsBody}`;
    const fingerprint = await sha256Hex(fingerprintInput);

    // Check for duplicate (idempotency)
    const { data: existing } = await supabase
      .from("sms_messages")
      .select("id, parse_status")
      .eq("fingerprint", fingerprint)
      .limit(1)
      .single();

    if (existing) {
      // Duplicate detected - return OK (idempotent)
      return jsonResponse(200, {
        ok: true,
        id: existing.id,
        duplicate: true,
        parse_status: existing.parse_status,
        reason: "duplicate_fingerprint",
      });
    }

    // Insert raw SMS
    const { data: inserted, error: insErr } = await supabase
      .from("sms_messages")
      .insert({
        momo_code: momoCode,
        device_label: device.device_label ?? null,
        sim_slot: body.sim_slot ?? null,
        direction: "in",
        sender: sender,
        body: smsBody,
        received_at: receivedAt,
        fingerprint: fingerprint,
        parse_status: "pending",
      })
      .select("id")
      .single();

    if (insErr || !inserted?.id) {
      return jsonResponse(500, {
        error: "Insert failed",
        details: insErr?.message ?? "unknown",
      });
    }

    const id = inserted.id as string;

    // Parse with Gemini
    try {
      const { parsed, model_used } = await parseWithGemini({
        sender: sender,
        body: smsBody,
      });

      const ft_id = safeString((parsed as any).ft_id);
      const txn_id = safeString((parsed as any).txn_id);
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
        meta: { model_used },
      };

      await supabase.from("sms_messages").update(updatePatch).eq("id", id);

      return jsonResponse(200, { ok: true, id, parse_status: "parsed", model_used });
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      await supabase
        .from("sms_messages")
        .update({ parse_status: "failed", parse_error: msg })
        .eq("id", id);
      return jsonResponse(200, { ok: true, id, parse_status: "failed", parse_error: msg });
    }
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    return jsonResponse(500, { error: "Unhandled error", details: msg });
  }
});
