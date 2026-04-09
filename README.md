# TourBooking monorepo

## Layout
- `backend/`: Spring Boot module that now owns the Java sources plus `src/main/resources` data/config.
- `frontend/`: Static HTML/CSS/JS tree. `frontend/assets/css`, `frontend/assets/js`, `frontend/pages`, `frontend/pages/admin`, `frontend/pages/auth`, `frontend/favicon.ico`, and `frontend/scripts/sync-to-backend.ps1` live here exactly as in the diagram, and Maven copies them into `backend/src/main/resources/static` every time the backend build runs.

## Working with the UI
1. Edit anything under `frontend/`.
2. Sync to the backend static directory in one of two ways:
   - Let Maven do it with `mvn -f backend/pom.xml process-resources` (or just `mvn -f backend/pom.xml clean package` / `spring-boot:run`).
   - Run `powershell -File frontend/scripts/sync-to-backend.ps1` to copy `assets`, `pages`, and `favicon.ico` into `backend/src/main/resources/static`.

## Running
- **Backend server:** `mvn -f backend/pom.xml spring-boot:run`.
- **Frontend-only:** serve `frontend/` with any static server (`npx http-server frontend -p 3000` or `npx live-server frontend`).

## Cleanup
- Top-level `backend/` now keeps only the Java project; the former `backend/src/main/resources/static/assets`/`pages` tree has been removed so Maven always copies from `frontend/`.
- All of the `.log` / `.txt` files in `backend/` root were deleted so the module starts in a clean state before the next build.
- There is still a leftover `booking/` folder that contains the original `.m2` cache; it could not be removed from inside the sandbox because several JARs in `booking/backend/.m2` were locked. You can delete that directory manually once the running process releases the files.
