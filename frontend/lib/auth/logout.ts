import { apiClient } from '../api-client';

/**
 * Ends the session and routes back to the home page.
 * Redirects even when logout fails so stale tokens do not linger.
 * @returns Promise that resolves after navigation.
 * @author Maruf Bepary
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
