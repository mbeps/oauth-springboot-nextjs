const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

/**
 * Starts the Microsoft Entra ID OAuth login via the backend route.
 * @returns Nothing. Navigates to the Entra login.
 * @author Maruf Bepary
 */
export function loginWithMicrosoft() {
  window.location.href = `${API_BASE_URL}/oauth2/authorization/azure`;
}
