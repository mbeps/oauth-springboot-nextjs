import { apiClient } from '../../api-client';

/**
 * Registers a new user with email, password and name.
 * @param {string} email - User email
 * @param {string} password - User password
 * @param {string} name - User full name
 * @async
 * @returns {Promise<void>}
 */
export async function signupWithEmail(email: string, password: string, name: string): Promise<void> {
  await apiClient.post('/api/auth/signup', { email, password, name });
  window.location.href = '/dashboard';
}
