#!/bin/bash
# Quick start script for Code Review Dashboard
set -e

echo "🔍 CodeReviewer AI Dashboard - Quick Start"
echo "========================================"

# Check prerequisites
command -v docker >/dev/null 2>&1 || { echo "❌ Docker is required but not installed. Aborting."; exit 1; }
command -v docker-compose >/dev/null 2>&1 || command -v docker compose >/dev/null 2>&1 || { echo "❌ Docker Compose is required. Aborting."; exit 1; }

# Setup .env if not present
if [ ! -f .env ]; then
  echo "📝 Creating .env from .env.example..."
  cp .env.example .env
  echo ""
  echo "⚠️  Please edit .env and add your:"
  echo "   - GITHUB_TOKEN (from https://github.com/settings/tokens)"
  echo "   - GROQ_API_KEY (from https://console.groq.com)"
  echo ""
  read -p "Press Enter to continue with empty API keys (mock mode)..."
fi

echo "🐳 Starting Docker containers..."
docker-compose up -d --build

echo ""
echo "⏳ Waiting for services to be ready..."
sleep 15

echo ""
echo "✅ Services started!"
echo ""
echo "🌐 Dashboard:  http://localhost:3000"
echo "🔧 Backend:    http://localhost:8080/api/health"
echo "🗄️  H2 Console: http://localhost:8080/h2-console"
echo ""
echo "📝 Next steps:"
echo "  1. Open http://localhost:3000"
echo "  2. Click '+ Add Repo' and add a GitHub repo (e.g., owner: facebook, name: react)"
echo "  3. Click '▶ Run Analysis' to fetch and analyze commits"
echo ""
echo "📋 Useful commands:"
echo "  docker-compose logs -f backend  # View backend logs"
echo "  docker-compose down             # Stop all services"
