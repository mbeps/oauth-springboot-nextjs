import { render, renderHook, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("@/lib/auth/status", () => ({
  checkAuthStatus: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  useRouter: vi.fn(() => ({
    push: vi.fn(),
  })),
}));

vi.mock("sonner", () => ({
  toast: {
    error: vi.fn(),
    success: vi.fn(),
  },
}));

import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import { checkAuthStatus } from "@/lib/auth/status";

const mockStatus = vi.mocked(checkAuthStatus);

describe("AuthContext", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("bootstraps auth state and supports manual refresh", async () => {
    mockStatus.mockResolvedValueOnce({
      authenticated: true,
      user: { login: "octocat", name: "Octocat" },
    });
    mockStatus.mockResolvedValueOnce({ authenticated: false });

    const Consumer = () => {
      const { user, authenticated, loading, refreshAuth } = useAuth();
      return (
        <div>
          <span data-testid="user">{user?.login ?? "none"}</span>
          <span data-testid="authenticated">
            {authenticated ? "yes" : "no"}
          </span>
          <span data-testid="loading">{loading ? "yes" : "no"}</span>
          <button onClick={refreshAuth}>refresh</button>
        </div>
      );
    };

    render(
      <AuthProvider>
        <Consumer />
      </AuthProvider>
    );

    expect(screen.getByTestId("loading")).toHaveTextContent("yes");
    await waitFor(() =>
      expect(screen.getByTestId("authenticated")).toHaveTextContent("yes")
    );
    expect(screen.getByTestId("user")).toHaveTextContent("octocat");
    expect(screen.getByTestId("loading")).toHaveTextContent("no");
    expect(mockStatus).toHaveBeenCalledTimes(1);

    await userEvent.click(screen.getByRole("button", { name: /refresh/i }));
    await waitFor(() =>
      expect(screen.getByTestId("authenticated")).toHaveTextContent("no")
    );
    expect(screen.getByTestId("user")).toHaveTextContent("none");
    expect(mockStatus).toHaveBeenCalledTimes(2);
  });

  it("falls back to unauthenticated state on failure", async () => {
    mockStatus.mockRejectedValueOnce(new Error("network"));

    const InlineConsumer = () => {
      const { authenticated } = useAuth();
      return <span data-testid="status">{authenticated ? "yes" : "no"}</span>;
    };

    render(
      <AuthProvider>
        <InlineConsumer />
      </AuthProvider>
    );

    await waitFor(() =>
      expect(screen.getByTestId("status")).toHaveTextContent("no")
    );
  });

  it("throws when used outside provider", () => {
    expect(() => renderHook(() => useAuth())).toThrowError(
      "useAuth must be used within an AuthProvider"
    );
  });
});
