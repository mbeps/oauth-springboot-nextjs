import { apiClient } from '../api-client';
import type { PublicData } from '@/types/public-data';

/**
 * Fetches non-sensitive data accessible to all users.
 * Typically used for health checks and public information.
 * @async
 * @returns {Promise<PublicData>} Public data from backend
 * @throws {AxiosError} If request fails
 */
export async function fetchPublicData(): Promise<PublicData> {
  const response = await apiClient.get<PublicData>('/api/public/health');
  return response.data;
}
