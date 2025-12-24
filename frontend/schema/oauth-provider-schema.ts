import { z } from "zod";

export const OAuthProviderSchema = z.object({
  key: z.string(),
  name: z.string(),
});
