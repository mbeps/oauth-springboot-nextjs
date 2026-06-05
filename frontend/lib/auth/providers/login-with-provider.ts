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

  if (typeof window === "undefined") {
    // This function is intended to be called from a browser action.
    // If called on the server, we don't have a reliable origin.
    return;
  }

  const frontendUrl = window.location.origin;
  window.location.href = `${authUrl}/oauth2/authorization/${providerKey}?redirect_uri=${encodeURIComponent(frontendUrl)}`;
}
