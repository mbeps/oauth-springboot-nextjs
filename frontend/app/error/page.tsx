import React, { Suspense } from 'react';
import ErrorClient from './ErrorClient';

/**
 * Error page wrapper with Suspense boundary for authentication errors.
 * Wraps client component that reads error query parameters.
 * @returns Error page with fallback loading state
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