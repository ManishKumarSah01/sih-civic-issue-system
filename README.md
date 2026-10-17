# SIH Smart Civic Issue Reporting System

Runnable starter full-stack application:
- React + Vite frontend
- Java 21 + Spring Boot 3.5.6 REST API
- MySQL + Spring Data JPA
- JWT authentication with CITIZEN / ADMIN roles
- Complaint create/list/status updates
- Image upload to local `uploads/` directory
- Email notifications (optional SMTP configuration)
- Leaflet / OpenStreetMap map picker

## Requirements
- Java 21
- Maven 3.9+
- Node.js 20+
- MySQL 8+

## 1. Create database
```sql
CREATE DATABASE civic_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 2. Configure backend
Copy `backend/.env.example` values into your environment or edit `backend/src/main/resources/application.properties`.
Set `DB_USERNAME`, `DB_PASSWORD`, and (optionally) SMTP variables. Do not commit real passwords.
The app creates tables automatically for local development (`ddl-auto=update`).

## 3. Start backend
```bash
cd backend
mvn spring-boot:run
```
API base URL: `http://localhost:8080/api`
Uploads are served at `http://localhost:8080/uploads/<filename>`.

### Create an admin
Register an account from the UI, then promote it in MySQL:
```sql
UPDATE users SET role='ADMIN' WHERE email='your-email@example.com';
```
Log out and log back in after promoting the account.

## 4. Start frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Open the URL printed by Vite (normally `http://localhost:5173`).

## 5. Email configuration (optional)
Set these environment variables before starting the backend:
- `MAIL_HOST` (e.g. `smtp.gmail.com`)
- `MAIL_PORT` (e.g. `587`)
- `MAIL_USERNAME`
- `MAIL_PASSWORD` (use an app password where required)
- `MAIL_FROM` (sender address)

Without SMTP credentials, the application logs a warning and still saves the complaint/status change. Never commit SMTP credentials.

## API overview
- `POST /api/auth/register` — `{ "name":"A User", "email":"user@example.com", "password":"StrongPass123" }`
- `POST /api/auth/login` — `{ "email":"user@example.com", "password":"StrongPass123" }`
- `GET /api/complaints` — citizen sees their own; admin sees all
- `POST /api/complaints` — multipart fields `title`, `description`, `category`, `location`, `latitude`, `longitude`, optional `image`
- `GET /api/complaints/{id}` — own complaint or admin
- `PATCH /api/complaints/{id}/status` — admin only; JSON `{ "status":"IN_PROGRESS" }`
- `GET /api/complaints/stats` — admin only

Allowed statuses: `PENDING`, `IN_PROGRESS`, `RESOLVED`, `REJECTED`.
Categories are free text in this starter.

## Notes before deployment
This is a learning/hackathon starter, not a production-certified service. Before public deployment add rate limiting, stronger validation, virus scanning and size limits for uploads, cloud/object storage, HTTPS, audit logs, account recovery, email verification, database migrations, and stricter admin provisioning. Restrict CORS to your actual frontend origin. OpenStreetMap tile usage is subject to its usage policy.
