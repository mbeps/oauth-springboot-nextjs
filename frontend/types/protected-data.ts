/**
 * Response from protected data endpoint.
 * Contains authorization-sensitive data that requires valid access token.
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
  data?:
    | {
        items?: string[];
        count?: number;
        lastUpdated?: number;
      }
    | null;
}
