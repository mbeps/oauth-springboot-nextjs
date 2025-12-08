const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Redirects user to GitHub OAuth authorization endpoint.
 * Initiates login flow by directing to backend OAuth2 redirect URL.
 * @returns {void}
 */
export function loginWithGitHub() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/github`;
}
