import { describe, expect, it, vi, beforeEach, afterEach } from "vitest";
import { getAuthBaseUrl, authClient } from "@/lib/auth-client";

describe("auth-client", () => {
  const originalEnv = process.env;

  beforeEach(() => {
    vi.resetModules();
    process.env = { ...originalEnv };
  });

  afterEach(() => {
    process.env = originalEnv;
  });

  describe("getAuthBaseUrl", () => {
    it("returns NEXT_PUBLIC_AUTH_URL when defined", () => {
      process.env.NEXT_PUBLIC_AUTH_URL = "http://my-auth-service:8081";
      expect(getAuthBaseUrl()).toBe("http://my-auth-service:8081");
    });

    it("returns default URL when NEXT_PUBLIC_AUTH_URL is undefined", () => {
      delete process.env.NEXT_PUBLIC_AUTH_URL;
      expect(getAuthBaseUrl()).toBe("http://localhost:8081");
    });
  });

  describe("authClient", () => {
    it("is configured with the correct baseURL from environment", () => {
      // Note: authClient is a constant initialized at module load time.
      // Since it's already imported, changing process.env won't affect it here
      // unless we re-import the module, but let's just check its current state.
      const expectedUrl =
        process.env.NEXT_PUBLIC_AUTH_URL || "http://localhost:8081";
      expect(authClient.defaults.baseURL).toBe(expectedUrl);
    });

    it("has withCredentials set to true", () => {
      expect(authClient.defaults.withCredentials).toBe(true);
    });

    it("has correct default headers", () => {
      expect(authClient.defaults.headers["Content-Type"]).toBe(
        "application/json",
      );
    });
  });
});
