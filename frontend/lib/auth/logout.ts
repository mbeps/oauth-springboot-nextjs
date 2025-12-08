import { apiClient } from '../api-client';

/**
 * Logs out current user and clears session.
 * Invalidates tokens on backend and redirects to home page.
 * Redirects to home even if logout request fails for security.
 * @async
 * @returns {Promise<void>}
 */
export async function logout() {
  try {
    await apiClient.post('/logout');
    window.location.href = '/';
  } catch (error) {
    console.error('Logout failed:', error);
    window.location.href = '/';
  }
}
