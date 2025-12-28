import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "npm:@supabase/supabase-js@2";

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

Deno.serve(async (req) => {
    try {
        if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
        if (req.method !== "POST") return jsonResponse(405, { error: "Method not allowed" });

        const body = await req.json().catch(() => null);
        if (!body) return jsonResponse(400, { error: "Invalid JSON body" });

        const momo_msisdn = (body.momo_msisdn ?? "").toString().trim();

        if (!momo_msisdn) {
            return jsonResponse(400, { error: "Missing momo_msisdn" });
        }

        // Normalize phone number (remove leading + or 250)
        let normalizedPhone = momo_msisdn.replace(/^\+/, "").replace(/^250/, "0");
        if (!normalizedPhone.startsWith("0")) {
            normalizedPhone = "0" + normalizedPhone;
        }

        const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
        const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";

        if (!supabaseUrl || !serviceKey) {
            return jsonResponse(500, { error: "Missing Supabase secrets" });
        }

        const supabase = createClient(supabaseUrl, serviceKey);

        // Look up device by MOMO number
        const { data: device, error: devErr } = await supabase
            .from("device_keys")
            .select("device_id, device_label, device_secret, enabled, momo_msisdn, momo_code")
            .eq("momo_msisdn", normalizedPhone)
            .single();

        if (devErr || !device) {
            // Try with original format
            const { data: device2, error: devErr2 } = await supabase
                .from("device_keys")
                .select("device_id, device_label, device_secret, enabled, momo_msisdn, momo_code")
                .eq("momo_msisdn", momo_msisdn)
                .single();

            if (devErr2 || !device2) {
                return jsonResponse(404, {
                    error: "Device not found",
                    message: "This phone number is not registered. Please contact admin."
                });
            }

            if (!device2.enabled) {
                return jsonResponse(403, { error: "Device disabled" });
            }

            return jsonResponse(200, {
                ok: true,
                device_id: device2.device_id,
                device_secret: device2.device_secret,
                device_label: device2.device_label,
                momo_msisdn: device2.momo_msisdn,
                momo_code: device2.momo_code,
                supabase_url: supabaseUrl,
            });
        }

        if (!device.enabled) {
            return jsonResponse(403, { error: "Device disabled" });
        }

        return jsonResponse(200, {
            ok: true,
            device_id: device.device_id,
            device_secret: device.device_secret,
            device_label: device.device_label,
            momo_msisdn: device.momo_msisdn,
            momo_code: device.momo_code,
            supabase_url: supabaseUrl,
        });

    } catch (e) {
        const msg = e instanceof Error ? e.message : String(e);
        return jsonResponse(500, { error: "Unhandled error", details: msg });
    }
});
