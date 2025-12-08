"use client";

import { useState } from "react";
import { toast } from "sonner";

import { BackendStatusBanner } from "@/components/auth/BackendStatusBanner";
import { LocalAuthTabs, type EmailAuthPayload } from "@/components/auth/LocalAuthTabs";
import { OAuthProviderButtons } from "@/components/auth/OAuthProviderButtons";
import { ProvidersDivider } from "@/components/auth/ProvidersDivider";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { loginWithEmail } from "@/lib/auth/local-auth/login";
import { signupWithEmail } from "@/lib/auth/local-auth/signup";
import type { OAuthProvider } from "@/types/oauth-provider";
import type { PublicData } from "@/types/public-data";

type AuthPanelProps = {
  providers: OAuthProvider[];
  publicData: PublicData | null;
};

/**
 * Renders the main auth panel combining OAuth, local auth, and backend status.
 * @param providers List of available auth providers.
 * @param publicData Public backend status payload.
 * @returns Auth card UI.
 * @author Maruf Bepary
 */
export function AuthPanel({ providers, publicData }: AuthPanelProps) {
  const oauthProviders = providers.filter((provider) => provider.key !== "local");
  const hasLocalAuth = providers.some((provider) => provider.key === "local");
  const [authLoading, setAuthLoading] = useState(false);

  const handleEmailAuth = async (payload: EmailAuthPayload) => {
    setAuthLoading(true);
    try {
      if (payload.mode === "login") {
        await loginWithEmail(payload.email, payload.password);
      } else {
        await signupWithEmail(payload.email, payload.password, payload.name ?? "");
      }
    } catch {
      toast.error(
        payload.mode === "login" ? "Login failed" : "Signup failed",
        {
          description: "Please check your credentials and try again",
        },
      );
    } finally {
      setAuthLoading(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Welcome</CardTitle>
        <CardDescription>
          Sign in to access the protected dashboard
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <OAuthProviderButtons
          providers={oauthProviders}
          hasLocalAuth={hasLocalAuth}
        />

        {hasLocalAuth && (
          <>
            {oauthProviders.length > 0 && <ProvidersDivider />}
            <LocalAuthTabs onAuth={handleEmailAuth} loading={authLoading} />
          </>
        )}

        {publicData && <BackendStatusBanner publicData={publicData} />}
      </CardContent>
    </Card>
  );
}
