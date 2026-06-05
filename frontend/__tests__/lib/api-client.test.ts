import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { apiClient } from "@/lib/api-client";
import { authClient } from "@/lib/auth-client";

describe("apiClient interceptors", () => {
  let mock: MockAdapter;
  let authMock: MockAdapter;
  const originalLocation = window.location;

  beforeEach(() => {
    mock = new MockAdapter(apiClient);
    authMock = new MockAdapter(authClient);
  });

  afterEach(() => {
    mock.restore();
    authMock.restore();
    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("retries the original request after a successful refresh", async () => {
    mock.onGet("/api/protected/data").replyOnce(401);
    authMock.onPost("/api/auth/refresh").replyOnce(200, {});
    mock.onGet("/api/protected/data").replyOnce(200, { message: "ok" });

    const response = await apiClient.get("/api/protected/data");

    expect(response.data).toEqual({ message: "ok" });
  });

  it("queues concurrent 401s while refreshing and resolves them after refresh", async () => {
    mock.onGet("/api/protected/data").replyOnce(401);
    mock.onGet("/api/protected/data").replyOnce(401);
    authMock.onPost("/api/auth/refresh").replyOnce(200, {});
    mock.onGet("/api/protected/data").replyOnce(200, { call: 1 });
    mock.onGet("/api/protected/data").replyOnce(200, { call: 2 });

    const [responseA, responseB] = await Promise.all([
      apiClient.get("/api/protected/data"),
      apiClient.get("/api/protected/data"),
    ]);

    expect(responseA.data).toEqual({ call: 1 });
    expect(responseB.data).toEqual({ call: 2 });
  });

  it("dispatches session-expired event on refresh failure", async () => {
    const dispatchSpy = vi.spyOn(window, "dispatchEvent");

    let href = "/dashboard";
    const mockLocation = {
      ...originalLocation,
      get href() {
        return href;
      },
      set href(value: string) {
        href = value;
      },
      pathname: "/dashboard",
      assign: vi.fn(),
      replace: vi.fn(),
    } as Location;

    Object.defineProperty(window, "location", {
      configurable: true,
      value: mockLocation,
    });

    mock.onGet("/api/protected/data").replyOnce(401);
    mock.onGet("/api/protected/data").replyOnce(401);
    authMock.onPost("/api/auth/refresh").replyOnce(500);

    const firstRequest = apiClient.get("/api/protected/data");
    const queuedRequest = apiClient.get("/api/protected/data");

    const [firstResult, queuedResult] = await Promise.allSettled([
      firstRequest,
      queuedRequest,
    ]);

    expect(firstResult.status).toBe("rejected");
    expect(queuedResult.status).toBe("rejected");

    // Verify event was dispatched
    expect(dispatchSpy).toHaveBeenCalled();
    const event = dispatchSpy.mock.calls.find(
      (call) =>
        call[0] instanceof Event && call[0].type === "auth:session-expired",
    );
    expect(event).toBeDefined();

    authMock.resetHandlers();
    authMock.onPost("/api/auth/refresh").replyOnce(401);
    await expect(authClient.post("/api/auth/refresh")).rejects.toBeTruthy();

    dispatchSpy.mockRestore();
  });

  it("handles baseURL selection from env", async () => {
    const originalEnv = process.env.NEXT_PUBLIC_API_URL;

    vi.resetModules();
    process.env.NEXT_PUBLIC_API_URL = "https://custom.example";
    const { apiClient: envClient } = await import("@/lib/api-client");
    expect(envClient.defaults.baseURL).toBe("https://custom.example");

    vi.resetModules();
    process.env.NEXT_PUBLIC_API_URL = "";
    // Should now throw because the module-level guard checks if it's missing
    await expect(import("@/lib/api-client")).rejects.toThrow(
      "NEXT_PUBLIC_API_URL environment variable is missing",
    );

    process.env.NEXT_PUBLIC_API_URL = originalEnv;
  });

  it("rejects errors without response payloads without logging", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    const handler = (
      apiClient.interceptors.response as unknown as {
        handlers: { rejected: (value: unknown) => Promise<unknown> }[];
      }
    ).handlers[0].rejected;

    const error = { message: "network down" };
    await expect(handler(error)).rejects.toBe(error);
    expect(consoleSpy).not.toHaveBeenCalled();
    consoleSpy.mockRestore();
  });

  it("does not redirect on refresh failure when already on home", async () => {
    let href = "/";
    const mockLocation = {
      ...window.location,
      get href() {
        return href;
      },
      set href(value: string) {
        href = value;
      },
      pathname: "/",
      assign: vi.fn(),
      replace: vi.fn(),
    } as Location;

    Object.defineProperty(window, "location", {
      configurable: true,
      value: mockLocation,
    });

    mock.resetHandlers();
    mock.onGet("/api/protected/data").replyOnce(401);
    authMock.onPost("/api/auth/refresh").replyOnce(500);

    await expect(apiClient.get("/api/protected/data")).rejects.toBeTruthy();
    expect(href).toBe("/");

    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("logs API error when error.response exists but is not a 401 requiring refresh", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    mock.onGet("/api/not-found").reply(404, { message: "Not Found" });

    await expect(apiClient.get("/api/not-found")).rejects.toThrow();

    expect(consoleSpy).toHaveBeenCalledWith(
      "API Error:",
      expect.objectContaining({
        status: 404,
        data: { message: "Not Found" },
        url: "/api/not-found",
      }),
    );

    consoleSpy.mockRestore();
  });

  it("rejects and clears failedQueue when refresh fails", async () => {
    mock.onGet("/request-a").replyOnce(401);
    mock.onGet("/request-b").replyOnce(401);
    authMock.onPost("/api/auth/refresh").replyOnce(500);

    // Initial request triggers refresh
    const firstRequest = apiClient.get("/request-a");
    // Concurrent request gets queued
    const queuedRequest = apiClient.get("/request-b");

    const [resA, resB] = await Promise.allSettled([
      firstRequest,
      queuedRequest,
    ]);

    expect(resA.status).toBe("rejected");
    expect(resB.status).toBe("rejected");
    if (resB.status === "rejected") {
      expect(resB.reason.message).toBe("Token refresh failed");
    }

    // Verify queue is cleared by making another 401 request
    authMock.onPost("/api/auth/refresh").replyOnce(200);
    mock.onGet("/request-c").replyOnce(401);
    mock.onGet("/request-c").replyOnce(200, { success: true });

    const resC = await apiClient.get("/request-c");
    expect(resC.data).toEqual({ success: true });
  });
});
