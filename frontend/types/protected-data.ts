import { z } from "zod";
import { ProtectedDataSchema } from "@/schema/protected-data-schema";

/**
 * Payload returned when calling the protected API.
 * Only available when the jwt cookie is valid.
 * @property message Status message for the secured request.
 * @property user Authenticated username echoed by the backend.
 * @property data Optional payload with demo content from the server.
 * @property data.items Optional list of protected items.
 * @property data.count Optional count for the payload.
 * @property data.lastUpdated Optional timestamp of the demo data.
 * @author Maruf Bepary
 */
export type ProtectedData = z.infer<typeof ProtectedDataSchema>;
