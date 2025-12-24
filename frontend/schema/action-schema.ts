import { z } from "zod";

export const ActionSchema = z.object({
  action: z
    .string()
    .min(1, "Action cannot be blank")
    .max(50, "Action must be less than 50 characters"),
});
