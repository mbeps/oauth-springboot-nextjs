"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";

import { AuthPanel } from "@/components/auth/AuthPanel";
import { ProtectedActionsCard } from "@/components/ProtectedActionsCard";
import { useAuth } from "@/contexts/AuthContext";
import { fetchPublicData } from "@/lib/auth/public";
import { performAction } from "@/lib/auth/protected/perform-action";
import { fetchProviders } from "@/lib/auth/providers/fetch-providers";
import type { OAuthProvider } from "@/types/oauth-provider";
import type { PublicData } from "@/types/public-data";

/**
 * Landing page that shows login options and public status.
 * Redirects signed-in users to the dashboard.
 * @returns Home view with OAuth and local auth flows.
 * @author Maruf Bepary
 */
export default function Home() {
  const { authenticated, loading } = useAuth();
  const router = useRouter();
  const [publicData, setPublicData] = useState<PublicData | null>(null);
  const [providers, setProviders] = useState<OAuthProvider[]>([]);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    // Redirect if already authenticated
    if (!loading && authenticated) {
      router.push("/dashboard");
    }
  }, [authenticated, loading, router]);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [pubData, providerList] = await Promise.all([
          fetchPublicData(),
          fetchProviders(),
        ]);
        setPublicData(pubData);
        setProviders(providerList);
      } catch (error) {
        console.error("Failed to load data:", error);
      }
    };

    loadData();
  }, []);

  /**
   * Attempts to perform a protected action (requires authentication).
   * Shows success toast if successful or error toast if unauthenticated.
   * @param action Action identifier to execute on backend.
   * @returns Promise that resolves after the attempt.
   * @author Maruf Bepary
   */
  const handleTestAction = async (action: string) => {
    setActionLoading(true);

    try {
      await performAction(action);
      toast.success(`Action '${action}' completed successfully`);
    } catch {
      toast.error("Authentication required", {
        description: "You must be logged in to perform this action",
      });
    } finally {
      setActionLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  // Don't show login page if authenticated (will redirect)
  if (authenticated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600">Redirecting to dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="max-w-md w-full space-y-6">
        <div className="text-center">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            OAuth Demo App
          </h1>
          <p className="text-gray-600">
            NextJS + Spring Boot OAuth Integration
          </p>
        </div>

        <AuthPanel providers={providers} publicData={publicData} />
        <ProtectedActionsCard
          onAction={handleTestAction}
          loading={actionLoading}
        />
        <div className="text-center text-sm text-gray-500">
          <p>This is a demo application showcasing OAuth integration</p>
          <p className="mt-1">Frontend: NextJS | Backend: Spring Boot</p>
        </div>
      </div>
    </div>
  );
}
