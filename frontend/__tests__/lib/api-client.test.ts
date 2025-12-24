import MockAdapter from "axios-mock-adapter";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { apiClient } from "@/lib/api-client";

describe("apiClient interceptors", () => {
  let mock: MockAdapter;
  const originalLocation = window.location;

  beforeEach(() => {
    mock = new MockAdapter(apiClient);
  });

  afterEach(() => {
    mock.restore();
    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("retries the original request after a successful refresh", async () => {
    mock.onGet("/api/protected/data").replyOnce(401);
    mock.onPost("/api/auth/refresh").replyOnce(200, {});
    mock.onGet("/api/protected/data").replyOnce(200, { message: "ok" });

    const response = await apiClient.get("/api/protected/data");

    expect(response.data).toEqual({ message: "ok" });
  });

  it("queues concurrent 401s while refreshing and resolves them after refresh", async () => {
    mock.onGet("/api/protected/data").replyOnce(401);
    mock.onGet("/api/protected/data").replyOnce(401);
    mock.onPost("/api/auth/refresh").replyOnce(200, {});
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
    mock.onPost("/api/auth/refresh").replyOnce(500);

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
        call[0] instanceof Event && call[0].type === "auth:session-expired"
    );
    expect(event).toBeDefined();

    mock.resetHandlers();
    mock.onPost("/api/auth/refresh").replyOnce(401);
    await expect(apiClient.post("/api/auth/refresh")).rejects.toBeTruthy();

    dispatchSpy.mockRestore();
  });

  it("propagates request interceptor errors", async () => {
    const handler = (
      apiClient.interceptors.request as unknown as {
        handlers: {
          fulfilled: (value: unknown) => unknown;
          rejected: (value: unknown) => Promise<unknown>;
        }[];
      }
    ).handlers[0];
    await expect(handler.rejected(new Error("request failed"))).rejects.toThrow(
      "request failed"
    );
  });

  it("handles baseURL selection from env or fallback", async () => {
    const originalEnv = process.env.NEXT_PUBLIC_API_URL;

    vi.resetModules();
    process.env.NEXT_PUBLIC_API_URL = "https://custom.example";
    const { apiClient: envClient } = await import("@/lib/api-client");
    expect(envClient.defaults.baseURL).toBe("https://custom.example");

    vi.resetModules();
    process.env.NEXT_PUBLIC_API_URL = "";
    const { apiClient: defaultClient } = await import("@/lib/api-client");
    expect(defaultClient.defaults.baseURL).toBe("http://localhost:8080");

    vi.resetModules();
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
    mock.onPost("/api/auth/refresh").replyOnce(500);

    await expect(apiClient.get("/api/protected/data")).rejects.toBeTruthy();
    expect(href).toBe("/");

    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });
});
