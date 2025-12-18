import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { LocalAuthTabs } from '@/components/auth/LocalAuthTabs';

describe('LocalAuthTabs', () => {
  it('submits login payload from the login tab', async () => {
    const onAuth = vi.fn().mockResolvedValue(undefined);
    render(<LocalAuthTabs loading={false} onAuth={onAuth} />);

    await userEvent.type(
      screen.getByLabelText(/^Email$/i),
      'user@example.com',
    );
    await userEvent.type(
      screen.getByLabelText(/^Password$/i),
      'super-secret',
    );
    await userEvent.click(
      screen.getByRole('button', { name: /Login/i }),
    );

    expect(onAuth).toHaveBeenCalledWith({
      email: 'user@example.com',
      password: 'super-secret',
      name: '',
      mode: 'login',
    });
  });

  it('submits signup payload from the signup tab', async () => {
    const onAuth = vi.fn().mockResolvedValue(undefined);
    render(<LocalAuthTabs loading={false} onAuth={onAuth} />);

    await userEvent.click(screen.getByRole('tab', { name: /Sign Up/i }));
    const signupPanel = screen.getByRole('tabpanel', { name: /Sign Up/i });
    const nameInput = within(signupPanel).getByLabelText(/^Name$/i);
    const signupEmail = within(signupPanel).getByLabelText(/^Email$/i);
    const signupPassword = within(signupPanel).getByLabelText(
      /^Password$/i,
    );

    await userEvent.type(nameInput, 'Test User');
    await userEvent.type(signupEmail, 'new@example.com');
    await userEvent.type(signupPassword, 'top-secret');

    await userEvent.click(
      screen.getByRole('button', { name: /Create Account/i }),
    );

    expect(onAuth).toHaveBeenCalledWith({
      email: 'new@example.com',
      password: 'top-secret',
      name: 'Test User',
      mode: 'signup',
    });
  });

  it('disables submission buttons while loading', async () => {
    render(<LocalAuthTabs loading onAuth={vi.fn()} />);

    expect(
      screen.getByRole('button', { name: /Processing.../i }),
    ).toBeDisabled();

    await userEvent.click(screen.getByRole('tab', { name: /Sign Up/i }));
    expect(
      screen.getByRole('button', { name: /Processing.../i }),
    ).toBeDisabled();
  });
});
