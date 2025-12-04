import { apiClient } from './api-client';

/**
 * Represents an authenticated user from OAuth provider.
 * @author Maruf Bepary
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

/**
 * Response from authentication status check endpoint.
 * Indicates whether a user is currently authenticated and their details.
 * @author Maruf Bepary
 * @property {boolean} authenticated - Whether the current session is authenticated
 * @property {User} [user] - User details if authenticated (optional)
 */
export interface AuthStatus {
  authenticated: boolean;
  user?: User;
}

/**
 * Response from protected data endpoint.
 * Contains authorization-sensitive data that requires valid access token.
 * @author Maruf Bepary
 * @property {string} message - Description or status message from backend
 * @property {string} user - Username or identifier of the authenticated user
 * @property {object} [data] - Additional protected data payload (optional)
 * @property {string[]} [data.items] - Array of data items
 * @property {number} [data.count] - Count or total number
 * @property {number} [data.lastUpdated] - Timestamp of last data update
 */
export interface ProtectedData {
  message: string;
  user: string;
  data?: {
    items?: string[];
    count?: number;
    lastUpdated?: number;
  } | null;
}

/**
 * Response from public data endpoint.
 * Contains non-sensitive data accessible without authentication.
 * @author Maruf Bepary
 * @property {string} status - Health or status indicator (e.g., "ok", "healthy")
 * @property {string} message - Descriptive message from backend
 * @property {number} timestamp - Server timestamp when response was generated
 */
export interface PublicData {
  status: string;
  message: string;
  timestamp: number;
}

/**
 * Represents an OAuth provider configuration.
 * @property {string} key - Provider identifier (e.g., "github", "azure")
 * @property {string} name - Display name of the provider
 */
export interface OAuthProvider {
  key: string;
  name: string;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Checks current authentication status with backend.
 * Returns user details if authenticated, or unverified status if check fails.
 * @author Maruf Bepary
 * @async
 * @returns {Promise<AuthStatus>} Authentication status with optional user data
 */
export async function checkAuthStatus(): Promise<AuthStatus> {
  try {
    const response = await apiClient.get<AuthStatus>('/api/auth/status');
    return response.data;
  } catch (error) {
    console.error('Auth status check failed:', error);
    return { authenticated: false };
  }
}

/**
 * Fetches sensitive data accessible only to authenticated users.
 * Requires valid access token in httpOnly cookie.
 * @author Maruf Bepary
 * @async
 * @returns {Promise<ProtectedData>} Protected data from backend
 * @throws {AxiosError} If request fails or user is unauthorized
 */
export async function fetchProtectedData(): Promise<ProtectedData> {
  const response = await apiClient.get<ProtectedData>('/api/protected/data');
  return response.data;
}

/**
 * Performs an authorized action on backend (e.g., create, update, delete).
 * Requires valid access token and CSRF protection.
 * @author Maruf Bepary
 * @async
 * @param {string} action - Action type or name to execute on backend
 * @returns {Promise<Record<string, unknown>>} Backend response from action execution
 * @throws {AxiosError} If request fails or user is unauthorized
 */
export async function performAction(action: string): Promise<Record<string, unknown>> {
  const response = await apiClient.post<Record<string, unknown>>('/api/protected/action', { action });
  return response.data;
}

/**
 * Fetches non-sensitive data accessible to all users.
 * Typically used for health checks and public information.
 * @author Maruf Bepary
 * @async
 * @returns {Promise<PublicData>} Public data from backend
 * @throws {AxiosError} If request fails
 */
export async function fetchPublicData(): Promise<PublicData> {
  const response = await apiClient.get<PublicData>('/api/public/health');
  return response.data;
}

/**
 * Redirects user to GitHub OAuth authorization endpoint.
 * Initiates login flow by directing to backend OAuth2 redirect URL.
 * @author Maruf Bepary
 * @returns {void}
 */
export function loginWithGitHub() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/github`;
}

/**
 * Redirects user to Microsoft Entra ID OAuth authorization endpoint.
 * Initiates login flow by directing to backend OAuth2 redirect URL.
 * @author Maruf Bepary
 * @returns {void}
 */
export function loginWithMicrosoft() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/azure`;
}

/**
 * Logs out current user and clears session.
 * Invalidates tokens on backend and redirects to home page.
 * Redirects to home even if logout request fails for security.
 * @author Maruf Bepary
 * @async
 * @returns {Promise<void>}
 */
export async function logout() {
  try {
    await apiClient.post('/logout');
    window.location.href = '/';
  } catch (error) {
    console.error('Logout failed:', error);
    window.location.href = '/';
  }
}

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

/**
 * Redirects user to specified OAuth provider authorization endpoint.
 * @param {string} providerKey - Provider identifier (e.g., "github", "azure")
 */
export function loginWithProvider(providerKey: string) {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/${providerKey}`;
}

/**
 * Authenticates user with email and password.
 * @param {string} email - User email
 * @param {string} password - User password
 * @async
 * @returns {Promise<void>}
 */
export async function loginWithEmail(email: string, password: string): Promise<void> {
  await apiClient.post('/api/auth/login', { email, password });
  window.location.href = '/dashboard';
}

/**
 * Registers a new user with email, password and name.
 * @param {string} email - User email
 * @param {string} password - User password
 * @param {string} name - User full name
 * @async
 * @returns {Promise<void>}
 */
export async function signupWithEmail(email: string, password: string, name: string): Promise<void> {
  await apiClient.post('/api/auth/signup', { email, password, name });
  window.location.href = '/dashboard';
}