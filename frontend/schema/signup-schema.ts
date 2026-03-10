/**
 * Schema for the `/api/auth/signup` request body. Extends login schema
 * with a `name` field.
 */
import { LoginSchema } from "./login-schema";
import { z } from "zod";

export const SignupSchema = LoginSchema.extend({
  name: z.string().min(1, "Name is required"),
});
