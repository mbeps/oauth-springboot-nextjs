import { apiClient } from '../../api-client';

/**
 * Authenticates user with email and password.
 * @param {string} email - User email
 * @param {string} password - User password
 * @async
 * @returns {Promise<void>}
 */
export async function loginWithEmail(email: string, password: string): Promise<void> {
  await apiClient.post('/api/auth/login', { email, password });
  window.location.href = '/dashboard';
}
