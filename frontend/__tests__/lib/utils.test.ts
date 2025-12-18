import { describe, expect, it } from 'vitest';

import { cn } from '@/lib/utils';

describe('cn', () => {
  it('merges class names and drops falsy values', () => {
    expect(cn('px-2', false && 'hidden', 'text-sm', undefined, 'px-2')).toBe(
      'text-sm px-2',
    );
  });
});
