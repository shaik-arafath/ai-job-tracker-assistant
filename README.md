# AI Job Tracker and Assistant

Production-ready full-stack starter for a job tracking SaaS:

- Frontend: React + TypeScript + Vite
- Backend: Spring Boot 3 + JWT + JPA
- Database: MySQL
- AI: OpenAI-compatible service layer
- Deployment: Docker Compose + Kubernetes manifests

## Quick Start (Local)

1. Ensure Docker Desktop is running.
2. From project root:

```bash
docker compose up --build
```

3. Open:
- Frontend: http://localhost:5173
- Backend health: http://localhost:8080/api/v1/health

## Env Vars

### Backend
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`
- `OPENAI_API_KEY`
- `OPENAI_MODEL` (default `gpt-4o-mini`)

### Frontend
- `VITE_API_BASE_URL` (default `http://localhost:8080/api/v1`)

## Production Deployment

- Build images:
```bash
docker build -t your-registry/ai-job-tracker-backend:latest ./backend
docker build -t your-registry/ai-job-tracker-frontend:latest ./frontend
```

- Push to registry and apply manifests in `deploy/k8s`.

## Notes

- The app intentionally does not automate job applications.
- URL import only supports public metadata extraction patterns.
