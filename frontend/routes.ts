/**
 * Public routes that stay open to all visitors.
 * @author Maruf Bepary
 */
export const publicRoutes = [
  '/',
  '/error',
];

/**
 * Routes that require a valid session cookie.
 * @author Maruf Bepary
 */
export const protectedRoutes = [
  '/dashboard',
];

/**
 * Default redirect after a successful login.
 * @author Maruf Bepary
 */
export const DEFAULT_LOGIN_REDIRECT = '/dashboard';
