import { z } from "zod";
import { AuthStatusSchema } from "@/schema/auth-status-schema";

/**
 * Result returned by `/api/auth/status`.
 * Tells the UI if a session is live and who is logged in.
 * @property authenticated Flag showing session validity.
 * @property user Optional user payload from the backend.
 * @author Maruf Bepary
 */
export type AuthStatus = z.infer<typeof AuthStatusSchema>;
