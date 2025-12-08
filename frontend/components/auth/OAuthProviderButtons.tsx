"use client";

import { FaGithub, FaMicrosoft } from "react-icons/fa";

import { Button } from "@/components/ui/button";
import { loginWithProvider } from "@/lib/auth/providers/login-with-provider";
import type { OAuthProvider } from "@/types/oauth-provider";

type OAuthProviderButtonsProps = {
  providers: OAuthProvider[];
  hasLocalAuth: boolean;
};

/**
 * Picks an icon for a provider key.
 * @param key Provider identifier.
 * @returns Matching icon element or null.
 * @author Maruf Bepary
 */
const getProviderIcon = (key: string) => {
  if (key === "github") {
    return <FaGithub className="w-5 h-5 mr-2" />;
  }
  if (key === "azure") {
    return <FaMicrosoft className="w-5 h-5 mr-2" />;
  }
  return null;
};

/**
 * Renders OAuth provider sign-in buttons.
 * @param providers Providers excluding local.
 * @param hasLocalAuth True when local auth is enabled.
 * @returns Provider buttons or loading state.
 * @author Maruf Bepary
 */
export function OAuthProviderButtons({
  providers,
  hasLocalAuth,
}: OAuthProviderButtonsProps) {
  if (providers.length === 0 && !hasLocalAuth) {
    return (
      <div className="text-center text-gray-500 py-4">
        Loading login options...
      </div>
    );
  }

  return (
    <>
      {providers.map((provider) => (
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
      ))}
    </>
  );
}
