/**
 * Schema for the authentication status response (`/api/auth/status`).
 * Indicates whether there is an active session and optionally the
 * user object.
 */
import { z } from "zod";
import { UserSchema } from "./user-schema";

export const AuthStatusSchema = z.object({
  authenticated: z.boolean(),
  user: UserSchema.optional(),
});
