/**
 * Axios instance targeting the authentication endpoints.
 *
 * This module exposes two helpers:
 *   - `authClient` is used for any call that belongs to authentication
 *     (login, signup, refresh, status, providers, logout). In the unified
 *     architecture, it points to the same backend 8080 service but
 *     uses `NEXT_PUBLIC_AUTH_URL` for configuration.
 *   - `getAuthBaseUrl()` returns the same base URL and is used when
 *     constructing OAuth2 redirect URLs.
 *
 * Keeping authentication traffic on a dedicated client avoids circular
 * dependencies and makes it clear which calls require cookies issued
 * by the auth service.
 */
import axios from "axios";

/**
 * Return the base URL of the auth service.
 *
 * Used by client code that must build an OAuth2 authorization URL
 * (see `login-with-provider.ts`).  The value is taken from
 * `NEXT_PUBLIC_AUTH_URL`.
 */
export function getAuthBaseUrl(): string {
  const url = process.env.NEXT_PUBLIC_AUTH_URL;
  if (!url) {
    throw new Error("NEXT_PUBLIC_AUTH_URL environment variable is missing");
  }
  return url;
}

const AUTH_BASE_URL = getAuthBaseUrl();

/**
 * Axios instance preconfigured for communicating with the auth service.
 * The `withCredentials` flag ensures HTTP-only cookies are sent along.
 */
export const authClient = axios.create({
  baseURL: AUTH_BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});
