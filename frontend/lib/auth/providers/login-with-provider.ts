const getApiBaseUrl = () =>
  process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Redirects to the backend OAuth authorization endpoint.
 * @param providerKey Provider key such as `github` or `azure`.
 * @returns Nothing. Navigates the browser to the provider.
 * @author Maruf Bepary
 */
export function loginWithProvider(providerKey: string) {
  window.location.href = `${getApiBaseUrl()}/oauth2/authorization/${providerKey}`;
}
