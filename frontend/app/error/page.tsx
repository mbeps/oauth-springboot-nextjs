import React, { Suspense } from 'react';
import ErrorClient from './ErrorClient';

/**
 * Error page that wraps the client reader in Suspense.
 * @returns Error view with a simple loading fallback.
 * @author Maruf Bepary
 */
export default function ErrorPage() {
  return (
    <Suspense fallback={<div className="min-h-screen flex items-center justify-center">Loading...</div>}>
      {/* ErrorClient is a client component that reads search params */}
      <ErrorClient />
    </Suspense>
  );
}
