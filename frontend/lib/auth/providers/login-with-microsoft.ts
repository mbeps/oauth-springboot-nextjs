import { loginWithProvider } from './login-with-provider';

/**
 * Starts the Microsoft Entra ID OAuth login via the backend route.
 * @returns Nothing. Navigates to the Entra login.
 * @author Maruf Bepary
 */
export function loginWithMicrosoft() {
  loginWithProvider('azure');
}
