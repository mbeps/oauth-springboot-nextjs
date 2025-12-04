import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

// Routes that don't require authentication
const publicRoutes = ['/', '/error'];

// Routes that require authentication
const protectedRoutes = ['/dashboard'];

/**
 * Next.js middleware for server-side route protection.
 * Validates JWT cookie presence for protected routes.
 * Redirects unauthenticated users to home page when accessing protected areas.
 * Backend validates token integrity and expiry.
 * @param request  Incoming HTTP request
 * @returns Response to continue or redirect request
 * @author Maruf Bepary
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Check if route is public
  const isPublicRoute = publicRoutes.includes(pathname);

  // Check if route is protected
  const isProtectedRoute = protectedRoutes.some(route => pathname.startsWith(route));

  // Public routes are always accessible
  if (isPublicRoute) {
    return NextResponse.next();
  }

  // For protected routes, check if JWT cookie exists
  if (isProtectedRoute) {
    const jwtCookie = request.cookies.get('jwt');

    // If no JWT cookie, redirect to home
    if (!jwtCookie) {
      const url = request.nextUrl.clone();
      url.pathname = '/';
      return NextResponse.redirect(url);
    }

    // JWT exists - let the request through
    // Backend will validate the token properly
    return NextResponse.next();
  }

  // Allow request to continue for all other routes
  return NextResponse.next();
}

// Configure which routes to run middleware on
export const config = {
  matcher: [
    // Skip Next.js internals and static files
    '/((?!_next|[^?]*\\.(?:html?|css|js(?!on)|jpe?g|webp|png|gif|svg|ttf|woff2?|ico|csv|docx?|xlsx?|zip|webmanifest)).*)',
    // Always run for API routes
    '/(api|trpc)(.*)',
  ],
};