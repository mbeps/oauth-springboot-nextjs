import { apiClient } from '../../api-client';
import type { OAuthProvider } from '@/types/oauth-provider';

/**
 * Fetches list of enabled OAuth providers from backend.
 * @async
 * @returns {Promise<OAuthProvider[]>} List of available providers
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
