import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { ProtectedActionsCard } from '@/components/ProtectedActionsCard';

describe('ProtectedActionsCard', () => {
  it('invokes provided actions when buttons are clicked', async () => {
    const onAction = vi.fn().mockResolvedValue(undefined);
    render(<ProtectedActionsCard onAction={onAction} loading={false} />);

    await userEvent.click(
      screen.getByRole('button', { name: /Test Action/i }),
    );
    await userEvent.click(
      screen.getByRole('button', { name: /Sample Operation/i }),
    );

    expect(onAction).toHaveBeenCalledWith('test_action');
    expect(onAction).toHaveBeenCalledWith('sample_operation');
    expect(onAction).toHaveBeenCalledTimes(2);
  });

  it('disables buttons when loading', () => {
    render(<ProtectedActionsCard onAction={vi.fn()} loading />);

    screen
      .getAllByRole('button')
      .forEach((button) => expect(button).toBeDisabled());
  });
});
