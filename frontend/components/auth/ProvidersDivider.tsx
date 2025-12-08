"use client";

/**
 * Visual divider between OAuth and local auth.
 * @returns Divider UI.
 * @author Maruf Bepary
 */
export const ProvidersDivider = () => (
  <div className="relative">
    <div className="absolute inset-0 flex items-center">
      <span className="w-full border-t" />
    </div>
    <div className="relative flex justify-center text-xs uppercase">
      <span className="bg-white px-2 text-gray-500">Or continue with</span>
    </div>
  </div>
);
