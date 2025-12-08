import { apiClient } from '../../api-client';
import type { OAuthProvider } from '@/types/oauth-provider';

/**
 * Loads enabled OAuth providers from `/api/auth/providers`.
 * Used to render dynamic login buttons.
 * @returns Promise that resolves with provider list.
 * @author Maruf Bepary
 */
export async function fetchProviders(): Promise<OAuthProvider[]> {
  try {
    const response = await apiClient.get<OAuthProvider[]>('/api/auth/providers');
    return response.data;
  } catch (error) {
    console.error('Failed to fetch providers:', error);
    return [];
  }
}
