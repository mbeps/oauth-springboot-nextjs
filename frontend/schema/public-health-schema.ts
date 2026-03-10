/**
 * Schema representing the public health response from
 * `/api/public/health`.  Contains status, message and timestamp.
 */
import { z } from "zod";

export const PublicHealthSchema = z.object({
  status: z.string(),
  message: z.string(),
  timestamp: z.number(),
});
