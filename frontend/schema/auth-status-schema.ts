import { z } from "zod";
import { UserSchema } from "./user-schema";

export const AuthStatusSchema = z.object({
  authenticated: z.boolean(),
  user: UserSchema.optional(),
});
