import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it } from 'vitest';

import { Button } from '@/components/ui/button';

describe('Button', () => {
  it('renders with variant and size styles', () => {
    render(
      <Button variant="destructive" size="sm">
        Delete
      </Button>,
    );

    const button = screen.getByRole('button', { name: /delete/i });
    expect(button.className).toContain('bg-destructive');
    expect(button.className).toContain('h-8');
  });

  it('supports asChild rendering', async () => {
    render(
      <Button asChild variant="link">
        <a href="/docs" data-testid="link-child">
          Docs
        </a>
      </Button>,
    );

    const link = screen.getByTestId('link-child');
    expect(link.tagName.toLowerCase()).toBe('a');
    expect(link.className).toContain('text-primary');

    await userEvent.click(link);
  });
});
