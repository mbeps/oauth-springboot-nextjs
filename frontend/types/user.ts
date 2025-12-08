/**
 * Represents an authenticated user from OAuth provider.
 * @property {number} id - Unique identifier for the user
 * @property {string} login - User login/username from OAuth provider
 * @property {string} name - Full name of the user
 * @property {string} [email] - User email address (optional)
 * @property {string} avatarUrl - URL to user's avatar/profile picture
 */
export interface User {
  id: string;
  login: string;
  name: string;
  email?: string;
  avatarUrl?: string;
}
