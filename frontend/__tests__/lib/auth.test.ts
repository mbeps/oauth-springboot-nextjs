import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

const getMock = vi.fn();
const postMock = vi.fn();

vi.mock('@/lib/api-client', () => ({
  apiClient: {
    get: (...args: unknown[]) => getMock(...args),
    post: (...args: unknown[]) => postMock(...args),
  },
}));

import { fetchPublicData } from '@/lib/auth/public';
import { checkAuthStatus } from '@/lib/auth/status';
import { fetchProviders } from '@/lib/auth/providers/fetch-providers';
import { loginWithProvider } from '@/lib/auth/providers/login-with-provider';
import { loginWithGitHub } from '@/lib/auth/providers/login-with-github';
import { loginWithMicrosoft } from '@/lib/auth/providers/login-with-microsoft';
import { loginWithEmail } from '@/lib/auth/local-auth/login';
import { signupWithEmail } from '@/lib/auth/local-auth/signup';
import { fetchProtectedData } from '@/lib/auth/protected/fetch-protected-data';
import { performAction } from '@/lib/auth/protected/perform-action';
import { logout } from '@/lib/auth/logout';

const setMockLocation = (initialPath = '/dashboard') => {
  const originalLocation = window.location;
  let href = initialPath;

  const mockLocation = {
    ...originalLocation,
    get href() {
      return href;
    },
    set href(value: string) {
      href = value;
    },
    pathname: initialPath,
    assign: vi.fn(),
    replace: vi.fn(),
  } as Location;

  Object.defineProperty(window, 'location', {
    configurable: true,
    value: mockLocation,
  });

  return {
    getHref: () => href,
    restore: () =>
      Object.defineProperty(window, 'location', {
        configurable: true,
        value: originalLocation,
      }),
  };
};

describe('auth helpers', () => {
  const originalEnv = process.env.NEXT_PUBLIC_API_URL;

  beforeEach(() => {
    getMock.mockReset();
    postMock.mockReset();
    process.env.NEXT_PUBLIC_API_URL = 'http://localhost:8080';
  });

  afterEach(() => {
    process.env.NEXT_PUBLIC_API_URL = originalEnv;
  });

  it('fetches public data', async () => {
    const payload = { status: 'ok', timestamp: new Date().toISOString() };
    getMock.mockResolvedValueOnce({ data: payload });

    const result = await fetchPublicData();

    expect(getMock).toHaveBeenCalledWith('/api/public/health');
    expect(result).toEqual(payload);
  });

  it('fetches protected data and performs actions', async () => {
    const protectedPayload = { message: 'secret', data: { items: ['a'] } };
    const actionPayload = { success: true };
    getMock.mockResolvedValueOnce({ data: protectedPayload });
    postMock.mockResolvedValueOnce({ data: actionPayload });

    const data = await fetchProtectedData();
    const actionResult = await performAction('test_action');

    expect(getMock).toHaveBeenCalledWith('/api/protected/data');
    expect(postMock).toHaveBeenCalledWith('/api/protected/action', {
      action: 'test_action',
    });
    expect(data).toEqual(protectedPayload);
    expect(actionResult).toEqual(actionPayload);
  });

  it('handles provider discovery success and failure', async () => {
    const providers = [
      { key: 'github', name: 'GitHub' },
      { key: 'azure', name: 'Microsoft' },
    ];
    getMock.mockResolvedValueOnce({ data: providers });
    const successResult = await fetchProviders();
    expect(getMock).toHaveBeenCalledWith('/api/auth/providers');
    expect(successResult).toEqual(providers);

    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    getMock.mockRejectedValueOnce(new Error('offline'));
    const failureResult = await fetchProviders();
    expect(failureResult).toEqual([]);
    consoleSpy.mockRestore();
  });

  it('checks auth status and falls back when API fails', async () => {
    const authPayload = { authenticated: true, user: { login: 'octocat' } };
    getMock.mockResolvedValueOnce({ data: authPayload });
    const status = await checkAuthStatus();
    expect(status).toEqual(authPayload);
    expect(getMock).toHaveBeenCalledWith('/api/auth/status');

    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    getMock.mockRejectedValueOnce(new Error('fail'));
    const fallbackStatus = await checkAuthStatus();
    expect(fallbackStatus).toEqual({ authenticated: false });
    consoleSpy.mockRestore();
  });

  it('logs out and still redirects when logout fails', async () => {
    const location = setMockLocation();
    postMock.mockResolvedValueOnce({});
    await logout();
    expect(postMock).toHaveBeenCalledWith('/logout');
    expect(location.getHref()).toBe('/');
    location.restore();

    const errorLocation = setMockLocation('/account');
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    postMock.mockRejectedValueOnce(new Error('network'));
    await logout();
    expect(errorLocation.getHref()).toBe('/');
    consoleSpy.mockRestore();
    errorLocation.restore();
  });

  it('supports local login and signup and redirects to dashboard', async () => {
    const location = setMockLocation();
    postMock.mockResolvedValueOnce({});
    await loginWithEmail('user@example.com', 'secret');
    expect(postMock).toHaveBeenCalledWith('/api/auth/login', {
      email: 'user@example.com',
      password: 'secret',
    });
    expect(location.getHref()).toBe('/dashboard');
    location.restore();

    const signupLocation = setMockLocation();
    postMock.mockResolvedValueOnce({});
    await signupWithEmail('new@example.com', 'secret', 'Test User');
    expect(postMock).toHaveBeenCalledWith('/api/auth/signup', {
      email: 'new@example.com',
      password: 'secret',
      name: 'Test User',
    });
    expect(signupLocation.getHref()).toBe('/dashboard');
    signupLocation.restore();
  });

  it('builds provider login URLs from configuration', () => {
    const location = setMockLocation();
    process.env.NEXT_PUBLIC_API_URL = 'https://api.example.com';
    loginWithProvider('custom');
    expect(location.getHref()).toBe(
      'https://api.example.com/oauth2/authorization/custom',
    );
    location.restore();
  });

  it('falls back to localhost base URL when config is missing', () => {
    const location = setMockLocation();
    process.env.NEXT_PUBLIC_API_URL = '';
    loginWithProvider('custom');
    expect(location.getHref()).toBe(
      'http://localhost:8080/oauth2/authorization/custom',
    );
    location.restore();
  });

  it('exposes convenience provider logins', () => {
    const location = setMockLocation('/home');
    process.env.NEXT_PUBLIC_API_URL = 'https://auth.example.com';

    loginWithGitHub();
    expect(location.getHref()).toBe(
      'https://auth.example.com/oauth2/authorization/github',
    );

    loginWithMicrosoft();
    expect(location.getHref()).toBe(
      'https://auth.example.com/oauth2/authorization/azure',
    );

    location.restore();
  });
});
