/**
 * @fileoverview Backend API client with automatic JWT refresh and request queueing.
 *
 * Provides an Axios instance (`apiClient`) preconfigured for the backend API (port 8080)
 * with built-in token refresh handling on 401 responses. This module implements a
 * sophisticated token refresh flow that queues concurrent requests during token refresh,
 * preventing the "thundering herd" problem when multiple API calls fail simultaneously.
 *
 * ## Module Purpose
 * - Wraps Axios for backend API calls (baseURL: `http://localhost:8080`, default)
 * - Automatically refreshes access tokens when 401 responses are received
 * - Queues concurrent requests that fail with 401 during an ongoing refresh
 * - Delegates token refresh to `authClient` (auth service on port 8081)
 * - Handles refresh failure by dispatching a global `auth:session-expired` event
 *
 * ## 401 Refresh Flow
 * 1. **API Request Failure**: A request to the backend receives a 401 (expired token)
 * 2. **Refresh Gate**: If a refresh is already in progress (`isRefreshing === true`),
 *    the request is queued and waits; otherwise, proceed to step 3
 * 3. **Token Refresh**: Call `authClient.post("/api/auth/refresh")` to get a new JWT
 *    - Note: This goes to the **auth service on port 8081**, not the backend
 *    - The refresh endpoint reads the `refresh_token` cookie and returns a new `jwt` cookie
 * 4. **Queue Processing**: Once the refresh succeeds, all queued requests are retried
 *    with the new token via `apiClient(originalRequest)`
 * 5. **On Failure**: If refresh fails, all queued requests are rejected and a global
 *    `auth:session-expired` event is dispatched to signal the AuthContext to clear
 *    user state and redirect to the login page
 *
 * ## Request Queueing Strategy
 * - `isRefreshing` flag tracks whether a refresh is in progress
 * - `failedQueue` stores promise resolve/reject pairs for requests that 401'd during refresh
 * - `processQueue()` drains the queue: either resolving (on success) or rejecting (on failure)
 * - On success: queued requests are retried with the new token
 * - On failure: queued requests are abandoned, session-expired event fired, user redirected
 *
 * ## When to Use This Client
 * Use `apiClient` for:
 * - Backend API calls (port 8080) — e.g., `/api/protected/data`, `/api/protected/action`
 * - Public endpoints (automatically retried on token expiry) — e.g., `/api/public/health`
 *
 * Use `authClient` (from `./auth-client`) instead for:
 * - Auth service calls (port 8081) — e.g., `/api/auth/status`, `/api/auth/login`
 * - The `authClient` has its own 401 interceptor and does not refresh (to prevent loops)
 *
 * ## Error Handling & Redirect Behavior
 * - When token refresh **succeeds**: queued requests are retried; no user disruption
 * - When token refresh **fails**:
 *   - A global `auth:session-expired` event is dispatched
 *   - The `AuthContext` listener clears the user state
 *   - User is redirected to the login page (`/`)
 *   - Original failed request is rejected to the caller
 *
 * ## Dependencies
 * - `authClient` from `./auth-client` — used for token refresh call
 * - Axios 1.12.2+ — HTTP client
 *
 * @module apiClient
 */

import axios from "axios";
import { authClient } from "./auth-client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * Flag set while a refresh request is in progress.
 * Prevents multiple simultaneous refresh calls.
 */
let isRefreshing = false;

/**
 * Queue of promise resolvers/rejectors for requests that failed with a
 * 401 while a refresh was underway.  Once the refresh completes the
 * queue is drained via `processQueue()`.
 */
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

/**
 * Drain the `failedQueue`.
 *
 * @param error  Optional error to pass to each queued reject callback.
 *               If `null` all queued requests will be resolved so they
 *               can be retried; otherwise they will all be rejected.
 */
const processQueue = (error: Error | null = null): void => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

/**
 * Axios instance configured for the backend API.  It uses a separate
 * base URL from `authClient` and includes logic to refresh access
 * tokens when a 401 response is received.
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // if we got a 401 and this request has not already been retried,
    // attempt to refresh the token via the auth service.
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // queue the current request until the ongoing refresh completes
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => apiClient(originalRequest))
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        await authClient.post("/api/auth/refresh");
        isRefreshing = false;
        processQueue();
        return apiClient(originalRequest);
      } catch {
        isRefreshing = false;
        processQueue(new Error("Token refresh failed"));

        if (typeof window !== "undefined" && window.location.pathname !== "/") {
          console.warn("Token refresh failed - redirecting to login");
          // dispatch a global event so the AuthContext can react
          window.dispatchEvent(new Event("auth:session-expired"));
        }

        return Promise.reject(error);
      }
    }

    if (error.response) {
      console.error("API Error:", {
        status: error.response.status,
        data: error.response.data,
        url: error.config?.url,
      });
    }

    return Promise.reject(error);
  },
);
