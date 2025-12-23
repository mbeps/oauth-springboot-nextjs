"use client";

import { checkAuthStatus } from "@/lib/auth/status";
import type { User } from "@/types/user";
import { useRouter } from "next/navigation";
import {
  createContext,
  ReactNode,
  useContext,
  useEffect,
  useState,
} from "react";
import { toast } from "sonner";

/**
 * Auth context value that mirrors backend session state.
 * Keeps user info and auth flags in memory for the UI.
 * @author Maruf Bepary
 */
interface AuthContextType {
  user: User | null;
  authenticated: boolean;
  loading: boolean;
  refreshAuth: () => Promise<void>;
}

/**
 * Context that shares auth state across the app tree.
 * Used by the provider and the `useAuth` hook.
 * @author Maruf Bepary
 */
const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Wraps the app with auth state from `/api/auth/status`.
 * Checks status on mount and exposes a refresh helper.
 * @param children Nodes to render inside the provider.
 * @returns Provider element that supplies auth context.
 * @author Maruf Bepary
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  /**
   * Pulls the latest auth state from the backend.
   * Resets user data when the session is invalid.
   * @returns Promise that resolves after state sync.
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

  useEffect(() => {
    const handleSessionExpired = () => {
      setAuthenticated(false);
      setUser(null);
      toast.error("Session expired. Please log in again.");
      router.push("/");
    };

    window.addEventListener("auth:session-expired", handleSessionExpired);
    return () => {
      window.removeEventListener("auth:session-expired", handleSessionExpired);
    };
  }, [router]);

  return (
    <AuthContext.Provider value={{ user, authenticated, loading, refreshAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Custom hook to access authentication context.
 * Must be used within the AuthProvider tree.
 * @returns Auth state and helpers from the context.
 * @throws Error when used without a surrounding AuthProvider.
 * @author Maruf Bepary
 */
export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
