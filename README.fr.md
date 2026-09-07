# Kiosk Backend

*Lire en [anglais / English](README.md).*

Backend d'un **kiosque numérique / distribution de contenu** multi-tenant en marque blanche. Un
partenaire peut être toute organisation souhaitant proposer un kiosque de contenu à sa marque :
journaux et médias sportifs, mais aussi des entreprises, des opérateurs télécoms / fournisseurs de
services téléphoniques, des banques, ou n'importe quelle marque. Chaque partenaire dispose d'un
tenant à sa marque ; ses utilisateurs finaux se connectent via le fournisseur d'identité du
partenaire, parcourent un catalogue de contenus par palier
(`FREE` / `PREMIUM`), mettent des articles en favori et s'abonnent à une offre premium via un
paiement fictif asynchrone. Un domaine d'authentification **admin** distinct gère les tenants et
leurs offres.

- **Stack :** Java 21, Spring Boot 4.1, Spring Security (JWT sans état), Spring Data JPA / Hibernate,
  PostgreSQL, Flyway, Lombok, Maven.
- **URL de base (local) :** `http://localhost:8000`
- **Préfixe d'API :** toutes les routes sont sous `/api/v1`
- **Contrat d'API complet :** [`docs/api_guide.md`](docs/api_guide.md) — DTO de requête/réponse,
  modèles TypeScript, formats d'erreur et notes d'intégration frontend. Ce README en est un résumé ;
  ce fichier fait foi.

---

## Sommaire

- [Architecture et concepts clés](#architecture-et-concepts-clés)
- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Mise en place de la base de données](#mise-en-place-de-la-base-de-données)
- [Lancer l'application](#lancer-lapplication)
- [Données de développement injectées](#données-de-développement-injectées)
- [Compilation et tests](#compilation-et-tests)
- [Structure du projet](#structure-du-projet)
- [Référence de l'API](#référence-de-lapi)
- [Modèle d'erreur](#modèle-derreur)
- [Limitations connues](#limitations-connues)

---

## Architecture et concepts clés

### Tenants (partenaires en marque blanche)
La plateforme est multi-tenant. Un partenaire peut être toute organisation souhaitant exploiter un
kiosque de contenu à sa marque pour son audience — un média, une entreprise, un opérateur télécoms /
fournisseur de services téléphoniques, une banque, un distributeur, etc. Chaque partenaire est un
`Tenant` avec sa propre identité visuelle (`logoUrl`, `primaryColor`, `secondaryColor`) et une clé de
fournisseur d'identité (`providerId`).
Un frontend résout un tenant via son `slug` et applique le thème renvoyé par
`GET /api/v1/tenants/{slug}/config`.

### Deux domaines d'authentification — Utilisateurs vs Admins
| Domaine | Qui | Endpoint de connexion | Claims du token | Garde |
|---------|-----|----------------------|-----------------|-------|
| **Utilisateur** | Utilisateurs finaux d'un kiosque partenaire | `POST /api/v1/auth/login` | sujet `userId`, `tenantId`, `role=USER` | `ROLE_USER` |
| **Admin** | Opérateurs back-office | `POST /api/v1/admin/auth/login` | sujet `adminId`, `role=ADMIN` (pas de `tenantId`) | `ROLE_ADMIN` sur `/api/v1/admin/**` |

Les deux utilisent le même format JWT, envoyé dans l'en-tête `Authorization: Bearer <token>`.
L'expiration par défaut est de 24 h ; il n'y a pas encore d'endpoint de rafraîchissement.

### Handshake de connexion partenaire
La connexion partenaire réelle est déléguée à l'IdP du partenaire. Le backend attend un
**`partnerToken`** — un JSON `PartnerIdentity` encodé en Base64 (`providerId`, `externalId`, `email`,
`firstName`, `lastName`). À la première connexion, la ligne utilisateur est provisionnée
automatiquement pour ce tenant. En développement local, `POST /api/v1/auth/mock-partner-token` génère
un `partnerToken` valide sans IdP réel.

### Droits d'accès (entitlements)
Le contenu est réparti en paliers `FREE` / `PREMIUM`. Un utilisateur ne peut ouvrir un contenu
`PREMIUM` que s'il détient un abonnement `PREMIUM` `ACTIVE` et non expiré (le dernier abonnement
l'emporte). Sinon, l'endpoint de détail renvoie `403 INSUFFICIENT_TIER`.

### Paiement asynchrone
`POST /api/v1/subscriptions/checkout` crée un abonnement `PENDING` + un paiement `PENDING` et répond
immédiatement. Un worker en arrière-plan (un `ScheduledExecutorService`, voir
`config/PaymentExecutorConfig`) le résout après ~2 s :
- succès → abonnement `ACTIVE`, `expiresAt = now + plan.billingPeriod` jours ;
- `simulateFailure: true` dans la requête → abonnement `FAILED`.

Le client doit **interroger** (`polling`) `GET /api/v1/me/subscription` jusqu'à ce que `status` ne
soit plus `PENDING`.

### Montants et identifiants
Tous les montants monétaires sont des **entiers en unités mineures** (centimes) : `999` → `9,99`.
`currency` est une chaîne de code ISO. Tous les identifiants d'entité sont des chaînes UUID
(`VARCHAR(36)`). Les horodatages sont au format ISO-8601 (`Instant`).

---

## Prérequis

- **JDK 21** (`java -version` doit indiquer 21).
- **PostgreSQL 13+** en cours d'exécution et accessible.
- Aucun Maven local requis — utiliser le wrapper fourni (`./mvnw` / `mvnw.cmd`).

---

## Configuration

La configuration se trouve dans `src/main/resources/` :

- `application.yaml` — toujours actif. Nom de l'application et origines CORS autorisées.
- `application-dev.yaml` — actif uniquement sous le profil `dev`. Datasource, JPA/Flyway, JWT.

### Variables d'environnement

| Variable | Utilisée par | Valeur par défaut | Notes |
|----------|--------------|-------------------|-------|
| `DATABASE_URL` | dev | _(aucune — requise)_ | URL JDBC, ex. `jdbc:postgresql://localhost:5432/kiosk` |
| `DATABASE_USERNAME` | dev | _(aucune — requise)_ | |
| `DATABASE_PASSWORD` | dev | _(aucune — requise)_ | |
| `JWT_SECRET` | tous | `kiosk_secret` | Secret de signature HMAC — **à remplacer dans tout environnement partagé** |
| `JWT_EXPIRATION` | tous | `86400000` | Durée de vie du token d'accès, ms (24 h) |
| `JWT_REFRESH_EXPIRATION` | tous | `2592000000` | 30 jours, ms (réservé — pas de flux de rafraîchissement) |
| `CORS_ALLOWED_ORIGINS` | tous | `http://localhost:3000,http://localhost:5173` | Liste d'origines séparées par des virgules |

Les variables de datasource ne sont référencées que dans `application-dev.yaml` ; il faut donc
**lancer l'application avec le profil `dev`** (voir ci-dessous) pour qu'elle démarre avec une base de
données.

---

## Mise en place de la base de données

1. Créer une base de données :
   ```sql
   CREATE DATABASE kiosk;
   ```
2. Flyway gère le schéma. Les migrations dans `src/main/resources/db/migrations` (`V1__…` … `V9__…`)
   s'exécutent automatiquement au démarrage ; Hibernate est configuré en `ddl-auto: validate` et se
   contente de vérifier que le mapping correspond au schéma migré.

> **Note (Spring Boot 4) :** l'auto-configuration de Flyway nécessite la dépendance explicite
> `org.springframework.boot:spring-boot-flyway` (déjà présente dans `pom.xml`) — sans elle, Flyway ne
> fait rien silencieusement.

---

## Lancer l'application

Définir les variables d'environnement de datasource et activer le profil `dev`.

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

### Depuis un jar packagé
```bash
./mvnw clean package
SPRING_PROFILES_ACTIVE=dev \
DATABASE_URL=jdbc:postgresql://localhost:5432/kiosk \
DATABASE_USERNAME=postgres DATABASE_PASSWORD=postgres \
java -jar target/kiosk_backend-0.0.1-SNAPSHOT.jar
```

Le serveur écoute sur **`http://localhost:8000`**.

---

## Données de développement injectées

Sous le profil `dev`, `config/DevSeeder` remplit la base au premier démarrage (uniquement si elle est
vide) :

**Admin** (`POST /api/v1/admin/auth/login`)

| Email | Mot de passe |
|-------|--------------|
| `admin@kioskbridge.com` | `password` |

**Tenants :** `le-matin` (Le Matin), `radar-sport` (Radar Sport)

**Utilisateurs** (utiliser l'`externalId` avec `POST /api/v1/auth/mock-partner-token`)

| Tenant | externalId | Email |
|--------|-----------|-------|
| `le-matin` | `le-matin-user-1` | `alice@lematin.example` |
| `le-matin` | `le-matin-user-2` | `admin@lematin.example` |
| `radar-sport` | `radar-sport-user-1` | `bob@radar-sport.example` |

**Contenu :** 2 articles `FREE` + 2 articles `PREMIUM` (globaux, non liés à un tenant).

**Offres :** chaque tenant reçoit une offre `Essentiel` (`FREE`, 0) et une offre `Premium`
(`PREMIUM`, `999` EUR / 30 jours).

### Connexion de bout en bout rapide (dev)
```bash
# 1. générer un partner token fictif
curl -s localhost:8000/api/v1/auth/mock-partner-token -H 'Content-Type: application/json' -d '{
  "tenantSlug":"le-matin","externalId":"le-matin-user-1",
  "email":"alice@lematin.example","firstName":"Alice","lastName":"Dupont"
}'

# 2. l'échanger contre un JWT
curl -s localhost:8000/api/v1/auth/login -H 'Content-Type: application/json' -d '{
  "tenantSlug":"le-matin","partnerToken":"<partnerToken de l'\''étape 1>"
}'

# 3. appeler un endpoint authentifié
curl -s localhost:8000/api/v1/content -H 'Authorization: Bearer <token de l'\''étape 2>'
```

---

## Compilation et tests

```bash
./mvnw clean verify      # compilation + tests
./mvnw test              # tests uniquement
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Les tests se limitent pour l'instant au test de démarrage du contexte Spring
(`KioskApplicationTests`).

---

## Structure du projet

```
src/main/java/com/example/kiosk/
├── KioskApplication.java
├── admin/                  # entité admin + rôle
│   ├── auth/               # connexion admin (email/mot de passe → JWT)
│   └── tenants/            # gestion des tenants et des offres par l'admin
├── auth/
│   ├── auth/               # connexion utilisateur, mock partner token, PartnerIdentity
│   ├── jwt/                # JwtService, JwtAuthenticationFilter
│   └── user/               # entité AppUser, repository, service
├── common/                 # ApiError, ApiPaths, GlobalExceptionHandler, Auditable, TenantContext
├── config/                 # SecurityConfig, DevSeeder, JpaAuditingConfig, PaymentExecutorConfig
├── content/                # entité Content, endpoints du catalogue, paliers
├── entitlement/            # vérifications d'accès premium
├── favorite/               # favoris par utilisateur
├── subscription/
│   ├── payment/            # entité Payment (interne au checkout)
│   ├── plan/               # Plan lié à un tenant
│   └── subscription/       # entité Subscription, CheckoutService asynchrone
└── tenant/                 # entité Tenant + endpoints publics config/plans

src/main/resources/
├── application.yaml
├── application-dev.yaml
└── db/migrations/          # Flyway V1..V9
```

---

## Référence de l'API

Légende Auth : `—` public · `USER` token utilisateur · `ADMIN` token admin.
Toutes les routes sont préfixées par `/api/v1`. Voir [`docs/api_guide.md`](docs/api_guide.md) pour les
corps de requête, les formes de réponse et les codes d'erreur par endpoint.

### Auth
| Méthode | Route | Auth | Description |
|---------|-------|------|-------------|
| `POST` | `/auth/login` | — | Échange `{ tenantSlug, partnerToken }` contre un JWT utilisateur. Provisionne l'utilisateur à la première connexion. |
| `POST` | `/auth/mock-partner-token` | — | Utilitaire dev : génère un `partnerToken` pour un tenant. |
| `POST` | `/admin/auth/login` | — | Connexion admin `{ email, password }` → JWT admin. |

### Tenant (public)
| Méthode | Route | Auth | Description |
|---------|-------|------|-------------|
| `GET` | `/tenants/{slug}/config` | — | Amorçage de l'identité visuelle / du thème d'un tenant. |
| `GET` | `/tenants/{slug}/plans` | — | Catalogue public des offres d'un tenant. |

### Contenu (utilisateur)
| Méthode | Route | Auth | Description |
|---------|-------|------|-------------|
| `GET` | `/content` | USER | Catalogue complet ; `favorite` calculé par utilisateur. |
| `GET` | `/content/{id}` | USER | Détail d'un article. `403 INSUFFICIENT_TIER` si l'utilisateur n'a pas le palier requis. |

### Favoris (utilisateur)
| Méthode | Route | Auth | Description |
|---------|-------|------|-------------|
| `GET` | `/me/favorites` | USER | Contenus mis en favori par l'utilisateur. |
| `POST` | `/me/favorites` | USER | Ajoute un favori `{ contentId }`. Idempotent. `201`. |
| `DELETE` | `/me/favorites/{contentId}` | USER | Retire un favori. `204`. |

### Abonnements (utilisateur)
| Méthode | Route | Auth | Description |
|---------|-------|------|-------------|
| `GET` | `/me/subscription` | USER | Dernier abonnement de l'utilisateur ; `200` avec **corps vide** si aucun. |
| `POST` | `/subscriptions/checkout` | USER | Démarre le paiement `{ planId, simulateFailure? }`. Renvoie `201` `PENDING` ; résolu de façon asynchrone après ~2 s. |

### Admin — tenants (`ADMIN`)
| Méthode | Route | Description |
|---------|-------|-------------|
| `GET` | `/admin/tenants` | Liste tous les tenants. |
| `GET` | `/admin/tenants/{id}` | Un tenant par son id. `404` pour un id inconnu. |
| `POST` | `/admin/tenants` | Crée un tenant. `409` en cas de conflit de slug/nom. |
| `PUT` | `/admin/tenants/{id}` | Met à jour un tenant (note : les changements de `slug` sont ignorés par le service). |
| `GET` | `/admin/tenants/{id}/users` | Utilisateurs appartenant à un tenant. |
| `GET` | `/admin/tenants/{id}/plans` | Offres d'un tenant. |
| `POST` | `/admin/tenants/{id}/plans` | Crée une offre pour un tenant. `201`. |

---

## Modèle d'erreur

Les erreurs issues d'un contrôleur ou d'un service sont unifiées par `GlobalExceptionHandler` en un
corps `ApiError` :

```jsonc
{
  "code": "NOT_FOUND",              // clé stable exploitable par la machine — c'est là-dessus qu'il faut brancher
  "message": "Unknown tenant",       // message lisible, utilisable en repli
  "status": 404,
  "path": "/api/v1/tenants/foo/config",
  "timestamp": "2026-09-07T12:34:56.789Z",
  "errors": [ { "field": "email", "message": "must be a valid email" } ]  // uniquement pour VALIDATION_ERROR
}
```

| Statut | `code` | Quand |
|--------|--------|-------|
| `400` | `VALIDATION_ERROR` | Échec de validation Bean sur un corps de requête (détail dans `errors[]`). |
| `400` | `MALFORMED_REQUEST` | Corps absent ou JSON invalide. |
| `401` | `UNAUTHORIZED` | Mauvais identifiants / partner token invalide / provider non concordant. |
| `403` | `INSUFFICIENT_TIER` | L'utilisateur n'a pas le palier requis pour `GET /content/{id}`. |
| `404` | `NOT_FOUND` | Tenant / contenu / offre / utilisateur / favori inconnu. |
| `409` | `CONFLICT` | `slug` ou `name` de tenant déjà pris. |
| `500` | `INTERNAL_ERROR` | Toute exception non interceptée. |

**Exception :** la couche de sécurité court-circuite la requête avant qu'elle n'atteigne un
contrôleur ; un JWT absent/expiré (`401`) et un rejet pour mauvais domaine/rôle (`403`) renvoient
donc le corps d'erreur brut du servlet (`{ timestamp, status, error, path }`, sans `code`). Dans ces
cas, se rabattre sur le statut HTTP.

---