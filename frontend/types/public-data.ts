import { z } from "zod";
import { PublicHealthSchema } from "@/schema/public-health-schema";

/**
 * Payload returned by the public health endpoint.
 * Lets the landing page confirm the backend is reachable.
 * @property status Health marker reported by the API.
 * @property message Short note from the backend.
 * @property timestamp Server time when the response was created.
 * @author Maruf Bepary
 */
export type PublicData = z.infer<typeof PublicHealthSchema>;
