import { apiClient } from '../../api-client';
import type { ProtectedData } from '@/types/protected-data';

/**
 * Pulls protected demo data for authenticated users.
 * Relies on the jwt cookie set by the backend.
 * @returns Promise that resolves with protected payload.
 * @author Maruf Bepary
 */
export async function fetchProtectedData(): Promise<ProtectedData> {
  const response = await apiClient.get<ProtectedData>('/api/protected/data');
  return response.data;
}
