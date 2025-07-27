#!/bin/bash
set -e

echo "Building and deploying Next.js app with Docker Compose..."

# Build and start the container
docker compose up --build -d

echo "Deployment complete!"
