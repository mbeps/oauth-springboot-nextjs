import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

const loginWithEmailMock = vi.fn();
const signupWithEmailMock = vi.fn();
const toastErrorMock = vi.fn();
const oauthPropsSpy = vi.fn();
let mockLoginPayload = {
  email: 'login@example.com',
  password: 'pw',
  name: 'Login User',
  mode: 'login',
};
let mockSignupPayload = {
  email: 'signup@example.com',
  password: 'pw',
  name: 'Signup User',
  mode: 'signup' as const,
};

vi.mock('@/lib/auth/local-auth/login', () => ({
  loginWithEmail: (...args: unknown[]) => loginWithEmailMock(...args),
}));

vi.mock('@/lib/auth/local-auth/signup', () => ({
  signupWithEmail: (...args: unknown[]) => signupWithEmailMock(...args),
}));

vi.mock('sonner', () => ({
  toast: {
    error: (...args: unknown[]) => toastErrorMock(...args),
  },
}));

vi.mock('@/components/auth/OAuthProviderButtons', () => ({
  OAuthProviderButtons: (props: any) => {
    oauthPropsSpy(props);
    return (
      <div data-testid="oauth-providers">
        {props.providers.map((provider: any) => provider.key).join(',')}
      </div>
    );
  },
}));

vi.mock('@/components/auth/LocalAuthTabs', () => ({
  LocalAuthTabs: ({ onAuth, loading }: any) => (
    <div>
      <button
        data-testid="login-trigger"
        aria-label={loading ? 'auth-loading' : 'auth-ready'}
        disabled={loading}
        onClick={() =>
          onAuth(mockLoginPayload)
        }
      >
        Login Trigger
      </button>
      <button
        data-testid="signup-trigger"
        disabled={loading}
        onClick={() =>
          onAuth(mockSignupPayload)
        }
      >
        Signup Trigger
      </button>
    </div>
  ),
}));

import { AuthPanel } from '@/components/auth/AuthPanel';

describe('AuthPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLoginPayload = {
      email: 'login@example.com',
      password: 'pw',
      name: 'Login User',
      mode: 'login',
    };
    mockSignupPayload = {
      email: 'signup@example.com',
      password: 'pw',
      name: 'Signup User',
      mode: 'signup',
    };
  });

  it('filters OAuth providers and renders backend status', () => {
    render(
      <AuthPanel
        providers={[
          { key: 'github', name: 'GitHub' },
          { key: 'local', name: 'Email' },
          { key: 'azure', name: 'Microsoft' },
        ]}
        publicData={{
          status: 'ok',
          timestamp: new Date().toISOString(),
        }}
      />,
    );

    expect(oauthPropsSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        providers: [
          { key: 'github', name: 'GitHub' },
          { key: 'azure', name: 'Microsoft' },
        ],
        hasLocalAuth: true,
      }),
    );

    expect(screen.getByTestId('oauth-providers').textContent).toContain(
      'github,azure',
    );
    expect(
      screen.getByText(/Backend connection successful/i),
    ).toBeInTheDocument();
  });

  it('handles local login and signup flows with loading states', async () => {
    loginWithEmailMock.mockResolvedValueOnce(undefined);
    signupWithEmailMock.mockRejectedValueOnce(new Error('fail'));

    render(
      <AuthPanel
        providers={[
          { key: 'local', name: 'Email' },
          { key: 'github', name: 'GitHub' },
        ]}
        publicData={null}
      />,
    );

    const loginTrigger = screen.getByTestId('login-trigger');
    await userEvent.click(loginTrigger);

    expect(loginWithEmailMock).toHaveBeenCalledWith(
      'login@example.com',
      'pw',
    );

    await waitFor(() =>
      expect(loginTrigger).toHaveAttribute('aria-label', 'auth-ready'),
    );

    const signupTrigger = screen.getByTestId('signup-trigger');
    await userEvent.click(signupTrigger);

    expect(signupWithEmailMock).toHaveBeenCalledWith(
      'signup@example.com',
      'pw',
      'Signup User',
    );

    await waitFor(() =>
      expect(toastErrorMock).toHaveBeenCalledWith(
        'Signup failed',
        expect.any(Object),
      ),
    );
  });

  it('shows login failure toast when local login fails', async () => {
    loginWithEmailMock.mockRejectedValueOnce(new Error('invalid'));

    render(
      <AuthPanel
        providers={[{ key: 'local', name: 'Email' }]}
        publicData={null}
      />,
    );

    await userEvent.click(screen.getByTestId('login-trigger'));

    await waitFor(() =>
      expect(toastErrorMock).toHaveBeenCalledWith(
        'Login failed',
        expect.any(Object),
      ),
    );
  });

  it('renders OAuth-only mode without local auth', () => {
    render(
      <AuthPanel
        providers={[{ key: 'github', name: 'GitHub' }]}
        publicData={null}
      />,
    );

    expect(oauthPropsSpy).toHaveBeenCalledWith(
      expect.objectContaining({
        providers: [{ key: 'github', name: 'GitHub' }],
        hasLocalAuth: false,
      }),
    );
    expect(screen.queryByTestId('login-trigger')).not.toBeInTheDocument();
  });

  it('falls back to empty name when signup payload omits it', async () => {
    mockSignupPayload = {
      email: 'no-name@example.com',
      password: 'pw',
      name: undefined,
      mode: 'signup',
    };
    render(
      <AuthPanel
        providers={[{ key: 'local', name: 'Local' }]}
        publicData={null}
      />,
    );

    await userEvent.click(screen.getByTestId('signup-trigger'));
    expect(signupWithEmailMock).toHaveBeenCalledWith(
      'no-name@example.com',
      'pw',
      '',
    );
  });
});
