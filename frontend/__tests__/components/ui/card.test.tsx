import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

describe('Card primitives', () => {
  it('renders all card slots with custom class names', () => {
    render(
      <Card className="custom-card">
        <CardHeader className="custom-header">
          <CardTitle className="custom-title">Title</CardTitle>
          <CardDescription className="custom-description">
            Description
          </CardDescription>
          <CardAction className="custom-action">Action</CardAction>
        </CardHeader>
        <CardContent className="custom-content">Content</CardContent>
        <CardFooter className="custom-footer">Footer</CardFooter>
      </Card>,
    );

    expect(screen.getByText('Title').closest('[data-slot="card"]')).toHaveClass(
      'custom-card',
    );
    expect(
      screen.getByText('Description').closest('[data-slot="card-header"]'),
    ).toHaveClass('custom-header');
    expect(screen.getByText('Title')).toHaveClass('custom-title');
    expect(screen.getByText('Description')).toHaveClass('custom-description');
    expect(screen.getByText('Action')).toHaveClass('custom-action');
    expect(screen.getByText('Content')).toHaveClass('custom-content');
    expect(screen.getByText('Footer')).toHaveClass('custom-footer');
  });
});
