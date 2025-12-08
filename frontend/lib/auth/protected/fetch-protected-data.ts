import { apiClient } from '../../api-client';
import type { ProtectedData } from '@/types/protected-data';

/**
 * Fetches sensitive data accessible only to authenticated users.
 * Requires valid access token in httpOnly cookie.
 * @async
 * @returns {Promise<ProtectedData>} Protected data from backend
 * @throws {AxiosError} If request fails or user is unauthorized
 */
export async function fetchProtectedData(): Promise<ProtectedData> {
  const response = await apiClient.get<ProtectedData>('/api/protected/data');
  return response.data;
}
