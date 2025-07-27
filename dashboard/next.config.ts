/** @type {import('next').NextConfig} */
const nextConfig = {
  serverExternalPackages: ['dockerode'],
  basePath: "/manage",
  trailingSlash: true
};

module.exports = nextConfig;
