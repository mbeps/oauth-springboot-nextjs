const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Redirects user to specified OAuth provider authorization endpoint.
 * @param {string} providerKey - Provider identifier (e.g., "github", "azure")
 */
export function loginWithProvider(providerKey: string) {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/${providerKey}`;
}
