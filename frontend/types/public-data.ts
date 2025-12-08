/**
 * Response from public data endpoint.
 * Contains non-sensitive data accessible without authentication.
 * @property {string} status - Health or status indicator (e.g., "ok", "healthy")
 * @property {string} message - Descriptive message from backend
 * @property {number} timestamp - Server timestamp when response was generated
 */
export interface PublicData {
  status: string;
  message: string;
  timestamp: number;
}
