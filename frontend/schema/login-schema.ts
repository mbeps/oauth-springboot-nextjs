/**
 * Schema for the request body of the `/api/auth/login` endpoint.
 * Used to validate credentials before sending them to the auth service.
 */
import { z } from "zod";

export const LoginSchema = z.object({
  email: z.string().min(1, "Email is required").email("Invalid email address"),
  password: z.string().min(1, "Password is required"),
});
