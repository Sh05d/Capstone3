# Khutaa — Capstone 3

**Khutaa** is a Spring Boot backend that helps students prepare for tech careers through AI-powered learning tools, mentor mock interviews, skill tracking, job analysis, challenges, and learning groups.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Project Structure](#project-structure)
- [Architecture Diagram](#architecture-diagram)
- [Entity Relationship Diagram](#entity-relationship-diagram)
- [Sequence Diagrams](#sequence-diagrams)
- [Team Contributions](#team-contributions)
- [API Base URL](#api-base-url)

---

## Overview

Khutaa supports three main user roles:

| Role | Description |
|------|-------------|
| **Student** | Builds a profile (CV, GitHub), earns XP via tasks and challenges, tracks a career roadmap, analyzes job postings, and books AI or mentor mock interviews |
| **Mentor** | Conducts mock interviews, submits reports, and receives student reviews |
| **Admin** | Manages skills, approves mentors, and oversees platform content |

Key integrations:

- **OpenAI** — task generation, answer grading, job analysis, interview questions, challenge validation
- **Email (SMTP)** — interview scheduling and PDF report delivery
- **WhatsApp (UltraMsg)** — interview request and reminder notifications
- **Zoom / Google Meet** — mentor interview meeting links
- **GitHub API** — student profile enrichment

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 4.0.6 |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Validation | Jakarta Bean Validation |
| Email | Spring Mail |
| PDF | iText html2pdf, Apache PDFBox |
| Build | Maven |

---

## Prerequisites

- **Java 17+**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- **MySQL 8+** with a database named `capstone_3`
- Optional but recommended for full functionality:
  - OpenAI API key
  - SMTP credentials (Gmail App Password or similar)
  - UltraMsg token (WhatsApp)
  - GitHub API token (higher rate limits for profile fetch)

---

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
cd Capstone_3
```

### 2. Create the database

```sql
CREATE DATABASE capstone_3;
```

### 3. Configure local secrets

Copy the example properties file and fill in your values:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

At minimum, set:

```properties
spring.datasource.password=your_mysql_password
openai.api.key=sk-your-key-here
```

See [Configuration](#configuration) for the full list of optional settings.

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API starts on **http://localhost:8080** by default.

### 5. Verify

```bash
curl http://localhost:8080/api/v1/student/get
```

---

## Configuration

| Property | Description | Required |
|----------|-------------|----------|
| `spring.datasource.password` | MySQL password | Yes |
| `openai.api.key` | OpenAI API key for AI features | Yes (for AI endpoints) |
| `spring.mail.*` | SMTP settings for email notifications | For email features |
| `ultramsg.api-url` / `ultramsg.token` | WhatsApp notifications | For WhatsApp features |
| `github.api.token` | GitHub API token | Optional (rate limits) |
| `ai.model` | OpenAI model (default: `gpt-4o-mini`) | No |
| `spring.jpa.hibernate.ddl-auto` | Schema mode (default: `update`) | No |

Non-secret defaults live in `src/main/resources/application.properties`. Secrets belong in `application-local.properties` (gitignored).

---

## Project Structure

```
src/main/java/org/example/capstone_3/
├── AI/              # OpenAI integration (AiService, parsers)
├── Api/             # Shared API types (ApiResponse, ApiException)
├── Controller/      # REST endpoints
├── DTO/IN           # Request bodies
├── DTO/OUT          # Response bodies
├── Model/           # JPA entities
├── Repository/      # Spring Data repositories
└── Service/         # Business logic

docs/images/         # Architecture & sequence diagram assets
```

---

## Architecture Diagram

![Khutaa Layered Architecture](docs/images/khutaa_capstone3_architecture_hd.png)

---

## Entity Relationship Diagram

![Khutaa Entity Relationship Diagram](docs/images/khutaa_capstone3_er_hd.png)

---

## Sequence Diagrams

### Mentor Mock Interview (Request & Accept)

![Mentor Mock Interview Sequence](docs/images/khutaa_seq_mentor_mock_interview_hd.png)

### AI Mock Interview

![AI Mock Interview Sequence](docs/images/khutaa_seq_ai_mock_interview_hd.png)

### Task Submission (AI Grading & XP)

![Task Submission Sequence](docs/images/khutaa_seq_task_submission_hd.png)

### Job Analysis

![Job Analysis Sequence](docs/images/khutaa_seq_job_analysis_hd.png)

### Challenge Attempt

![Challenge Attempt Sequence](docs/images/khutaa_seq_challenge_attempt_hd.png)

### Mentor Report & Email (PDF)

![Mentor Report Email Sequence](docs/images/khutaa_seq_mentor_report_email_hd.png)

### Student Review

![Student Review Sequence](docs/images/khutaa_seq_review_hd.png)

### Task Publishing

![Task Publish Sequence](docs/images/khutaa_seq_task_publish_hd.png)

---

## Team Contributions

### Saud Shafie

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/student/leaderboard` | Returns the full student leaderboard ordered by XP |
| `GET` | `/api/v1/student/leaderboard/rank/{id}` | Returns a single student's leaderboard rank and XP |
| `PUT` | `/api/v1/student/{id}/cv` | Updates a student's CV URL; extracts and stores CV text from the PDF |
| `PUT` | `/api/v1/student/{id}/github` | Updates a student's GitHub URL; fetches and stores profile text |
| `GET` | `/api/v1/review/student/{studentId}` | Returns all reviews written by a student |
| `GET` | `/api/v1/review/student/{studentId}/reviewable-interviews` | Returns completed mentor interviews the student can still review |
| `GET` | `/api/v1/mentor/get/{id}/reviews` | Returns all reviews received by a mentor |
| `POST` | `/api/v1/admin/{adminId}/skills/generate-by-category` | Admin generates AI skill suggestions for a category and saves them |
| `GET` | `/api/v1/admin/{adminId}/mentor/pending` | Returns mentors awaiting admin approval |
| `PUT` | `/api/v1/admin/{adminId}/mentor/{mentorId}/approve` | Approves a pending mentor |
| `PUT` | `/api/v1/admin/{adminId}/mentor/{mentorId}/unapprove` | Revokes a mentor's approval |
| `POST` | `/api/v1/job-analysis/add/{studentId}` | Runs AI job analysis against a student's profile and saves the result |
| `GET` | `/api/v1/student/{studentId}/roadmap/{roadmapId}/current-step` | Returns the student's current active roadmap step |

#### Example requests (Saud Shafie endpoints)

**Update CV**

```http
PUT /api/v1/student/1/cv
Content-Type: application/json

{
  "cvUrl": "https://example.com/resume.pdf"
}
```

**Update GitHub**

```http
PUT /api/v1/student/1/github
Content-Type: application/json

{
  "githubUrl": "https://github.com/username"
}
```

**Add job analysis**

```http
POST /api/v1/job-analysis/add/1
Content-Type: application/json

{
  "jobDescription": "We are looking for a Java developer with Spring Boot experience..."
}
```

**Generate skills by category (Admin)**

```http
POST /api/v1/admin/1/skills/generate-by-category
Content-Type: application/json

{
  "category": "Programming"
}
```

Valid categories: `Programming`, `Database`, `Testing`, `DevOps`, `Soft Skill`, `AI`, `Security`, `Other`.

---

## API Base URL

```
http://localhost:8080/api/v1
```

All endpoints return JSON. Successful mutations typically respond with an `ApiResponse` message or the created/updated DTO.

Errors are returned with HTTP 4xx/5xx and a message body when `spring.web.error.include-message=always` is enabled.

---

## License

Capstone project — see repository for license details.
