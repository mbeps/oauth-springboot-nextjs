import { z } from "zod";

export const ProtectedDataSchema = z.object({
  message: z.string(),
  user: z.string(),
  data: z
    .object({
      items: z.array(z.string()).optional(),
      count: z.number().optional(),
      lastUpdated: z.number().optional(),
    })
    .optional()
    .nullable(),
});
