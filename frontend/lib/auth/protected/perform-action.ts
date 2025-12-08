import { apiClient } from '../../api-client';

/**
 * Triggers a protected backend action.
 * Expects the jwt cookie to be present.
 * @param action Action name to execute on the backend.
 * @returns Promise that resolves with backend response.
 * @author Maruf Bepary
 */
export async function performAction(action: string): Promise<Record<string, unknown>> {
  const response = await apiClient.post<Record<string, unknown>>('/api/protected/action', { action });
  return response.data;
}
