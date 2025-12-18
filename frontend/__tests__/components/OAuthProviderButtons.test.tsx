import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

vi.mock('@/lib/auth/providers/login-with-provider', () => ({
  loginWithProvider: vi.fn(),
}));

import { OAuthProviderButtons } from '@/components/auth/OAuthProviderButtons';
import { loginWithProvider } from '@/lib/auth/providers/login-with-provider';

describe('OAuthProviderButtons', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading state when no providers are available', () => {
    render(<OAuthProviderButtons providers={[]} hasLocalAuth={false} />);
    expect(
      screen.getByText(/Loading login options/i),
    ).toBeInTheDocument();
  });

  it('renders provider buttons and triggers login', async () => {
    const providers = [
      { key: 'github', name: 'GitHub' },
      { key: 'azure', name: 'Microsoft' },
      { key: 'custom', name: 'Custom' },
    ];

    render(<OAuthProviderButtons providers={providers} hasLocalAuth />);

    const githubButton = screen.getByRole('button', {
      name: /Sign in with GitHub/i,
    });
    const azureButton = screen.getByRole('button', {
      name: /Sign in with Microsoft/i,
    });
    const customButton = screen.getByRole('button', {
      name: /Sign in with Custom/i,
    });

    await userEvent.click(githubButton);
    await userEvent.click(azureButton);
    await userEvent.click(customButton);

    expect(loginWithProvider).toHaveBeenCalledWith('github');
    expect(loginWithProvider).toHaveBeenCalledWith('azure');
    expect(loginWithProvider).toHaveBeenCalledWith('custom');
    expect(loginWithProvider).toHaveBeenCalledTimes(3);

    expect(within(customButton).queryByRole('img')).toBeNull();
  });
});
