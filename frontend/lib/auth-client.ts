/**
 * Axios instance targeting the authentication service.
 *
 * This module exposes two helpers:
 *   - `authClient` is used for any call that belongs to the auth service
 *     (login, signup, refresh, status, providers, logout). It has its
 *     own base URL separate from the API backend and is controlled by
 *     the `NEXT_PUBLIC_AUTH_URL` environment variable (default
 *     http://localhost:8081).
 *   - `getAuthBaseUrl()` returns the same base URL and is used when
 *     constructing OAuth2 redirect URLs; it also falls back to the
 *     default when the env var is undefined.
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
 * `NEXT_PUBLIC_AUTH_URL` with a fallback of `http://localhost:8081`.
 */
export function getAuthBaseUrl(): string {
  return process.env.NEXT_PUBLIC_AUTH_URL ?? "http://localhost:8081";
}

const AUTH_BASE_URL =
  process.env.NEXT_PUBLIC_AUTH_URL || "http://localhost:8081";

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
