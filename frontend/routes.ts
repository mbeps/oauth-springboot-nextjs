/**
 * Public routes that don't require authentication
 */
export const publicRoutes = [
  '/',
  '/error',
];

/**
 * Routes that require authentication
 */
export const protectedRoutes = [
  '/dashboard',
];

/**
 * Default redirect after successful login
 */
export const DEFAULT_LOGIN_REDIRECT = '/dashboard';