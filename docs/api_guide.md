# Kiosk Backend — API Guide

Reference for building the Vue 3 + TypeScript frontend against the `kiosk_backend` service.
Everything below is derived from the current source (Spring Boot 4.1, Java 21), current as of commit `085f590`
(unified `ApiError` body, `favorite` flag on content responses, `DELETE /me/favorites/{contentId}`,
`GET /admin/tenants/{id}`, and the `GET /content` `locked` flag now fixed to be tier-aware).

- **Base URL (local):** `http://localhost:8000`
- **API prefix:** every route is under `/api/v1`
- **Auth:** stateless JWT, sent as `Authorization: Bearer <token>`
- **CORS:** allowed origins are `http://localhost:3000` and `http://localhost:5173` (override with `CORS_ALLOWED_ORIGINS`, comma‑separated). `Authorization` is an exposed header, credentials are allowed, methods `GET/POST/PUT/DELETE/OPTIONS`.
- **Content type:** `application/json` for all request/response bodies.
- **Dates:** serialized as ISO‑8601 strings (`Instant`), e.g. `"2026-09-06T12:34:56.789Z"`.
- **Money:** all amounts (`price`, `amount`) are **integers in minor units** (cents). `999` → `9.99`. `currency` is an ISO code string (`"EUR"`).
- **IDs:** all entity IDs are UUID strings (`VARCHAR(36)`).
- **Errors:** application errors return a unified `ApiError` body (see §5). Auth (`401`) and role (`403`) rejections from the security layer are the exception — they still use the raw servlet error shape.

---

## 1. Concepts

### Tenants (white‑label partners)
The platform is multi‑tenant. Each partner (e.g. *Le Matin*, *Radar Sport*) is a `Tenant` with its own
branding (`logoUrl`, `primaryColor`, `secondaryColor`) and an identity provider (`providerId`).
The frontend is expected to be tenant‑aware: it resolves a tenant by its `slug` (from the URL, subdomain,
or build config) and themes itself from `GET /tenants/{slug}/config`.

### Users vs Admins — two separate auth realms
| Realm | Who | Login endpoint | Token subject | Guards |
|-------|-----|----------------|---------------|--------|
| **App user** | End users of a partner kiosk | `POST /auth/login` | `userId`, claims `tenantId` + `role=USER` | `ROLE_USER` |
| **Admin** | Back‑office operators | `POST /admin/auth/login` | `adminId`, claim `role=ADMIN`, **no** `tenantId` | `ROLE_ADMIN` on `/admin/**` |

Both token types are the same JWT format and go in the same `Authorization: Bearer` header.
Keep them in separate storage slots in the frontend (e.g. a user app and an admin app / separate stores).

### Partner login handshake
Real partner login is delegated to the partner's identity provider. The backend expects a
**`partnerToken`** — a Base64‑encoded JSON `PartnerIdentity` (`providerId`, `externalId`, `email`,
`firstName`, `lastName`). On first login the user row is auto‑provisioned for that tenant.

For local development, `POST /auth/mock-partner-token` mints a valid `partnerToken` for a tenant so you
can exercise the flow without a real IdP.

### Entitlements
Content is tiered `FREE` / `PREMIUM`. A user can read `PREMIUM` content only while they hold an
**`ACTIVE`, non‑expired `PREMIUM` subscription** (latest subscription wins). Otherwise:
- list endpoints return the item but with `locked: true` for `PREMIUM` rows the user can't open,
- the detail endpoint (`GET /content/{id}`) returns `403` with `code`/`message` `INSUFFICIENT_TIER`.

`locked` is computed as `tier == PREMIUM && !hasActivePremium` on **both** `GET /content` and
`GET /me/favorites` — `FREE` rows are always `locked: false`, and a user with an active premium
subscription sees `locked: false` everywhere.

> **Fixed in `085f590`.** The earlier `GET /content` bug — where `locked` was set to *"does the user
> have active premium"* for every row, ignoring the row's tier (effectively inverted) — is resolved.
> The catalogue list now uses the same tier‑aware rule as the favorites list and the detail endpoint.

### Checkout is asynchronous
`POST /subscriptions/checkout` creates a `PENDING` subscription + `PENDING` payment and returns
immediately. A background worker resolves it after ~2 seconds:
- success → subscription `ACTIVE`, `startedAt`/`expiresAt` set (`expiresAt = now + plan.billingPeriod` days),
- `simulateFailure: true` → subscription `FAILED`.

The frontend should **poll** `GET /me/subscription` after checkout until `status` leaves `PENDING`.

---

## 2. Enums

```ts
export type ContentTier = 'FREE' | 'PREMIUM';

export type SubscriptionStatus =
  | 'PENDING'
  | 'ACTIVE'
  | 'FAILED'
  | 'CANCELED'
  | 'EXPIRED';

export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

export type UserRole = 'USER';
export type AdminRole = 'ADMIN';
```

---

## 3. TypeScript models

### 3.1 Shared / audit

All persisted entities carry these (via `Auditable`); response DTOs include them only where noted.

```ts
interface AuditFields {
  id: string;
  createdAt: string;  // ISO-8601
  updatedAt: string;  // ISO-8601 (not exposed in any current response DTO)
}
```

### 3.2 Domain entities (server-side shape — for reference, not returned raw)

```ts
// Tenant
interface Tenant {
  id: string;
  name: string;            // unique
  slug: string;            // unique, URL-safe key
  logoUrl: string;
  primaryColor: string | null;   // hex, e.g. "#1E3A8A"
  secondaryColor: string | null; // hex
  providerId: string | null;     // identity-provider key
  createdAt: string;
  updatedAt: string;
}

// AppUser
interface AppUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  externalId: string;      // id from the partner IdP, unique per tenant
  role: UserRole;
  tenantId: string;
  createdAt: string;
  updatedAt: string;
}

// Content
interface Content {
  id: string;
  title: string;
  excerpt: string | null;
  body: string | null;     // full article text
  category: string;        // free-text label, e.g. "Sport", "Actualités"
  tier: ContentTier;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

// Plan (tenant-scoped)
interface Plan {
  id: string;
  name: string;
  tier: ContentTier;
  price: number;           // minor units
  currency: string;
  billingPeriod: number;   // days
  tenantId: string;
}

// Subscription (user-scoped)
interface Subscription {
  id: string;
  status: SubscriptionStatus;
  startedAt: string | null;
  expiresAt: string | null;
  userId: string;
  planId: string;
}

// Payment (subscription-scoped, not exposed via any endpoint today)
interface Payment {
  id: string;
  amount: number;
  currency: string;
  status: PaymentStatus;
  provider: string;        // "test" in the mock flow
  externalRef: string | null;
  processedAt: string | null;
  subscriptionId: string;
}

// Admin
interface Admin {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: AdminRole;
}
```

### 3.3 Request DTOs

```ts
// POST /auth/login
interface LoginRequest {
  tenantSlug: string;   // required
  partnerToken: string; // required — Base64(JSON PartnerIdentity)
}

// POST /auth/mock-partner-token  (dev helper)
interface MockPartnerTokenRequest {
  tenantSlug: string;   // required
  externalId: string;   // required
  email: string;        // required, valid email
  firstName: string;    // required
  lastName: string;     // required
}

// POST /admin/auth/login
interface AdminLoginRequest {
  email: string;        // required, valid email
  password: string;     // required
}

// POST /me/favorites
interface AddFavoriteRequest {
  contentId: string;    // required
}

// POST /subscriptions/checkout
interface CheckoutRequest {
  planId: string;              // required
  simulateFailure?: boolean;   // default false — forces payment failure (testing)
}

// POST /admin/tenants  and  PUT /admin/tenants/{id}
interface AdminCreateTenantRequest {
  name: string;           // required
  slug: string;           // required
  logoUrl: string;        // required
  primaryColor: string;   // required (hex)
  secondaryColor: string; // required (hex)
  providerId: string;     // required
}
type AdminUpdateTenantRequest = AdminCreateTenantRequest; // same shape
// NOTE: on update, `slug` is validated for uniqueness but the persisted
// slug is NOT changed by the service; name/logo/colors/providerId are updated.

// POST /admin/tenants/{id}/plans
interface AdminCreatePlanRequest {
  name: string;           // required
  tier: ContentTier;      // required
  price: number;          // >= 0, minor units
  currency: string;       // required
  billingPeriod: number;  // > 0, days
}
```

Validation failures return **`400`** (see §5).

### 3.4 Response DTOs

```ts
// POST /auth/login
interface LoginResponse {
  token: string;
  userId: string;
  tenantId: string;
  role: UserRole;
}

// POST /auth/mock-partner-token
interface MockPartnerTokenResponse {
  partnerToken: string;
}

// POST /admin/auth/login
interface AdminLoginResponse {
  token: string;
  id: string;
  email: string;
  firstName: string;
  lastName: string;
}

// GET /tenants/{slug}/config   (public)
interface TenantPublicResponse {
  slug: string;
  name: string;
  logoUrl: string;
  primaryColor: string | null;
  secondaryColor: string | null;
  providerId: string | null;
}

// GET /admin/tenants  ·  GET /admin/tenants/{id}  ·  POST /admin/tenants  ·  PUT /admin/tenants/{id}
interface TenantAdminResponse {
  id: string;
  slug: string;
  name: string;
  logoUrl: string;
  primaryColor: string | null;
  secondaryColor: string | null;
  providerId: string | null;
  createdAt: string;
}

// GET /admin/tenants/{id}/users
interface AppUserResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: UserRole;
  createdAt: string;
}

// GET /content  ·  GET /me/favorites
interface ContentSummaryResponse {
  id: string;
  title: string;
  excerpt: string | null;
  category: string;
  tier: ContentTier;
  publishedAt: string | null;
  locked: boolean;    // true => user lacks the tier to open the detail view (tier == PREMIUM && !hasActivePremium)
  favorite: boolean;  // true => in the current user's favorites
}

// GET /content/{id}
interface ContentDetailResponse {
  id: string;
  title: string;
  excerpt: string | null;
  body: string | null;
  category: string;
  tier: ContentTier;
  publishedAt: string | null;
  favorite: boolean;  // true => in the current user's favorites
}

// GET /tenants/{slug}/plans  ·  GET /admin/tenants/{id}/plans  ·  POST /admin/tenants/{id}/plans
interface PlanResponse {
  id: string;
  name: string;
  tier: ContentTier;
  price: number;          // minor units
  currency: string;
  billingPeriod: number;  // days
}

// GET /me/subscription  ·  POST /subscriptions/checkout
interface SubscriptionResponse {
  id: string;
  planId: string;
  planName: string;
  planTier: ContentTier;
  status: SubscriptionStatus;
  startedAt: string | null;
  expiresAt: string | null;
}
```

---

## 4. Endpoints

Legend for **Auth**: `—` public · `USER` valid app‑user token · `ADMIN` valid admin token.

### 4.1 Auth

| Method | Path | Auth | Body | Response | Notes |
|--------|------|------|------|----------|-------|
| `POST` | `/api/v1/auth/login` | — | `LoginRequest` | `200 LoginResponse` | Auto‑provisions the user on first login. `404 NOT_FOUND` unknown tenant; `401 UNAUTHORIZED` bad/invalid partner token or provider mismatch. |
| `POST` | `/api/v1/auth/mock-partner-token` | — | `MockPartnerTokenRequest` | `200 MockPartnerTokenResponse` | Dev helper. `404 NOT_FOUND` unknown tenant. |
| `POST` | `/api/v1/admin/auth/login` | — | `AdminLoginRequest` | `200 AdminLoginResponse` | `401 UNAUTHORIZED` (`"Invalid Credentials"`). Seeded dev admin: `admin@kioskbridge.com` / `password`. |

### 4.2 Tenant (public)

| Method | Path | Auth | Response | Notes |
|--------|------|------|----------|-------|
| `GET` | `/api/v1/tenants/{slug}/config` | — | `200 TenantPublicResponse` | Branding/theme bootstrap. `404` unknown slug. |
| `GET` | `/api/v1/tenants/{slug}/plans` | — | `200 PlanResponse[]` | Public plan catalogue for a tenant. `404` unknown slug. |

### 4.3 Content (app user)

| Method | Path | Auth | Response | Notes |
|--------|------|------|----------|-------|
| `GET` | `/api/v1/content` | USER | `200 ContentSummaryResponse[]` | Full catalogue; `favorite` and `locked` computed per user (`locked` is tier‑aware as of `085f590`). Not tenant‑filtered today — content is global. |
| `GET` | `/api/v1/content/{id}` | USER | `200 ContentDetailResponse` | `404 NOT_FOUND`. `403 INSUFFICIENT_TIER` when the user can't access the tier. Includes `favorite`. |

### 4.4 Favorites (app user) — base `/api/v1/me/favorites`

| Method | Path | Auth | Body | Response | Notes |
|--------|------|------|------|----------|-------|
| `GET` | `/api/v1/me/favorites` | USER | — | `200 ContentSummaryResponse[]` | The user's favorited content. `favorite` is always `true`; `locked` is tier‑aware (same rule as `GET /content`). |
| `POST` | `/api/v1/me/favorites` | USER | `AddFavoriteRequest` | `201 ContentSummaryResponse` | Idempotent — re‑adding an existing favorite returns the existing one. `404 NOT_FOUND` (`"Content not found"`) for unknown content. |
| `DELETE` | `/api/v1/me/favorites/{contentId}` | USER | — | `204` | Remove a favorite. `404 NOT_FOUND` (`"Favorite not found"`) if the user has not favorited that content. *(Fixed in `3b50e15` — was previously misrouted as `POST /contentId`.)* |

### 4.5 Subscriptions (app user)

| Method | Path | Auth | Body | Response | Notes |
|--------|------|------|------|----------|-------|
| `GET` | `/api/v1/me/subscription` | USER | — | `200 SubscriptionResponse` **or `200` with empty body** | Latest subscription for the user; **null / empty body when the user has none** — handle both. |
| `POST` | `/api/v1/subscriptions/checkout` | USER | `CheckoutRequest` | `201 SubscriptionResponse` (`status: "PENDING"`) | Async resolution after ~2s. Poll `GET /me/subscription`. `404 NOT_FOUND` (`"Unknown plan !"`) when the plan doesn't exist **or** isn't in the user's tenant (same code for both). |

### 4.6 Admin — tenants  (base `/api/v1/admin/tenants`, all require `ADMIN`)

| Method | Path | Body | Response | Notes |
|--------|------|------|----------|-------|
| `GET` | `/api/v1/admin/tenants` | — | `200 TenantAdminResponse[]` | |
| `GET` | `/api/v1/admin/tenants/{id}` | — | `200 TenantAdminResponse` | Single tenant by id. `404 NOT_FOUND` (`"Tenant not found ! "`) for an unknown id. *(Added in `085f590`.)* |
| `POST` | `/api/v1/admin/tenants` | `AdminCreateTenantRequest` | `200 TenantAdminResponse` | `409 CONFLICT` if slug or name already exists. |
| `PUT` | `/api/v1/admin/tenants/{id}` | `AdminUpdateTenantRequest` | `200 TenantAdminResponse` | `404 NOT_FOUND` unknown tenant, `409 CONFLICT` slug conflict. See DTO note — slug is not actually changed. |
| `GET` | `/api/v1/admin/tenants/{id}/users` | — | `200 AppUserResponse[]` | Users belonging to that tenant. Empty array for an unknown tenant id (not `404`). |
| `GET` | `/api/v1/admin/tenants/{id}/plans` | — | `200 PlanResponse[]` | Empty array for an unknown tenant id (not `404`). |
| `POST` | `/api/v1/admin/tenants/{id}/plans` | `AdminCreatePlanRequest` | `201 PlanResponse` | `404 NOT_FOUND` unknown tenant. |

---

## 5. Errors

As of commit `2d85607` a global `@RestControllerAdvice` (`GlobalExceptionHandler`) unifies every error
that comes out of a **controller or service** into a single `ApiError` body:

```ts
interface ApiError {
  code: string;                    // stable machine-readable key — switch on this
  message: string;                 // human-readable, safe to show as a fallback
  status: number;                  // HTTP status, mirrors the response status
  path: string;                    // request URI
  timestamp: string;               // ISO-8601
  errors: FieldValidationError[];  // only populated for VALIDATION_ERROR, otherwise []
}

interface FieldValidationError {
  field: string;
  message: string;
}
```

### Codes emitted by the advice

| Status | `code` | When | `message` |
|--------|--------|------|-----------|
| `400` | `VALIDATION_ERROR` | Bean‑validation failure on a `@RequestBody` (missing/blank field, bad email, `price < 0`, `billingPeriod <= 0`). | `"Request validation failed"` — per‑field detail in `errors[]`. |
| `400` | `MALFORMED_REQUEST` | Body missing or not valid JSON. | `"Request body is missing or malformed"` |
| `403` | `INSUFFICIENT_TIER` | User lacks the tier for `GET /content/{id}`. | `"INSUFFICIENT_TIER"` |
| `404` | `NOT_FOUND` | Unknown tenant / content / plan / user / favorite. | Specific reason, e.g. `"Unknown tenant"`, `"Unknown plan !"`, `"Content not found"`, `"Favorite not found"`. |
| `409` | `CONFLICT` | Tenant `slug` or `name` already taken. | `"Tenant slug already exists ! "` / `"Tenant name already exists ! "` |
| `500` | `INTERNAL_ERROR` | Any uncaught exception. | `"An unexpected error occurred"` |

Rule for `ResponseStatusException`: if the reason string matches `^[A-Z][A-Z0-9_]{2,}$` it is used
verbatim as `code` (that's how `INSUFFICIENT_TIER` works); otherwise `code` is the status name
(`NOT_FOUND`, `CONFLICT`, …) and the reason becomes `message`.

### Errors that do **not** use this shape

The security layer short‑circuits the request before it reaches a controller, so these bypass the
advice and return the raw servlet error body (`{ timestamp, status, error, path }`, no `code`):

| Status | When |
|--------|------|
| `401` | Missing/expired/invalid/absent JWT. Emitted as `sendError(401, "Unauthorized")`. Bad login credentials, invalid partner token, and provider mismatch also surface as `401` but *do* go through the advice with `code: "UNAUTHORIZED"` (thrown as `ResponseStatusException` from the auth services). |
| `403` | Authenticated but wrong realm/role — e.g. a user token on `/admin/**`. Spring Security `AccessDeniedException` → `sendError(403)`. |

**Frontend guidance:** read `error.response.data.code` when it exists and branch on it; fall back to
`error.response.status` for the `401` / role‑`403` cases where `code` is absent. Always keep
`message` as a last‑resort display string.

---

## 6. Frontend integration notes

### Axios setup
```ts
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8000/api/v1',
});

api.interceptors.request.use((config) => {
  const token = /* user store */ localStorage.getItem('kiosk.userToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401) {
      /* clear token, redirect to login */
    }
    // Unified error body (§5): { code, message, status, path, timestamp, errors[] }.
    // `code` is absent on raw 401 / role-403 — fall back to status then.
    const code: string | undefined = error.response?.data?.code;
    const message: string = error.response?.data?.message ?? 'Something went wrong';
    return Promise.reject(Object.assign(error, { apiCode: code, apiMessage: message }));
  },
);
```

### Handling validation errors (`400 VALIDATION_ERROR`)
```ts
try {
  await api.post('/admin/tenants', payload);
} catch (e) {
  if (e.apiCode === 'VALIDATION_ERROR') {
    // e.response.data.errors: { field: string; message: string }[]
    for (const { field, message } of e.response.data.errors) setFieldError(field, message);
  }
}
```

### App‑user login flow (dev)
```ts
// 1. mint a mock partner token
const { data: mp } = await api.post<MockPartnerTokenResponse>('/auth/mock-partner-token', {
  tenantSlug: 'le-matin',
  externalId: 'le-matin-user-1',
  email: 'alice@lematin.example',
  firstName: 'Alice',
  lastName: 'Dupont',
});

// 2. exchange it for a JWT
const { data: session } = await api.post<LoginResponse>('/auth/login', {
  tenantSlug: 'le-matin',
  partnerToken: mp.partnerToken,
});
// store session.token, session.userId, session.tenantId
```
In production, step 1 is replaced by the real partner IdP handing your app a `partnerToken`.

### Theming
Call `GET /tenants/{slug}/config` before mounting the app; map `primaryColor` / `secondaryColor`
to CSS custom properties, use `logoUrl` in the header, `name` in the title.

### Checkout polling
```ts
const { data: pending } = await api.post<SubscriptionResponse>('/subscriptions/checkout', {
  planId,
});
// pending.status === 'PENDING'

async function pollSubscription(): Promise<SubscriptionResponse | null> {
  for (let i = 0; i < 10; i++) {
    await new Promise((r) => setTimeout(r, 1000));
    const { data } = await api.get<SubscriptionResponse | ''>('/me/subscription');
    if (data && data.status !== 'PENDING') return data;
  }
  return null; // still pending — show "processing" state
}
```

### `GET /me/subscription` empty body
When the user never subscribed, the endpoint returns `200` with an **empty response body**
(`data` is `''` / `null` depending on the HTTP client). Guard for it:
```ts
const { data } = await api.get('/me/subscription');
const subscription: SubscriptionResponse | null = data || null;
```

### `locked` content
`ContentSummaryResponse.locked === true` → show a paywall / upsell instead of navigating to the
detail route. If you navigate anyway, `GET /content/{id}` returns `403 INSUFFICIENT_TIER`.

`locked` is now tier‑aware on both `GET /content` and `GET /me/favorites` (fixed in `085f590`) —
it equals `item.tier === 'PREMIUM' && !userHasActivePremium`. You can trust it directly from either
list; `GET /content/{id}` → `403 INSUFFICIENT_TIER` remains the authoritative gate if you navigate
anyway.

### `favorite` flag
Both `ContentSummaryResponse` and `ContentDetailResponse` now carry `favorite: boolean`. Use it to
render the filled/empty bookmark toggle without a separate lookup. After `POST`/`DELETE
/me/favorites` you can flip it locally rather than refetching.

### Two token stores
Keep the app‑user token and the admin token in separate keys/stores. The admin token has no
`tenantId` claim and only works on `/admin/**`; the user token is rejected there with `403`.

### Money formatting
```ts
const format = (minor: number, currency: string) =>
  new Intl.NumberFormat('fr-FR', { style: 'currency', currency }).format(minor / 100);
```

### Token lifetime
JWT expiry is 24h (`JWT_EXPIRATION`, ms). There is no refresh endpoint yet — on `401`, send the
user back through login.

---

## 7. Gaps / things the backend does not expose yet

- No content **create/update/delete** API (content is seeded only).
- No **payments** endpoint — `Payment` is internal to the checkout flow.
- No subscription **cancel** endpoint (`CANCELED` / `EXPIRED` statuses exist but aren't reachable via API).
- No admin **content** or admin **user management** beyond listing.
- No pagination/filtering on list endpoints — everything is returned at once.
- `GET /content` is not tenant‑scoped; every tenant sees the same catalogue.
- `PUT /admin/tenants/{id}` silently ignores `slug` changes (validates uniqueness, persists the old value).
- `401` (missing JWT) and role‑`403` responses don't use the unified `ApiError` shape (§5).
- Admin sub‑resource lists (`/admin/tenants/{id}/users`, `/plans`) don't validate the tenant id — an unknown id returns `[]` rather than `404`. (`GET /admin/tenants/{id}` itself *does* `404`.)
