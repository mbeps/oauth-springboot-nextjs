const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Starts the GitHub OAuth login via the backend route.
 * @returns Nothing. Navigates to the GitHub login.
 * @author Maruf Bepary
 */
export function loginWithGitHub() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/github`;
}
