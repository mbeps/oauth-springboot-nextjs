/**
 * Schema for OAuth provider metadata returned by
 * `/api/auth/providers`.  Each object describes a provider with a key
 * and display name.
 */
import { z } from "zod";

export const OAuthProviderSchema = z.object({
  key: z.string(),
  name: z.string(),
});
