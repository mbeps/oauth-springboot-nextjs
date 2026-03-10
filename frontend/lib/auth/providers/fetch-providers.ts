import { authClient } from "../../auth-client";
import type { OAuthProvider } from "@/types/oauth-provider";

/**
 * Loads enabled OAuth providers from the auth service's
 * `/api/auth/providers` endpoint via the `authClient`.  This is why it
 * is separate from the API client; the providers list is owned by the
 * authentication service.
 * Used to render dynamic login buttons.
 * @returns Promise that resolves with provider list.
 * @author Maruf Bepary
 */
export async function fetchProviders(): Promise<OAuthProvider[]> {
  try {
    const response = await authClient.get<OAuthProvider[]>(
      "/api/auth/providers",
    );
    return response.data;
  } catch (error) {
    console.error("Failed to fetch providers:", error);
    return [];
  }
}
