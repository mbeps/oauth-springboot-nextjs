"use client"

import * as React from "react"
import { ThemeProvider as NextThemesProvider } from "next-themes"

/**
 * Theme provider wrapper component for light/dark mode support.
 * Wraps next-themes provider to enable theme switching across application.
 * Must wrap components that use useTheme hook.
 * @param props Component props
 * @param props.children Child components to wrap
 * @param props Additional next-themes provider props (attribute, defaultTheme, etc.)
 * @returns Theme provider component
 * @author Maruf Bepary
 */
export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>
}