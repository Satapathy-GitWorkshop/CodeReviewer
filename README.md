# 🔍 CodeReviewer AI Dashboard

An AI-powered code review analytics platform that automatically analyzes GitHub commits, performs static code analysis, generates AI-powered reviews using Groq's free API, and displays insights in a beautiful dashboard.

---

## 📸 Features

- **GitHub Integration** — Fetch commits from any public/private repository
- **Static Analysis** — Pattern-based detection of bugs, security issues, and code smells (Java, Python, JavaScript)
- **AI Code Reviews** — Powered by Groq API (Llama 3 70B) — free tier supported
- **Risk Scoring** — Each commit gets a 1–10 risk score with explanation
- **Interactive Dashboard** — Real-time charts, KPI cards, and commit drill-down
- **Scheduled Analysis** — Auto-runs every 6 hours
- **Dark/Light Theme** — Toggle between themes
- **Free Tier Friendly** — Runs entirely on free infrastructure

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────┐
│                  React Frontend                   │
│  Dashboard │ Commits List │ Commit Detail         │
└────────────────────┬─────────────────────────────┘
                     │ REST API
┌────────────────────▼─────────────────────────────┐
│              Spring Boot Backend                  │
│  Controllers │ Services │ Schedulers              │
│  ┌──────────────────────────────────────────┐    │
│  │ GitHub Client │ Groq Client              │    │
│  │ Static Analysis │ AI Summary             │    │
│  └──────────────────────────────────────────┘    │
└────────────────────┬─────────────────────────────┘
                     │ JPA
┌────────────────────▼─────────────────────────────┐
│         PostgreSQL / H2 Database                  │
│  repositories │ commits │ analysis_results        │
│  ai_summaries                                     │
└──────────────────────────────────────────────────┘
```

---

## ⚡ Quick Start (5 minutes)

### Option 1: Docker Compose (Recommended)

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd code-review-dashboard

# 2. Copy and configure environment
cp .env.example .env
# Edit .env with your GitHub token and Groq API key

# 3. Start everything
docker-compose up -d

# 4. Open the dashboard
open http://localhost:3000
```

### Option 2: Manual Setup

**Backend:**
```bash
cd backend
# Set environment variables (or create application-local.yml)
export GITHUB_TOKEN=your_github_token
export GROQ_API_KEY=your_groq_api_key

mvn spring-boot:run
# Backend runs at http://localhost:8080
```

**Frontend:**
```bash
cd frontend
npm install
npm start
# Frontend runs at http://localhost:3000
```

---

## 🔑 Configuration

### Required: GitHub Token

1. Go to https://github.com/settings/tokens
2. Click "Generate new token (classic)"
3. Select scopes: `repo` (for private repos) or `public_repo` (for public only)
4. Copy the token → set as `GITHUB_TOKEN` in `.env`

> **Without a GitHub token:** The app still works but with a rate limit of 60 requests/hour (unauthenticated). Good for testing public repos.

### Optional: Groq API Key (for AI reviews)

1. Go to https://console.groq.com
2. Create a free account
3. Generate an API key → set as `GROQ_API_KEY` in `.env`

> **Without Groq API key:** The app generates mock AI reviews. Real analysis requires the key.

---

## 📡 API Reference

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/repositories` | List all repositories |
| POST | `/api/repositories` | Add a repository `{"owner":"facebook","name":"react"}` |
| GET | `/api/commits?repoId={id}` | Get commits for a repository |
| GET | `/api/commits/{sha}/analysis` | Get full analysis for a commit |
| GET | `/api/dashboard/metrics?repoId={id}` | Get dashboard KPIs and charts |
| POST | `/api/analyze/manual` | Trigger manual analysis `{"repositoryId": 1}` |
| GET | `/api/health` | Health check |
| GET | `/h2-console` | H2 database console (dev only) |

---

## 🗄️ Database Schema

```sql
repositories   -- GitHub repos being tracked
commits        -- Individual commits with metadata
analysis_results -- Static analysis findings per commit
ai_summaries   -- AI-generated reviews and risk scores
```

---

## 🚀 Deployment

### Backend → Render (Free)

1. Push your code to GitHub
2. Go to https://render.com → New Web Service
3. Connect your GitHub repo, set root directory to `backend`
4. Build command: `mvn clean package -DskipTests`
5. Start command: `java -jar target/code-review-dashboard-1.0.0.jar`
6. Add environment variables:
   - `SPRING_PROFILES_ACTIVE=postgres`
   - `DATABASE_URL` (from Render's free PostgreSQL)
   - `GITHUB_TOKEN`
   - `GROQ_API_KEY`

### Frontend → Vercel (Free)

1. Go to https://vercel.com → New Project
2. Import your GitHub repo, set root to `frontend`
3. Add environment variable:
   - `REACT_APP_API_URL=https://your-backend.onrender.com/api`
4. Deploy!

### Keep Backend Alive (Render Free Tier)

Render's free tier spins down after 15 minutes of inactivity. Use [cron-job.org](https://cron-job.org) to ping your `/api/health` endpoint every 10 minutes for free.

---

## 🧪 Testing

```bash
# Backend tests
cd backend
mvn test

# With PostgreSQL (integration tests)
mvn test -Dspring.profiles.active=postgres
```

---

## 📦 Project Structure

```
code-review-dashboard/
├── backend/
│   ├── src/main/java/com/codereview/
│   │   ├── client/          # GitHub & Groq API clients
│   │   ├── config/          # Spring configuration
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data transfer objects
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Spring Data repositories
│   │   ├── scheduler/       # Scheduled jobs
│   │   └── service/         # Business logic
│   └── src/main/resources/
│       ├── db/migration/    # Flyway SQL migrations
│       └── application.yml
├── frontend/
│   └── src/
│       ├── api/             # Axios API client
│       ├── components/      # React components
│       ├── context/         # React context (state)
│       ├── pages/           # Page components
│       └── types/           # TypeScript interfaces
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🆓 Free Tier Limits

| Service | Limit | Our Usage |
|---------|-------|-----------|
| GitHub API | 5,000 req/hr | ~20 req/analysis |
| Groq API | 30 req/min, 1440/day | 1 req/commit |
| Render | 750 hrs/month | ~720 hrs/month |
| Render DB | 1 GB | ~50MB typical |
| GitHub Actions | 2,000 min/month | ~5 min/deploy |

---

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.2, Spring Data JPA, Flyway, Lombok
- **Frontend:** React 18, TypeScript, Recharts, React Router
- **Database:** H2 (dev), PostgreSQL (production)
- **AI:** Groq API (Llama 3 70B — free tier)
- **DevOps:** Docker, GitHub Actions, Render, Vercel