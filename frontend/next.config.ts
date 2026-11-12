import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  images: {
    remotePatterns: [{ protocol: "https", hostname: "images.unsplash.com" }],
  },
  async rewrites() {
    const backend = (process.env.ANEON_API_ORIGIN || "http://127.0.0.1:8080").replace(/\/$/, "");
    return [{ source: "/aneon-api/:path*", destination: `${backend}/api/v1/:path*` }];
  },
};

export default nextConfig;
