import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

/**
 * Merges conditional Tailwind class names without conflicts.
 * @param inputs Class values to merge into a single string.
 * @returns Combined class name string.
 * @author Maruf Bepary
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}
