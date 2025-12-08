import { apiClient } from '../../api-client';

/**
 * Logs in with email and password when local auth is enabled.
 * @param email Address used during signup.
 * @param password Secret set during signup.
 * @returns Promise that resolves after redirect.
 * @author Maruf Bepary
 */
export async function loginWithEmail(email: string, password: string): Promise<void> {
  await apiClient.post('/api/auth/login', { email, password });
  window.location.href = '/dashboard';
}
