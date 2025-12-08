"use client"

import * as React from "react"
import { ThemeProvider as NextThemesProvider } from "next-themes"

/**
 * Wraps the app with next-themes for light and dark support.
 * Required for components that call `useTheme`.
 * @param children Nodes to render inside the provider.
 * @param props Additional theme configuration.
 * @returns Theme provider element.
 * @author Maruf Bepary
 */
export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>
}
