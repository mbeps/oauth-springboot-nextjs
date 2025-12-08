import { apiClient } from '../api-client';
import type { AuthStatus } from '@/types/auth-status';

/**
 * Checks current authentication status with backend.
 * Returns user details if authenticated, or unverified status if check fails.
 * @async
 * @returns {Promise<AuthStatus>} Authentication status with optional user data
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
