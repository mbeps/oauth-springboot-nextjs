import { z } from "zod";
import { UserSchema } from "@/schema/user-schema";

/**
 * User profile returned after OAuth or local login.
 * Drives avatar, display name, and contact details on the UI.
 * @property id Identifier from the provider or local store.
 * @property login Username shown on dashboards and calls.
 * @property name Full name used for friendly greetings.
 * @property email Optional email from the provider.
 * @property avatarUrl Optional avatar rendered on the dashboard.
 * @author Maruf Bepary
 */
export type User = z.infer<typeof UserSchema>;
