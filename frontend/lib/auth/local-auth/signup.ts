import { apiClient } from '../../api-client';

/**
 * Creates a local account with email, password, and name.
 * @param email Address to register.
 * @param password Secret to secure the account.
 * @param name Display name shown in the UI.
 * @returns Promise that resolves after redirect.
 * @author Maruf Bepary
 */
export async function signupWithEmail(email: string, password: string, name: string): Promise<void> {
  await apiClient.post('/api/auth/signup', { email, password, name });
  window.location.href = '/dashboard';
}
