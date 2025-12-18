import { loginWithProvider } from './login-with-provider';

/**
 * Starts the GitHub OAuth login via the backend route.
 * @returns Nothing. Navigates to the GitHub login.
 * @author Maruf Bepary
 */
export function loginWithGitHub() {
  loginWithProvider('github');
}
