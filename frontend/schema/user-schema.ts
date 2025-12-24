import { z } from "zod";

export const UserSchema = z.object({
  id: z.string(),
  login: z.string(),
  name: z.string(),
  email: z.string().optional(),
  avatarUrl: z.string().optional(),
});
