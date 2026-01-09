import "jsr:@supabase/functions-js/edge-runtime.d.ts";
import { createClient } from "npm:@supabase/supabase-js@2";

type NotificationRequest = {
  type: string;
  recipientPhoneNumbers?: string[];
  message: string;
  parameters?: Record<string, string>;
  payerIds?: string[];
  sendToAll?: boolean;
  scheduledFor?: string;
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

// In-memory token cache (in production, consider using Redis or Supabase for token storage)
let mtnTokenCache: { token: string; expiresAt: number } | null = null;

/**
 * Send SMS notification via SMS gateway API.
 * Configure your SMS provider by setting the appropriate environment variables.
 * 
 * Supported providers:
 * - MTN: Set MTN_CONSUMER_KEY, MTN_CONSUMER_SECRET, MTN_SENDER_ADDRESS, MTN_SENDER_NAME (default)
 * - AWS_SNS: Set AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
 * - MESSAGEBIRD: Set MESSAGEBIRD_API_KEY, MESSAGEBIRD_ORIGINATOR
 * - GENERIC: Set SMS_GATEWAY_URL, SMS_GATEWAY_API_KEY, SMS_GATEWAY_METHOD (GET|POST)
 */
async function sendSmsNotification(
  phoneNumber: string,
  message: string
): Promise<{ success: boolean; error?: string }> {
  const provider = Deno.env.get("SMS_PROVIDER") || "MTN";
  
  try {
    switch (provider.toUpperCase()) {
      case "MTN":
        return await sendViaMtnSms(phoneNumber, message);
      case "AWS_SNS":
        return await sendViaAwsSns(phoneNumber, message);
      case "MESSAGEBIRD":
        return await sendViaMessageBird(phoneNumber, message);
      case "GENERIC":
        return await sendViaGenericApi(phoneNumber, message);
      default:
        console.warn(`Unknown SMS provider: ${provider}, using MTN`);
        return await sendViaMtnSms(phoneNumber, message);
    }
  } catch (error) {
    const errorMsg = error instanceof Error ? error.message : String(error);
    console.error(`SMS sending failed for ${phoneNumber}:`, errorMsg);
    return { success: false, error: errorMsg };
  }
}

/**
 * Get OAuth 2.0 access token from MTN API
 * Uses Client Credentials flow as per MTN SMS V3 API specification
 */
async function getMtnAccessToken(): Promise<string> {
  // Check if cached token is still valid (refresh 5 minutes before expiry)
  if (mtnTokenCache && mtnTokenCache.expiresAt > Date.now() + 5 * 60 * 1000) {
    return mtnTokenCache.token;
  }

  const consumerKey = Deno.env.get("MTN_CONSUMER_KEY");
  const consumerSecret = Deno.env.get("MTN_CONSUMER_SECRET");

  if (!consumerKey || !consumerSecret) {
    throw new Error("MTN credentials not configured. Set MTN_CONSUMER_KEY and MTN_CONSUMER_SECRET");
  }

  const tokenUrl = "https://api.mtn.com/v1/oauth/access_token/accesstoken?grant_type=client_credentials";

  // Base64 encode Consumer Key:Consumer Secret for Basic Auth
  const authString = `${consumerKey}:${consumerSecret}`;
  const encodedAuth = btoa(authString);

  const response = await fetch(tokenUrl, {
    method: "POST",
    headers: {
      "Authorization": `Basic ${encodedAuth}`,
      "Content-Type": "application/x-www-form-urlencoded",
    },
    body: new URLSearchParams({
      grant_type: "client_credentials",
      scope: "SEND-SMS",
    }).toString(),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`MTN OAuth error (${response.status}): ${errorText}`);
  }

  const data = await response.json();
  const accessToken = data.access_token;
  const expiresIn = data.expires_in || 3600; // Default to 1 hour if not provided

  // Cache the token with expiration time
  mtnTokenCache = {
    token: accessToken,
    expiresAt: Date.now() + (expiresIn * 1000),
  };

  console.log("MTN access token obtained and cached");
  return accessToken;
}

/**
 * Send SMS via MTN SMS V3 API
 * Documentation: https://developer.mtn.com/api-documentation
 */
async function sendViaMtnSms(
  phoneNumber: string,
  message: string
): Promise<{ success: boolean; error?: string }> {
  const senderAddress = Deno.env.get("MTN_SENDER_ADDRESS") || "MTN";
  const senderName = Deno.env.get("MTN_SENDER_NAME") || "MTN";
  const notifyUrl = Deno.env.get("MTN_NOTIFY_URL"); // Optional webhook for delivery receipts
  const callbackData = Deno.env.get("MTN_CALLBACK_DATA"); // Optional callback data

  // Get OAuth access token
  const accessToken = await getMtnAccessToken();

  // Format phone number to E.164 format (tel:+countrycode...)
  let formattedPhone = phoneNumber.trim();
  if (!formattedPhone.startsWith("tel:")) {
    formattedPhone = formattedPhone.startsWith("+") 
      ? `tel:${formattedPhone}` 
      : `tel:+${formattedPhone}`;
  }

  // Generate unique client correlator for tracking
  const clientCorrelator = `sms-${Date.now()}-${Math.random().toString(36).substring(7)}`;

  const apiUrl = "https://api.mtn.com/v3/sms/messages/sms/outbound";

  const requestBody = {
    outboundSMSMessageRequest: {
      senderAddress: senderAddress,
      receiverAddress: [formattedPhone],
      outboundSMSTextMessage: {
        message: message,
      },
      clientCorrelator: clientCorrelator,
      ...(notifyUrl && {
        receiptRequest: {
          notifyURL: notifyUrl,
          ...(callbackData && { callbackData: callbackData }),
        },
      }),
      senderName: senderName,
    },
  };

  const response = await fetch(apiUrl, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json",
      "X-Target-Environment": Deno.env.get("MTN_TARGET_ENVIRONMENT") || "sandbox", // sandbox or production
    },
    body: JSON.stringify(requestBody),
  });

  if (!response.ok) {
    let errorText: string;
    try {
      const errorData = await response.json();
      errorText = JSON.stringify(errorData);
    } catch {
      errorText = await response.text();
    }
    throw new Error(`MTN SMS API error (${response.status}): ${errorText}`);
  }

  const responseData = await response.json();
  
  // MTN API returns response with resourceReference and deliveryInfo
  const resourceReference = responseData?.outboundSMSMessageRequest?.resourceReference;
  const requestId = resourceReference?.resourceURL?.split("/").pop();
  
  console.log(`MTN SMS sent: Request ID ${requestId || "unknown"}`);
  return { success: true };
}

/**
 * Send SMS via AWS SNS
 */
async function sendViaAwsSns(
  phoneNumber: string,
  message: string
): Promise<{ success: boolean; error?: string }> {
  const accessKeyId = Deno.env.get("AWS_ACCESS_KEY_ID");
  const secretAccessKey = Deno.env.get("AWS_SECRET_ACCESS_KEY");
  const region = Deno.env.get("AWS_REGION") || "us-east-1";
  
  if (!accessKeyId || !secretAccessKey) {
    throw new Error("AWS credentials not configured. Set AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY");
  }
  
  // For AWS SNS, you need to use AWS Signature Version 4
  // This is a simplified version - in production, use AWS SDK or proper signature generation
  const endpoint = `https://sns.${region}.amazonaws.com/`;
  
  // Format phone number for AWS SNS (must be E.164)
  const to = phoneNumber.startsWith("+") ? phoneNumber : `+${phoneNumber}`;
  
  // Create SNS publish parameters
  const params = new URLSearchParams();
  params.append("Action", "Publish");
  params.append("PhoneNumber", to);
  params.append("Message", message);
  params.append("Version", "2010-03-31");
  
  // Note: In production, you should properly sign AWS requests using AWS Signature Version 4
  // For now, this is a placeholder. Consider using AWS SDK for Deno or implementing proper signing
  console.warn("AWS SNS implementation requires proper AWS Signature. Please use AWS SDK or implement signing.");
  
  // Placeholder - replace with actual AWS SNS API call with proper authentication
  // const response = await fetch(endpoint, {
  //   method: "POST",
  //   headers: {
  //     "Content-Type": "application/x-www-form-urlencoded",
  //     // Add AWS Signature headers here
  //   },
  //   body: params.toString(),
  // });
  
  throw new Error("AWS SNS implementation requires AWS SDK or proper signature generation");
}

/**
 * Send SMS via MessageBird
 */
async function sendViaMessageBird(
  phoneNumber: string,
  message: string
): Promise<{ success: boolean; error?: string }> {
  const apiKey = Deno.env.get("MESSAGEBIRD_API_KEY");
  const originator = Deno.env.get("MESSAGEBIRD_ORIGINATOR") || "SMSGateway";
  
  if (!apiKey) {
    throw new Error("MessageBird API key not configured. Set MESSAGEBIRD_API_KEY");
  }
  
  // Format phone number (MessageBird expects E.164 format)
  const to = phoneNumber.startsWith("+") ? phoneNumber : `+${phoneNumber}`;
  
  const url = "https://rest.messagebird.com/messages";
  
  const response = await fetch(url, {
    method: "POST",
    headers: {
      "Authorization": `AccessKey ${apiKey}`,
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      originator: originator,
      recipients: [to],
      body: message,
    }),
  });
  
  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(`MessageBird API error (${response.status}): ${JSON.stringify(errorData)}`);
  }
  
  const data = await response.json();
  console.log(`MessageBird SMS sent: ID ${data.id}`);
  return { success: true };
}

/**
 * Send SMS via generic HTTP API
 */
async function sendViaGenericApi(
  phoneNumber: string,
  message: string
): Promise<{ success: boolean; error?: string }> {
  const gatewayUrl = Deno.env.get("SMS_GATEWAY_URL");
  const apiKey = Deno.env.get("SMS_GATEWAY_API_KEY");
  const method = (Deno.env.get("SMS_GATEWAY_METHOD") || "POST").toUpperCase();
  
  if (!gatewayUrl) {
    throw new Error("Generic SMS gateway URL not configured. Set SMS_GATEWAY_URL");
  }
  
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  
  if (apiKey) {
    headers["Authorization"] = `Bearer ${apiKey}`;
    // Alternative header formats:
    // headers["X-API-Key"] = apiKey;
    // headers["Authorization"] = `Basic ${btoa(apiKey)}`;
  }
  
  const payload = {
    to: phoneNumber,
    message: message,
    // Add additional fields as needed by your gateway
    // from: Deno.env.get("SMS_GATEWAY_FROM"),
    // sender: Deno.env.get("SMS_GATEWAY_SENDER"),
  };
  
  const response = await fetch(gatewayUrl, {
    method: method,
    headers: headers,
    body: method === "POST" ? JSON.stringify(payload) : undefined,
  });
  
  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`SMS Gateway API error (${response.status}): ${errorText}`);
  }
  
  console.log(`Generic SMS gateway: Message sent to ${phoneNumber}`);
  return { success: true };
}

Deno.serve(async (req) => {
  try {
    if (req.method === "OPTIONS") return new Response("ok", { headers: corsHeaders });
    if (req.method !== "POST") return jsonResponse(405, { error: "Method not allowed" });

    const body = (await req.json().catch(() => null)) as NotificationRequest | null;
    if (!body) return jsonResponse(400, { error: "Invalid JSON body" });

    const { type, message, sendToAll, recipientPhoneNumbers, payerIds, parameters } = body;

    if (!type || !message) {
      return jsonResponse(400, { error: "Missing required fields: type, message" });
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL") ?? "";
    const serviceKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY") ?? "";
    if (!supabaseUrl || !serviceKey) {
      return jsonResponse(500, { error: "Missing Supabase secrets" });
    }

    const supabase = createClient(supabaseUrl, serviceKey);

    // Get list of phone numbers to notify
    let phoneNumbers: string[] = [];

    if (sendToAll) {
      // Fetch all active payers
      const { data: payers, error: payersError } = await supabase
        .from("payers")
        .select("phone_number, id, name")
        .eq("is_active", true);

      if (payersError) {
        return jsonResponse(500, { error: `Failed to fetch payers: ${payersError.message}` });
      }

      phoneNumbers = payers?.map((p) => p.phone_number).filter(Boolean) ?? [];
    } else if (payerIds && payerIds.length > 0) {
      // Fetch specific payers by ID
      const { data: payers, error: payersError } = await supabase
        .from("payers")
        .select("phone_number, id, name")
        .in("id", payerIds)
        .eq("is_active", true);

      if (payersError) {
        return jsonResponse(500, { error: `Failed to fetch payers: ${payersError.message}` });
      }

      phoneNumbers = payers?.map((p) => p.phone_number).filter(Boolean) ?? [];
    } else if (recipientPhoneNumbers && recipientPhoneNumbers.length > 0) {
      phoneNumbers = recipientPhoneNumbers;
    } else {
      return jsonResponse(400, { error: "No recipients specified. Provide sendToAll, payerIds, or recipientPhoneNumbers" });
    }

    if (phoneNumbers.length === 0) {
      return jsonResponse(200, {
        success: true,
        message: "No recipients found",
        sentCount: 0,
        failedCount: 0,
      });
    }

    // Process notifications
    const results = await Promise.allSettled(
      phoneNumbers.map((phoneNumber) => sendSmsNotification(phoneNumber, message))
    );

    let sentCount = 0;
    let failedCount = 0;
    const errors: string[] = [];

    results.forEach((result, index) => {
      if (result.status === "fulfilled" && result.value.success) {
        sentCount++;
      } else {
        failedCount++;
        const phoneNumber = phoneNumbers[index];
        const errorMsg = result.status === "rejected"
          ? result.reason?.message ?? "Unknown error"
          : result.value.error ?? "Unknown error";
        errors.push(`${phoneNumber}: ${errorMsg}`);
      }
    });

    // Log notification to database (optional)
    try {
      await supabase.from("notifications").insert({
        type,
        message,
        recipient_count: phoneNumbers.length,
        sent_count: sentCount,
        failed_count: failedCount,
        parameters: parameters ?? {},
        created_at: new Date().toISOString(),
      });
    } catch (logError) {
      console.error("Failed to log notification:", logError);
      // Don't fail the request if logging fails
    }

    return jsonResponse(200, {
      success: true,
      message: `Notifications processed: ${sentCount} sent, ${failedCount} failed`,
      sentCount,
      failedCount,
      errors: errors.length > 0 ? errors : undefined,
    });
  } catch (e) {
    const msg = e instanceof Error ? e.message : String(e);
    console.error("Error in send-notification:", msg);
    return jsonResponse(500, { error: "Unhandled error", details: msg });
  }
});
