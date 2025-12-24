import { z } from "zod";

export const PublicHealthSchema = z.object({
  status: z.string(),
  message: z.string(),
  timestamp: z.number(),
});
