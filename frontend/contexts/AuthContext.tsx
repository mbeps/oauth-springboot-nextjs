"use client";

import { checkAuthStatus } from "@/lib/auth/status";
import type { User } from "@/types/user";
import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";

/**
 * Type definition for authentication context value.
 * Provides authentication state and methods to child components.
 * @author Maruf Bepary
 */
interface AuthContextType {
  user: User | null;
  authenticated: boolean;
  loading: boolean;
  refreshAuth: () => Promise<void>;
}

/**
 * React Context for managing authentication state across application.
 * Stores user data, authentication status, and loading state.
 * @author Maruf Bepary
 * @type {React.Context<AuthContextType|undefined>}
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Provider component that wraps application with authentication state.
 * Automatically checks auth status on mount and provides it to children.
 * @param props Component props
 * @param props.children Child components to wrap
 * @returns Provider component with context
 * @author Maruf Bepary
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  /**
   * Fetches current auth status and updates context state.
   * Used for initial check and manual auth state refresh.
   * @async
   * @author Maruf Bepary
   */
  const refreshAuth = async () => {
    try {
      const status = await checkAuthStatus();
      setAuthenticated(status.authenticated);
      setUser(status.user || null);
    } catch (error) {
      console.error("Auth refresh failed:", error);
      setAuthenticated(false);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refreshAuth();
  }, []);

  return (
    <AuthContext.Provider value={{ user, authenticated, loading, refreshAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to access authentication context.
 * Must be used within AuthProvider component tree.
 * @returns Authentication state and methods
 * @throws {Error} If used outside AuthProvider
 * @author Maruf Bepary
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
