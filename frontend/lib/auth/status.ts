import { authClient } from "../auth-client";
import type { AuthStatus } from "@/types/auth-status";

/**
 * Uses the dedicated auth client to call the auth service's
 * `/api/auth/status` endpoint (not the backend API client) and mirror
 * the current session state.  In other words, this helper talks
 * directly to the authentication service rather than the general API.
 * Falls back to unauthenticated when the check fails.
 * @returns Promise that resolves with auth status data.
 * @author Maruf Bepary
 */
export async function checkAuthStatus(): Promise<AuthStatus> {
  try {
    const response = await authClient.get<AuthStatus>("/api/auth/status");
    return response.data;
  } catch (error) {
    console.error("Auth status check failed:", error);
    return { authenticated: false };
  }
}
