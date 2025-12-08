/**
 * Represents an OAuth provider configuration.
 * @property {string} key - Provider identifier (e.g., "github", "azure")
 * @property {string} name - Display name of the provider
 */
export interface OAuthProvider {
  key: string;
  name: string;
}
