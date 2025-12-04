import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

/**
 * Combines and merges Tailwind CSS class names intelligently.
 * Resolves class conflicts by giving priority to later values.
 * Useful for conditionally applying Tailwind classes and component variants.
 * @param inputs Class names to merge (strings, objects, arrays)
 * @returns Merged and conflict-resolved class string
 * @author Maruf Bepary
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
