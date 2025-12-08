import { apiClient } from '../api-client';
import type { AuthStatus } from '@/types/auth-status';

/**
 * Calls `/api/auth/status` to mirror backend session state.
 * Falls back to unauthenticated when the check fails.
 * @returns Promise that resolves with auth status data.
 * @author Maruf Bepary
 */
export async function checkAuthStatus(): Promise<AuthStatus> {
  try {
    const response = await apiClient.get<AuthStatus>('/api/auth/status');
    return response.data;
  } catch (error) {
    console.error('Auth status check failed:', error);
    return { authenticated: false };
  }
}
