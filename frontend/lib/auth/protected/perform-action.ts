import { apiClient } from '../../api-client';

/**
 * Performs an authorized action on backend (e.g., create, update, delete).
 * Requires valid access token and CSRF protection.
 * @async
 * @param {string} action - Action type or name to execute on backend
 * @returns {Promise<Record<string, unknown>>} Backend response from action execution
 * @throws {AxiosError} If request fails or user is unauthorized
 */
export async function performAction(action: string): Promise<Record<string, unknown>> {
  const response = await apiClient.post<Record<string, unknown>>('/api/protected/action', { action });
  return response.data;
}
