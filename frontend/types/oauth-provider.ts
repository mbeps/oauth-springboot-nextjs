/**
 * Metadata for an OAuth provider exposed by `/api/auth/providers`.
 * Powers the dynamic login buttons on the landing page.
 * @property key Identifier such as `github` or `azure`.
 * @property name Label shown on the provider button.
 * @author Maruf Bepary
 */
export interface OAuthProvider {
  key: string;
  name: string;
}
