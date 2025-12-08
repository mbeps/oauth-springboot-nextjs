import type { User } from './user';

/**
 * Response from authentication status check endpoint.
 * Indicates whether a user is currently authenticated and their details.
 * @property {boolean} authenticated - Whether the current session is authenticated
 * @property {User} [user] - User details if authenticated (optional)
 */
export interface AuthStatus {
  authenticated: boolean;
  user?: User;
}
