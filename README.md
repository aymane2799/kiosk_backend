# Kiosk Backend

*Read this in [French / Français](README.fr.md).*

A multi-tenant, white-label **digital kiosk / content distribution** backend. Partners — newspapers
and sports media, but also enterprises, telecom / telephone service providers, banks, or any brand
that wants to offer a branded content kiosk to its own audience — each get a branded tenant; their
end users log in through the partner's identity provider,
browse a catalogue of tiered content (`FREE` / `PREMIUM`), favourite articles, and subscribe to a
premium plan through an asynchronous mock checkout. A separate back-office **admin** realm manages
tenants and their plans.

- **Stack:** Java 21, Spring Boot 4.1, Spring Security (stateless JWT), Spring Data JPA / Hibernate,
  PostgreSQL, Flyway, Lombok, Maven.
- **Base URL (local):** `http://localhost:8000`
- **API prefix:** every route lives under `/api/v1`
- **Full API contract:** [`docs/api_guide.md`](docs/api_guide.md) — request/response DTOs, TypeScript
  models, error shapes, and frontend integration notes. This README summarises it; that file is the
  source of truth.

---

## Table of contents

- [Architecture & core concepts](#architecture--core-concepts)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Database setup](#database-setup)
- [Running the application](#running-the-application)
- [Seeded development data](#seeded-development-data)
- [Building & testing](#building--testing)
- [Project structure](#project-structure)
- [API reference](#api-reference)
- [Error model](#error-model)
- [Known gaps / limitations](#known-gaps--limitations)

---

## Architecture & core concepts

### Tenants (white-label partners)
The platform is multi-tenant. A partner can be any organization that wants to run a branded content
kiosk for its audience — a media outlet, an enterprise, a telecom / telephone service provider, a
bank, a retailer, etc. Each partner is a `Tenant` with its own branding (`logoUrl`, `primaryColor`,
`secondaryColor`) and an identity-provider key (`providerId`). A frontend resolves a tenant by its
`slug` and themes itself from `GET /api/v1/tenants/{slug}/config`.

### Two auth realms — App users vs Admins
| Realm | Who | Login endpoint | Token claims | Guard |
|-------|-----|----------------|--------------|-------|
| **App user** | End users of a partner kiosk | `POST /api/v1/auth/login` | subject `userId`, `tenantId`, `role=USER` | `ROLE_USER` |
| **Admin** | Back-office operators | `POST /api/v1/admin/auth/login` | subject `adminId`, `role=ADMIN` (no `tenantId`) | `ROLE_ADMIN` on `/api/v1/admin/**` |

Both are the same JWT format, sent as `Authorization: Bearer <token>`. Default expiry is 24h; there is
no refresh endpoint yet.

### Partner login handshake
Real partner login is delegated to the partner's IdP. The backend expects a **`partnerToken`** — a
Base64-encoded JSON `PartnerIdentity` (`providerId`, `externalId`, `email`, `firstName`, `lastName`).
On first login the user row is auto-provisioned for that tenant. For local development,
`POST /api/v1/auth/mock-partner-token` mints a valid `partnerToken` without a real IdP.

### Entitlements
Content is tiered `FREE` / `PREMIUM`. A user may open `PREMIUM` content only while holding an
`ACTIVE`, non-expired `PREMIUM` subscription (latest subscription wins). Otherwise the detail
endpoint returns `403 INSUFFICIENT_TIER`.

### Asynchronous checkout
`POST /api/v1/subscriptions/checkout` creates a `PENDING` subscription + `PENDING` payment and returns
immediately. A background worker (a `ScheduledExecutorService`, see `config/PaymentExecutorConfig`)
resolves it after ~2s:
- success → subscription `ACTIVE`, `expiresAt = now + plan.billingPeriod` days;
- `simulateFailure: true` in the request → subscription `FAILED`.

Clients should **poll** `GET /api/v1/me/subscription` until `status` leaves `PENDING`.

### Money & IDs
All monetary amounts are **integers in minor units** (cents): `999` → `9.99`. `currency` is an ISO
code string. All entity IDs are UUID strings (`VARCHAR(36)`). Timestamps are ISO-8601 (`Instant`).

---

## Prerequisites

- **JDK 21** (`java -version` should report 21).
- **PostgreSQL 13+** running and reachable.
- No local Maven needed — use the bundled wrapper (`./mvnw` / `mvnw.cmd`).

---

## Configuration

Configuration lives in `src/main/resources/`:

- `application.yaml` — always active. App name and CORS allowed origins.
- `application-dev.yaml` — active only under the `dev` profile. Datasource, JPA/Flyway, JWT.

### Environment variables

| Variable | Used by | Default | Notes |
|----------|---------|---------|-------|
| `DATABASE_URL` | dev | _(none — required)_ | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/kiosk` |
| `DATABASE_USERNAME` | dev | _(none — required)_ | |
| `DATABASE_PASSWORD` | dev | _(none — required)_ | |
| `JWT_SECRET` | all | `kiosk_secret` | HMAC signing secret — **override in any shared environment** |
| `JWT_EXPIRATION` | all | `86400000` | Access-token lifetime, ms (24h) |
| `JWT_REFRESH_EXPIRATION` | all | `2592000000` | 30 days, ms (reserved — no refresh flow yet) |
| `CORS_ALLOWED_ORIGINS` | all | `http://localhost:3000,http://localhost:5173` | Comma-separated origin list |

The datasource variables are only referenced from `application-dev.yaml`, so **you must run with the
`dev` profile** (see below) for the app to start with a database.

---

## Database setup

1. Create a database:
   ```sql
   CREATE DATABASE kiosk;
   ```
2. Flyway owns the schema. Migrations in `src/main/resources/db/migrations` (`V1__…` … `V9__…`) run
   automatically on startup; Hibernate is set to `ddl-auto: validate` and only checks the mapping
   against the migrated schema.

> **Note (Spring Boot 4):** Flyway auto-configuration requires the explicit
> `org.springframework.boot:spring-boot-flyway` dependency (already in `pom.xml`) — without it Flyway
> silently does nothing.

---

## Running the application

Set the datasource env vars and activate the `dev` profile.

### macOS / Linux
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/kiosk
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Windows (PowerShell)
```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/kiosk"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "postgres"

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### From a packaged jar
```bash
./mvnw clean package
SPRING_PROFILES_ACTIVE=dev \
DATABASE_URL=jdbc:postgresql://localhost:5432/kiosk \
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres \
java -jar target/kiosk_backend-0.0.1-SNAPSHOT.jar
```

The server listens on **`http://localhost:8000`**.

---

## Seeded development data

Under the `dev` profile, `config/DevSeeder` populates the database on first run (only if it is empty):

**Admin** (`POST /api/v1/admin/auth/login`)

| Email | Password |
|-------|----------|
| `admin@kioskbridge.com` | `password` |

**Tenants:** `le-matin` (Le Matin), `radar-sport` (Radar Sport)

**App users** (use the `externalId` with `POST /api/v1/auth/mock-partner-token`)

| Tenant | externalId | Email |
|--------|-----------|-------|
| `le-matin` | `le-matin-user-1` | `alice@lematin.example` |
| `le-matin` | `le-matin-user-2` | `admin@lematin.example` |
| `radar-sport` | `radar-sport-user-1` | `bob@radar-sport.example` |

**Content:** 2 `FREE` + 2 `PREMIUM` articles (global, not tenant-scoped).

**Plans:** each tenant gets an `Essentiel` (`FREE`, 0) and a `Premium` (`PREMIUM`, `999` EUR / 30 days).

### Quick end-to-end login (dev)
```bash
# 1. mint a mock partner token
curl -s localhost:8000/api/v1/auth/mock-partner-token -H 'Content-Type: application/json' -d '{
  "tenantSlug":"le-matin","externalId":"le-matin-user-1",
  "email":"alice@lematin.example","firstName":"Alice","lastName":"Dupont"
}'

# 2. exchange it for a JWT
curl -s localhost:8000/api/v1/auth/login -H 'Content-Type: application/json' -d '{
  "tenantSlug":"le-matin","partnerToken":"<partnerToken from step 1>"
}'

# 3. call an authenticated endpoint
curl -s localhost:8000/api/v1/content -H 'Authorization: Bearer <token from step 2>'
```

---

## Building & testing

```bash
./mvnw clean verify      # compile + run tests
./mvnw test              # tests only
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Tests currently consist of the Spring context smoke test (`KioskApplicationTests`).

---

## Project structure

```
src/main/java/com/example/kiosk/
├── KioskApplication.java
├── admin/                  # admin entity + role
│   ├── auth/               # admin login (email/password → JWT)
│   └── tenants/            # admin tenant & plan management
├── auth/
│   ├── auth/               # app-user login, mock partner token, PartnerIdentity
│   ├── jwt/                # JwtService, JwtAuthenticationFilter
│   └── user/               # AppUser entity, repository, service
├── common/                 # ApiError, ApiPaths, GlobalExceptionHandler, Auditable, TenantContext
├── config/                 # SecurityConfig, DevSeeder, JpaAuditingConfig, PaymentExecutorConfig
├── content/                # Content entity, catalogue endpoints, tiers
├── entitlement/            # premium-access checks
├── favorite/               # per-user favourites
├── subscription/
│   ├── payment/            # Payment entity (internal to checkout)
│   ├── plan/               # tenant-scoped Plan
│   └── subscription/       # Subscription entity, async CheckoutService
└── tenant/                 # Tenant entity + public config/plans endpoints

src/main/resources/
├── application.yaml
├── application-dev.yaml
└── db/migrations/          # Flyway V1..V9
```

---

## API reference

Auth legend: `—` public · `USER` app-user token · `ADMIN` admin token.
All paths are prefixed with `/api/v1`. See [`docs/api_guide.md`](docs/api_guide.md) for bodies,
response shapes, and per-endpoint error codes.

### Auth
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `POST` | `/auth/login` | — | Exchange `{ tenantSlug, partnerToken }` for a user JWT. Auto-provisions the user on first login. |
| `POST` | `/auth/mock-partner-token` | — | Dev helper: mint a `partnerToken` for a tenant. |
| `POST` | `/admin/auth/login` | — | Admin login `{ email, password }` → admin JWT. |

### Tenant (public)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/tenants/{slug}/config` | — | Branding/theme bootstrap for a tenant. |
| `GET` | `/tenants/{slug}/plans` | — | Public plan catalogue for a tenant. |

### Content (app user)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/content` | USER | Full catalogue; `favorite` computed per user. |
| `GET` | `/content/{id}` | USER | Article detail. `403 INSUFFICIENT_TIER` if the user lacks the tier. |

### Favorites (app user)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/me/favorites` | USER | The user's favourited content. |
| `POST` | `/me/favorites` | USER | Add a favourite `{ contentId }`. Idempotent. `201`. |
| `DELETE` | `/me/favorites/{contentId}` | USER | Remove a favourite. `204`. |

### Subscriptions (app user)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| `GET` | `/me/subscription` | USER | Latest subscription for the user; `200` with **empty body** when none. |
| `POST` | `/subscriptions/checkout` | USER | Start checkout `{ planId, simulateFailure? }`. Returns `201` `PENDING`; resolves async after ~2s. |

### Admin — tenants (`ADMIN`)
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/admin/tenants` | List all tenants. |
| `GET` | `/admin/tenants/{id}` | Single tenant by id. `404` for an unknown id. |
| `POST` | `/admin/tenants` | Create a tenant. `409` on slug/name conflict. |
| `PUT` | `/admin/tenants/{id}` | Update a tenant (note: `slug` changes are ignored by the service). |
| `GET` | `/admin/tenants/{id}/users` | Users belonging to a tenant. |
| `GET` | `/admin/tenants/{id}/plans` | Plans for a tenant. |
| `POST` | `/admin/tenants/{id}/plans` | Create a plan for a tenant. `201`. |

---

## Error model

Errors from a controller or service are unified by `GlobalExceptionHandler` into an `ApiError` body:

```jsonc
{
  "code": "NOT_FOUND",              // stable machine-readable key — branch on this
  "message": "Unknown tenant",       // human-readable fallback
  "status": 404,
  "path": "/api/v1/tenants/foo/config",
  "timestamp": "2026-09-07T12:34:56.789Z",
  "errors": [ { "field": "email", "message": "must be a valid email" } ]  // only for VALIDATION_ERROR
}
```

| Status | `code` | When |
|--------|--------|------|
| `400` | `VALIDATION_ERROR` | Bean-validation failure on a request body (detail in `errors[]`). |
| `400` | `MALFORMED_REQUEST` | Body missing or not valid JSON. |
| `401` | `UNAUTHORIZED` | Bad credentials / invalid partner token / provider mismatch. |
| `403` | `INSUFFICIENT_TIER` | User lacks the tier for `GET /content/{id}`. |
| `404` | `NOT_FOUND` | Unknown tenant / content / plan / user / favourite. |
| `409` | `CONFLICT` | Tenant `slug` or `name` already taken. |
| `500` | `INTERNAL_ERROR` | Any uncaught exception. |

**Exception:** the security layer short-circuits before reaching a controller, so a missing/expired
JWT (`401`) and a wrong-realm/role rejection (`403`) return the raw servlet error body
(`{ timestamp, status, error, path }`, no `code`). Fall back to the HTTP status in those cases.

---