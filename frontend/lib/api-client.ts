import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

/**
 * Processes the queue of failed requests during token refresh.
 * Resolves or rejects queued promises based on refresh outcome.
 * @author Maruf Bepary
 * @param Error if refresh failed, null if successful.
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
 * Axios HTTP client for communicating with the Spring Boot backend.
 * Handles authentication, token refresh, and error management.
 * Used for all API requests from the frontend.
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
 * Response interceptor for global error handling and token refresh.
 * Refreshes tokens on 401 errors and retries requests as needed.
 * Only attempts refresh if a refresh token cookie exists.
 * @param response Axios response object.
 * @param error Axios error object.
 * @returns Response or rejected promise.
 * @author Maruf Reformátory
 */
apiClient.interceptors.response.use(
  (response) => response,
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
      } catch (refreshError) {
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

// Request interceptor (if needed for future enhancements)
apiClient.interceptors.request.use(
  (config) => {
    // Could add tokens or other headers here in the future
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
