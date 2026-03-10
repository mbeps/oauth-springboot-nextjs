/**
 * @fileoverview Theme provider component for light and dark mode support.
 *
 * This module provides a client-side theme provider that wraps the Next.js application
 * with the `next-themes` library, enabling dynamic theme switching and persistence.
 * It abstracts the underlying `NextThemesProvider` and integrates seamlessly with
 * the app's layout structure.
 *
 * ## Purpose
 * - Enables light/dark mode switching across the entire application
 * - Persists user theme preference to localStorage
 * - Provides the `useTheme` hook to child components
 *
 * ## Usage
 * Wrap the app layout (typically in `app/layout.tsx`) with `ThemeProvider`:
 * ```tsx
 * import { ThemeProvider } from "@/components/theme-provider"
 *
 * export default function RootLayout({ children }) {
 *   return (
 *     <html>
 *       <body>
 *         <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
 *           {children}
 *         </ThemeProvider>
 *       </body>
 *     </html>
 *   )
 * }
 * ```
 *
 * ## Integration Details
 * - Must be placed as early as possible in the component tree (typically in root layout)
 * - Requires `"use client"` directive because `next-themes` is client-side only
 * - Child components can call `useTheme()` to access theme state and switching functionality
 * - Works with Tailwind's dark mode utilities and shadcn/ui components
 */

"use client";

import * as React from "react";
import { ThemeProvider as NextThemesProvider } from "next-themes";

/**
 * Theme provider component that enables light/dark mode support across the application.
 *
 * This component wraps all child components with Next.js theme provider capabilities,
 * allowing:
 * - Theme switching between light, dark, and system preferences
 * - Automatic theme persistence to localStorage
 * - Availability of the `useTheme()` hook to descendant components
 * - Integration with Tailwind CSS dark mode and shadcn/ui components
 *
 * @component
 * @param {Object} props - Component properties
 * @param {React.ReactNode} props.children - React nodes to render inside the provider
 * @param {...Object} props - Additional configuration options from `next-themes`:
 *   - `attribute` - The HTML attribute or CSS variable to use (e.g., "class", "data-theme")
 *   - `defaultTheme` - Default theme to apply (e.g., "light", "dark", "system")
 *   - `enableSystem` - Enable system preference detection (boolean)
 *   - `disableTransitionOnChange` - Disable transitions during theme switch (boolean)
 *   - `storageKey` - localStorage key for theme persistence (string)
 *
 * @returns {React.ReactElement} A styled Next.js theme provider component
 *
 * @example
 * // Basic usage with system theme detection
 * <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
 *   <YourApp />
 * </ThemeProvider>
 *
 * @example
 * // In a child component, access theme control:
 * import { useTheme } from "next-themes"
 *
 * function MyComponent() {
 *   const { theme, setTheme } = useTheme()
 *   return (
 *     <button onClick={() => setTheme(theme === "dark" ? "light" : "dark")}>
 *       Toggle to {theme === "dark" ? "light" : "dark"} mode
 *     </button>
 *   )
 * }
 *
 * @remarks
 * - This component requires `"use client"` directive and runs only on the client
 * - Should be placed near the root of the component tree for global effect
 * - Theme changes are saved to localStorage for persistence across sessions
 * - Compatible with Tailwind's dark: prefix and shadcn/ui's theme system
 *
 * @see {@link https://github.com/pacocoursey/next-themes | next-themes documentation}
 * @author Maruf Bepary
 */
export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>;
}
