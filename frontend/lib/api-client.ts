import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

/**
 * Resolves queued requests once refresh completes.
 * Rejects the queue when refresh fails.
 * @param error Error to propagate when refresh fails.
 * @returns Nothing. Clears the local queue.
 * @author Maruf Bepary
 */
const processQueue = (error: Error | null = null): void => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });

  failedQueue = [];
};


/**
 * Axios client configured for the Spring Boot backend.
 * Carries credentials and JSON defaults for every call.
 * @author Maruf Bepary
 */
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});


/**
 * Response interceptor that retries requests after refresh.
 * Skips refresh loops for refresh and status endpoints.
 * @returns Response or rejected promise for the caller.
 * @author Maruf Bepary
 */
apiClient.interceptors.response.use(
  (response) => response,
  /**
   * Handles failed responses and retries after refresh when needed.
   * @param error Error returned by Axios.
   * @returns Response retry or rejection to propagate.
   * @author Maruf Bepary
   */
  async (error) => {
    const originalRequest = error.config;

    // Handle 401 Unauthorized - try to refresh token
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Don't attempt refresh if this IS the refresh endpoint or auth status
      const isRefreshEndpoint = originalRequest.url?.includes('/api/auth/refresh');
      const isAuthStatusEndpoint = originalRequest.url?.includes('/api/auth/status');
      
      if (isRefreshEndpoint || isAuthStatusEndpoint) {
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then(() => apiClient(originalRequest))
          .catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Try to refresh the token
        await apiClient.post('/api/auth/refresh');
        
        // Token refreshed successfully
        isRefreshing = false;
        processQueue();
        
        // Retry the original request
        return apiClient(originalRequest);
      } catch {
        // Refresh failed
        isRefreshing = false;
        processQueue(new Error('Token refresh failed'));
        
        // Only redirect if we're on a protected page
        if (typeof window !== 'undefined' && window.location.pathname !== '/') {
          console.warn('Token refresh failed - redirecting to login');
          window.location.href = '/';
        }
        
        // Return the original error so the caller can handle it
        return Promise.reject(error);
      }
    }

    // Log other errors
    if (error.response) {
      console.error('API Error:', {
        status: error.response.status,
        data: error.response.data,
        url: error.config?.url,
      });
    }

    return Promise.reject(error);
  }
);

/**
 * Request interceptor reserved for future headers.
 * @param config Outgoing request configuration.
 * @returns Request config to continue the chain.
 * @author Maruf Bepary
 */
apiClient.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error)
);
