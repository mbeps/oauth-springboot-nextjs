import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { BackendStatusBanner } from '@/components/auth/BackendStatusBanner';
import { ProvidersDivider } from '@/components/auth/ProvidersDivider';

describe('BackendStatusBanner', () => {
  it('renders backend status details', () => {
    const timestamp = new Date('2024-01-01T00:00:00Z').toISOString();
    render(
      <BackendStatusBanner publicData={{ status: 'ok', timestamp }} />,
    );

    expect(
      screen.getByText(/Backend connection successful/i),
    ).toBeInTheDocument();
    expect(screen.getByText(/Status: ok/i)).toBeInTheDocument();
  });
});

describe('ProvidersDivider', () => {
  it('shows a divider label', () => {
    render(<ProvidersDivider />);
    expect(
      screen.getByText(/Or continue with/i),
    ).toBeInTheDocument();
  });
});
