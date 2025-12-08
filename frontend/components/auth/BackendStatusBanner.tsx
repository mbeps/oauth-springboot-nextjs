"use client";

import type { PublicData } from "@/types/public-data";

type BackendStatusBannerProps = {
  publicData: PublicData;
};

/**
 * Shows backend health info when available.
 * @param publicData Public status payload.
 * @returns Status banner UI.
 * @author Maruf Bepary
 */
export const BackendStatusBanner = ({
  publicData,
}: BackendStatusBannerProps) => (
  <div className="mt-4 p-3 bg-green-50 border border-green-200 rounded">
    <p className="text-sm text-green-700">✅ Backend connection successful</p>
    <p className="text-xs text-green-600 mt-1">
      Status: {publicData.status} |{" "}
      {new Date(publicData.timestamp).toLocaleTimeString()}
    </p>
  </div>
);
