import { apiClient } from '../api-client';
import type { PublicData } from '@/types/public-data';

/**
 * Gets public health data from the backend.
 * Used by the landing page to confirm connectivity.
 * @returns Promise that resolves with public payload.
 * @author Maruf Bepary
 */
export async function fetchPublicData(): Promise<PublicData> {
  const response = await apiClient.get<PublicData>('/api/public/health');
  return response.data;
}
