import { getAuthBaseUrl } from "@/lib/auth-client";

/**
 * Initiate an OAuth2 login flow by redirecting the browser.
 *
 * The frontend constructs the URL of the auth service using
 * `getAuthBaseUrl()`.  The current origin is encoded and sent as the
 * `redirect_uri` query parameter so that the auth service knows where to
 * send the user after successful authentication.
 *
 * @param providerKey  key of the provider (e.g. "github" or "azure")
 */
export function loginWithProvider(providerKey: string) {
  const authUrl = getAuthBaseUrl();
  const frontendUrl =
    typeof window !== "undefined"
      ? window.location.origin
      : "http://localhost:3000";
  window.location.href = `${authUrl}/oauth2/authorization/${providerKey}?redirect_uri=${encodeURIComponent(frontendUrl)}`;
}
