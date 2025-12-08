"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { fetchPublicData } from "@/lib/auth/public";
import { performAction } from "@/lib/auth/protected/perform-action";
import { fetchProviders } from "@/lib/auth/providers/fetch-providers";
import { loginWithProvider } from "@/lib/auth/providers/login-with-provider";
import { loginWithEmail } from "@/lib/auth/local-auth/login";
import { signupWithEmail } from "@/lib/auth/local-auth/signup";
import type { PublicData } from "@/types/public-data";
import type { OAuthProvider } from "@/types/oauth-provider";
import { useAuth } from "@/contexts/AuthContext";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { FaGithub, FaMicrosoft } from "react-icons/fa";

/**
 * Home page component - login and public information page.
 * Displays OAuth login buttons and Email/Password form if enabled.
 * Redirects authenticated users to dashboard.
 * @returns Home page with login options and public data
 * @author Maruf Bepary
 */
export default function Home() {
  const { authenticated, loading } = useAuth();
  const [publicData, setPublicData] = useState<PublicData | null>(null);
  const [providers, setProviders] = useState<OAuthProvider[]>([]);
  const [actionLoading, setActionLoading] = useState(false);

  // Local auth state
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [authLoading, setAuthLoading] = useState(false);

  const router = useRouter();

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

  const getProviderIcon = (key: string) => {
    if (key === "github") {
      return <FaGithub className="w-5 h-5 mr-2" />;
    }
    if (key === "azure") {
      return <FaMicrosoft className="w-5 h-5 mr-2" />;
    }
    return null;
  };

  const handleEmailAuth = async (e: React.FormEvent, isLogin: boolean) => {
    e.preventDefault();
    setAuthLoading(true);
    try {
      if (isLogin) {
        await loginWithEmail(email, password);
      } else {
        await signupWithEmail(email, password, name);
      }
    } catch (error) {
      toast.error(isLogin ? "Login failed" : "Signup failed", {
        description: "Please check your credentials and try again",
      });
    } finally {
      setAuthLoading(false);
    }
  };

  /**
   * Attempts to perform a protected action (requires authentication).
   * Shows success toast if successful or error toast if unauthenticated.
   * @async
   * @param action Action identifier to execute on backend
   * @author Maruf Bepary
   */
  const handleTestAction = async (action: string) => {
    setActionLoading(true);

    try {
      await performAction(action);
      toast.success(`Action '${action}' completed successfully`);
    } catch (error) {
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

  const oauthProviders = providers.filter((p) => p.key !== "local");
  const hasLocalAuth = providers.some((p) => p.key === "local");

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

        <Card>
          <CardHeader>
            <CardTitle>Welcome</CardTitle>
            <CardDescription>
              Sign in to access the protected dashboard
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            {oauthProviders.length > 0
              ? oauthProviders.map((provider) => (
                  <Button
                    key={provider.key}
                    onClick={() => loginWithProvider(provider.key)}
                    className="w-full"
                    size="lg"
                    variant="outline"
                  >
                    {getProviderIcon(provider.key)}
                    Sign in with {provider.name}
                  </Button>
                ))
              : !hasLocalAuth && (
                  <div className="text-center text-gray-500 py-4">
                    Loading login options...
                  </div>
                )}

            {hasLocalAuth && (
              <>
                {oauthProviders.length > 0 && (
                  <div className="relative">
                    <div className="absolute inset-0 flex items-center">
                      <span className="w-full border-t" />
                    </div>
                    <div className="relative flex justify-center text-xs uppercase">
                      <span className="bg-white px-2 text-gray-500">
                        Or continue with
                      </span>
                    </div>
                  </div>
                )}

                <Tabs defaultValue="login" className="w-full">
                  <TabsList className="grid w-full grid-cols-2">
                    <TabsTrigger value="login">Login</TabsTrigger>
                    <TabsTrigger value="signup">Sign Up</TabsTrigger>
                  </TabsList>

                  <TabsContent value="login">
                    <form
                      onSubmit={(e) => handleEmailAuth(e, true)}
                      className="space-y-3"
                    >
                      <div className="space-y-1">
                        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                          Email
                        </label>
                        <input
                          type="email"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          placeholder="name@example.com"
                          required
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                          Password
                        </label>
                        <input
                          type="password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          required
                        />
                      </div>
                      <Button
                        type="submit"
                        className="w-full"
                        disabled={authLoading}
                      >
                        {authLoading ? "Processing..." : "Login"}
                      </Button>
                    </form>
                  </TabsContent>

                  <TabsContent value="signup">
                    <form
                      onSubmit={(e) => handleEmailAuth(e, false)}
                      className="space-y-3"
                    >
                      <div className="space-y-1">
                        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                          Name
                        </label>
                        <input
                          type="text"
                          value={name}
                          onChange={(e) => setName(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          placeholder="John Doe"
                          required
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                          Email
                        </label>
                        <input
                          type="email"
                          value={email}
                          onChange={(e) => setEmail(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          placeholder="name@example.com"
                          required
                        />
                      </div>
                      <div className="space-y-1">
                        <label className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70">
                          Password
                        </label>
                        <input
                          type="password"
                          value={password}
                          onChange={(e) => setPassword(e.target.value)}
                          className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
                          required
                        />
                      </div>
                      <Button
                        type="submit"
                        className="w-full"
                        disabled={authLoading}
                      >
                        {authLoading ? "Processing..." : "Create Account"}
                      </Button>
                    </form>
                  </TabsContent>
                </Tabs>
              </>
            )}

            {publicData && (
              <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded">
                <p className="text-sm text-green-700">
                  ✅ Backend connection successful
                </p>
                <p className="text-xs text-green-600 mt-1">
                  Status: {publicData.status} |{" "}
                  {new Date(publicData.timestamp).toLocaleTimeString()}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Test Protected Actions (Should fail when not logged in) */}
        <Card>
          <CardHeader>
            <CardTitle>Test Protected Actions</CardTitle>
            <CardDescription>
              These buttons require authentication and will show error toasts
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              <Button
                variant="outline"
                onClick={() => handleTestAction("test_action")}
                disabled={actionLoading}
              >
                Test Action
              </Button>
              <Button
                variant="outline"
                onClick={() => handleTestAction("sample_operation")}
                disabled={actionLoading}
              >
                Sample Operation
              </Button>
            </div>
          </CardContent>
        </Card>

        <div className="text-center text-sm text-gray-500">
          <p>This is a demo application showcasing OAuth integration</p>
          <p className="mt-1">Frontend: NextJS | Backend: Spring Boot</p>
        </div>
      </div>
    </div>
  );
}
