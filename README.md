# **Next.JS & Spring Boot OAuth System**

A modern full-stack OAuth 2.0 authentication application built with Next.js 15 and Spring Boot 4.0.3, architected as a three-service system: a standalone **Auth Service** (Spring Boot, port 8081) managing all OAuth2 flows and JWT signing, a stateless **Backend API** (Spring Boot, port 8080) verifying tokens only, and a **Next.js frontend** (port 3000). This system showcases secure OAuth integration with **GitHub** and **Microsoft Entra ID**, along with optional email/password authentication, protected routes, and seamless user authentication with RS256 asymmetric JWT signing.

The application implements a dual-token authentication system with short-lived access tokens and long-lived refresh tokens, both stored as httpOnly cookies to prevent XSS attacks. The auth service owns all identity logic—OAuth2 flows, RS256 JWT signing with an RSA private key, and MongoDB storage for token lifecycle management—whilst the backend API remains stateless, verifying JWTs only via the JWKS public key endpoint. CORS is properly configured to enable secure cross-origin communication across all services, whilst automatic token refresh mechanisms ensure uninterrupted user sessions without requiring re-authentication.

# Features

## Authentication and Authorisation
The application provides comprehensive OAuth 2.0 authentication with flexible provider support:
- **Dynamic OAuth Provider Support**: GitHub and/or Microsoft Entra ID (Azure AD) with runtime selection
- Providers are dynamically discovered from backend configuration and exposed via `/api/auth/providers` endpoint
- Frontend automatically detects enabled providers and renders login buttons accordingly
- **Email/Password Authentication**: Optional local authentication that can be enabled/disabled via configuration
- Provider-agnostic authentication with Spring Security OAuth2 Client
- Users can log out securely with complete token invalidation
- Automatic token refresh maintains session continuity
- Client-side authentication state management

## JWT Token Management
Secure token generation, validation, and lifecycle management with RS256 asymmetric signing:
- **RS256 RSA asymmetric signing**: Auth service signs tokens with an RSA private key; backend verifies using the public key fetched from the JWKS endpoint
- Only the auth service can sign valid tokens (holds the RSA private key); if the backend is compromised, no valid tokens can be created
- Dual-token system with access tokens (15 minutes by default) and refresh tokens (7 days by default)
- Automatic token generation upon successful authentication
- Token validation on protected endpoints
- Custom JWT claims with user information (ID, username, email, avatar) and token type identifier
- Token expiry handling and validation
- Automatic access token refresh using refresh tokens
- Refresh token rotation for enhanced security
- Persistent refresh token storage in MongoDB (auth service only)

## Protected Routes and Endpoints
Comprehensive route-level and API-level security:
- **Frontend Protected Routes**: Dashboard accessible only to authenticated users
- Middleware validates JWT tokens before granting access
- Automatic redirection to login for unauthorised access attempts
- **Backend Protected Endpoints**: User profile retrieval, protected data access, and authenticated action endpoints
- Request validation using JWT tokens
- Real-time feedback via toast notifications

## Token Invalidation and Session Management
Secure session termination and cleanup:
- Access token invalidation on logout
- Refresh token revocation from database
- Cookie deletion on logout
- Automatic cleanup of expired tokens via MongoDB TTL indexes

## User Profile Management
Authenticated users can view their profile information:
- Display OAuth provider profile details (GitHub or Microsoft)
- View user avatar and username
- Access user ID and email information

## Public Endpoints
Health check and discovery endpoints for monitoring:
- Public health check endpoint
- Authentication status verification
- Provider discovery endpoint
- No authentication required

# Requirements
These are the requirements needed to run the project:
- Node.js 22 LTS or higher
- Java 17 or higher (required for Spring Boot 4.0.3)
- MongoDB 4.4 or higher (required for the auth service only; backend API is stateless with no database)
- OAuth Application credentials for one or both providers (configured in the auth service):
  - **GitHub OAuth Application** (Client ID and Client Secret)
  - **Microsoft Entra ID App Registration** (Client ID, Client Secret, and Tenant ID)

# Stack
These are the main technologies used in this project:

## Front-End
- [**TypeScript**](https://www.typescriptlang.org/): A strongly typed superset of JavaScript that enhances code quality and developer productivity through static type checking.
- [**Next.js**](https://nextjs.org/): A React framework with App Router for building server-side rendered and statically generated web applications.
- [**React.js**](https://react.dev/): A JavaScript library for building user interfaces with component-based architecture.
- [**Tailwind CSS**](https://tailwindcss.com/): A utility-first CSS framework for rapidly building custom user interfaces.
- [**Shadcn UI**](https://ui.shadcn.com/): A collection of accessible and customisable React components built with Radix UI and Tailwind CSS.
- [**Axios**](https://axios-http.com/): A promise-based HTTP client for making API requests with interceptors for token management.

## Auth Service
- [**Java**](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html): An object-oriented programming language with strong typing and extensive libraries.
- [**Spring Boot**](https://spring.io/projects/spring-boot): A framework for building production-ready applications with minimal configuration. This project uses Spring Boot 4.0.3 with updated starters: `spring-boot-starter-webmvc` (replacing `spring-boot-starter-web`) and `spring-boot-starter-security-oauth2-client` (replacing `spring-boot-starter-oauth2-client`).
- [**Spring Security**](https://spring.io/projects/spring-security): Comprehensive security framework providing authentication and authorisation.
- [**Spring Security OAuth2 Client**](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html): OAuth 2.0 client implementation for handling provider callbacks and authorization.
- [**Spring Data MongoDB**](https://spring.io/projects/spring-data-mongodb): Provides integration with MongoDB for token persistence and user storage with Spring Boot 4.0.3 UUID representation support.
- [**JJWT**](https://github.com/jwtk/jjwt): Java JWT library for creating and parsing JSON Web Tokens with RS256 RSA signing.
- [**Gradle**](https://gradle.org/): Build automation tool for dependency management and project building.

## Backend API (Stateless)
- [**Java**](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html): An object-oriented programming language with strong typing and extensive libraries.
- [**Spring Boot**](https://spring.io/projects/spring-boot): A framework for building production-ready applications with minimal configuration. Uses `spring-boot-starter-webmvc` and `spring-boot-starter-security` (no OAuth2 client or database starters).
- [**Spring Security**](https://spring.io/projects/spring-security): Comprehensive security framework providing authentication and authorisation (verification only, no OAuth2 client).
- [**Spring RestClient**](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html): Modern HTTP client (Spring Boot 4.0 standard) for fetching the JWKS endpoint from the auth service, replacing deprecated RestTemplate.
- [**JJWT**](https://github.com/jwtk/jjwt): Java JWT library for parsing and validating JSON Web Tokens with JWKS public key verification.
- [**Gradle**](https://gradle.org/): Build automation tool for dependency management and project building.
- **Note**: No database; backend is stateless and verifies JWTs only via fetching the public key from the auth service's JWKS endpoint on startup.

## Database
- [**MongoDB**](https://www.mongodb.com/): NoSQL database for storing refresh tokens and invalidated access tokens with TTL-based expiry.

# Design

## Token Storage Strategy
The application uses httpOnly cookies for token storage rather than localStorage. This approach prevents XSS attacks as JavaScript cannot access httpOnly cookies. Access tokens have a 15-minute lifespan whilst refresh tokens last 7 days by default. Both tokens are transmitted securely with the Secure flag in production.

## Database Architecture
MongoDB is used exclusively by the auth service (backend API is stateless with no database). The auth service stores three collections:

- `users`: Local user credentials and profile information (used when local auth is enabled)
  - `id`: MongoDB ObjectId
  - `email`: Unique login identifier
  - `password`: BCrypt hash
  - `name`: Display name
  - `avatarUrl`: Optional profile picture URL
  - `roles`: User roles (e.g. ROLE_USER)

- `refresh_tokens`: Long-lived tokens supporting rotation and hashing
  - `id`: MongoDB ObjectId
  - `token`: SHA-256 hash of the token (when hashing is enabled)
  - `username`: Associated user email/login
  - `expiresAt`: Expiry timestamp with TTL index; document auto-deleted when reached
  - `createdAt`: Token creation timestamp
  - `lastUsed`: Timestamp of last refresh use

- `invalidated_access_tokens`: Blacklist of revoked but not yet naturally expired access tokens
  - `id`: MongoDB ObjectId
  - `token`: Raw JWT string (unique indexed)
  - `username`: User identifier for audit
  - `expiresAt`: Token's natural expiry with TTL index; document auto-removed at expiry
  - `invalidatedAt`: Logout/revocation timestamp
  - `reason`: Reason for invalidation (e.g. "logout")

All collections use MongoDB's TTL indexes on the `expiresAt` field to automatically delete expired documents, eliminating the need for manual cleanup. The backend API does not use MongoDB and remains completely stateless.

## JWT Token Structure
Access tokens contain user claims (ID, login, name, email, avatar URL) and a type field set to `access`. Refresh tokens contain minimal information with type set to `refresh`. 

**Signing**: Tokens are signed using **RS256 RSA asymmetric signing**:
- Auth service signs tokens with an RSA private key loaded from `keys/auth-private.pem` (PKCS8 PEM format)
- Backend verifies tokens using the RSA public key fetched from the auth service's `/.well-known/jwks.json` JWKS endpoint (RFC 7517)
- This asymmetric approach ensures that only the auth service can create valid tokens; the backend cannot forge tokens even if compromised
- Key rotation requires only auth service redeployment; backend caches the JWKS key at startup

## CORS Configuration
CORS is configured to accept requests from the frontend URL (default: `http://localhost:3000`) with credentials enabled. Allowed methods include GET, POST, PUT, DELETE, and OPTIONS. This enables secure cross-origin communication whilst preventing unauthorised access.

## Authentication Flow
The authentication flow involves the auth service (port 8081) handling all identity operations:

1. User initiates OAuth login or email/password authentication on the frontend (port 3000)
2. Frontend redirects to the **auth service** (port 8081) for OAuth providers, or calls the auth service login endpoint directly for email/password
3. For OAuth: Auth service's `CustomOAuth2AuthorizationRequestResolver` validates the `redirect_uri` parameter and encodes it into the OAuth2 state (Base64-encoded after the `:` separator)
4. Auth service redirects the browser to the selected OAuth provider; user approves the scope request
5. OAuth provider redirects back to auth service: `/login/oauth2/code/{registrationId}`
6. Auth service's `OAuth2AuthenticationSuccessHandler` extracts user attributes, generates **RS256-signed JWTs** (access token + refresh token), stores the hashed refresh token in MongoDB, sets both tokens as httpOnly cookies, decodes the `redirect_uri` from the OAuth2 state, and redirects the browser to `{redirectUrl}/dashboard`
7. For local auth: Auth service validates credentials, generates RS256-signed JWTs, stores refresh token, sets cookies, and redirects
8. Frontend's Next.js middleware validates the presence of the `jwt` cookie to protect the dashboard route
9. The `AuthContext` calls `/api/auth/status` against the auth service (port 8081) to hydrate UI with user information
10. Subsequent API calls to the backend (port 8080) automatically include the `jwt` cookie
11. Backend's `JwtAuthenticationFilter` verifies the access token using the public key fetched from auth service's JWKS endpoint at startup

## Token Refresh Flow
The token refresh flow is initiated by the frontend's API client when the backend (port 8080) returns a 401 response:

1. Frontend's API client makes a request to the backend (port 8080) for protected data
2. Backend verifies the access token using the public key from the auth service's JWKS endpoint; if expired or invalid, returns 401
3. Frontend's `apiClient` 401 interceptor detects the failure (with a guard to prevent retry loops)
4. Interceptor queues any concurrent requests and calls `authClient` to the **auth service** (port 8081): `POST /api/auth/refresh`
5. Auth service reads the `refresh_token` cookie, validates it against the stored hash in MongoDB, and checks it hasn't expired
6. Auth service generates a **new RS256-signed access token** and sets it as a fresh httpOnly cookie
7. If `rotationEnabled` is configured: Auth service deletes the old refresh token from MongoDB, generates a new refresh token, stores it hashed, and sets it as a new httpOnly cookie
8. Frontend's interceptor resolves the queued requests and replays them to the backend with the new access token
9. Subsequent requests succeed with the new access token
10. If refresh token is invalid or expired, a `auth:session-expired` event is dispatched and the user is redirected to the login page

## Logout Flow
The logout flow revokes both tokens and ends the user session:

1. User clicks the logout button on the frontend
2. Frontend calls `logout()` which posts to the **auth service** (port 8081): `POST /logout`
3. Auth service's logout handler extracts both the `jwt` (access token) and `refresh_token` from cookies
4. Auth service adds the access token to the `invalidated_access_tokens` collection in MongoDB with the current timestamp and expiry date
5. Auth service deletes the refresh token from the `refresh_tokens` collection in MongoDB
6. Auth service clears both cookies by setting them to `maxAge=0`
7. Auth service returns a success response
8. Frontend redirects the user to the login page (`/`)

# Setting Up Project
These are the steps to run the full application locally.

## 1. Clone the Project Locally
```sh
git clone https://github.com/mbeps/oauth-springboot-nextjs.git
cd oauth-springboot-nextjs
```

## 2. Set Up MongoDB
Ensure MongoDB is running locally on `mongodb://localhost:27018` (required by the auth service only). The auth service will automatically create the required collections and indexes. The backend API does not use MongoDB and requires no database configuration.

## 3. Create OAuth Applications (Optional)
OAuth applications are configured in the auth service (`authentication/application.yaml`), not the backend API. You can configure one or both OAuth providers. If you only want to support one provider, simply remove the other provider's configuration block from the auth service's `application.yaml`:

### GitHub OAuth Application
Create a GitHub OAuth application with the following settings:
- **Homepage URL**: `http://localhost:8081`
- **Authorisation callback URL**: `http://localhost:8081/login/oauth2/code/github`

Note your Client ID and Client Secret for the next step.

### Microsoft Entra ID App Registration
Create an Entra ID app registration with the following settings:
- **Redirect URI (SPA)**: `http://localhost:8081/login/oauth2/code/azure`
- **Supported account types**: Single tenant or multitenant as required
- **API permissions**: `openid`, `profile`, `email`, `offline_access`, `User.Read` (required for profile picture and user details)
- **Authentication**: Enable PKCE and implicit flow for SPA

Note your Application (client) ID, Client Secret (create one in Certificates & secrets), and Directory (tenant) ID.

**For Production**: Update redirect URIs to your production domain (e.g., `https://yourdomain.com` instead of `http://localhost:8081`).

## 4. Configure Auth Service
Navigate to the `authentication` directory and create or update the `application.yaml` file with your OAuth credentials and configuration. This example uses environment variable interpolation for sensitive values:

```yaml
spring:
  application:
    name: auth
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: ${GITHUB_CLIENT_ID:}
            client-secret: ${GITHUB_CLIENT_SECRET:}
            scope:
              - user:email
              - read:user
          azure:
            client-id: ${AZURE_CLIENT_ID:}
            client-secret: ${AZURE_CLIENT_SECRET:}
            scope:
              - openid
              - profile
              - email
              - offline_access
              - User.Read
        provider:
          azure:
            issuer-uri: https://login.microsoftonline.com/${AZURE_TENANT_ID:}/v2.0
  data:
    mongodb:
      uri: ${MONGO_URI:mongodb://localhost:27018/auth_db}
  mongodb:
    representation:
      uuid: JAVA_LEGACY  # Maintains compatibility with existing MongoDB data (Spring Boot 4.0 change)

server:
  port: ${SERVER_PORT:8081}

# JWT Configuration - RS256 RSA Asymmetric Signing
jwt:
  private-key-path: ${JWT_PRIVATE_KEY_PATH:keys/auth-private.pem}  # PKCS8 PEM RSA private key for signing
  public-key-path: ${JWT_PUBLIC_KEY_PATH:keys/auth-public.pem}    # X509 PEM RSA public key (served via JWKS endpoint)
  access-token-expiration: ${JWT_ACCESS_TOKEN_EXPIRATION:900000}    # 15 minutes in milliseconds
  refresh-token-expiration: ${JWT_REFRESH_TOKEN_EXPIRATION:604800000}  # 7 days in milliseconds

# Auth service allowed origins and redirect URIs
auth:
  allowed-origins:
    - ${AUTH_ALLOWED_ORIGIN_1:http://localhost:3000}
    - ${AUTH_ALLOWED_ORIGIN_2:http://localhost:8080}
  allowed-redirect-urls:
    - ${AUTH_ALLOWED_REDIRECT_1:http://localhost:3000}

# Cookie security settings
cookie:
  secure: ${COOKIE_SECURE:false}  # Set to true in production (requires HTTPS)
  same-site: ${COOKIE_SAME_SITE:Lax}  # Options: Strict, Lax, None

# Token security and rotation
app:
  security:
    local-auth:
      enabled: ${LOCAL_AUTH_ENABLED:true}  # Set to false to disable email/password login/signup
    refresh-token:
      hashing-enabled: ${REFRESH_TOKEN_HASHING:true}  # SHA-256 hash tokens before storing in MongoDB
      rotation-enabled: ${REFRESH_TOKEN_ROTATION:true}  # Issue new refresh token on each use and revoke old one
```

### Auth Service Configuration Parameters

`spring.security.oauth2.client.registration.github`:
- `client-id`: Your GitHub OAuth application Client ID from GitHub Developer Settings
- `client-secret`: Your GitHub OAuth application Client Secret
- `scope`: OAuth scopes requesting access to user profile and email

`spring.security.oauth2.client.registration.azure`:
- `client-id`: Your Microsoft Entra ID Application (client) ID from Azure Portal
- `client-secret`: Your Microsoft Entra ID Client Secret from Certificates & secrets
- `scope`: OIDC scopes for authentication (openid, profile, email, offline_access for refresh tokens)

`spring.security.oauth2.client.provider.azure`:
- `issuer-uri`: Microsoft identity platform issuer URI containing your Tenant ID for token validation

`spring.data.mongodb`:
- `uri`: MongoDB connection string pointing to auth service database (default: `mongodb://localhost:27018/auth_db`). **Note**: Auth service uses port 27018 in development; standard MongoDB port 27017 may be used in production if running on a separate MongoDB instance.

`jwt`:
- `private-key-path`: Path to RSA private key in PKCS8 PEM format (auth service uses this to sign all JWTs)
- `public-key-path`: Path to RSA public key in X509 PEM format (served via `/.well-known/jwks.json` for backend verification)
- `access-token-expiration`: Lifespan of short-lived access tokens in milliseconds (default: 900000 = 15 minutes)
- `refresh-token-expiration`: Lifespan of long-lived refresh tokens in milliseconds (default: 604800000 = 7 days)

`auth.allowed-origins`:
- CORS allowed origins list for requests from frontend and backend

`auth.allowed-redirect-urls`:
- Whitelist of post-authentication redirect URIs; OAuth2 state contains Base64-encoded user-supplied `redirect_uri` validated against this list

`cookie`:
- `secure`: Whether cookies require HTTPS (set to `false` for local development, `true` for production)
- `same-site`: SameSite attribute for CSRF protection

`app.security.local-auth.enabled`:
- Toggles email/password login/signup endpoints (set to `true` to enable, `false` for OAuth-only)

`app.security.refresh-token`:
- `hashing-enabled`: SHA-256 hash refresh tokens before storing in MongoDB (recommended for production)
- `rotation-enabled`: Issue a new refresh token and revoke the old one on each refresh use (recommended for enhanced security)

### Environment Variables & Configuration Pattern

the auth service supports Spring Boot's property placeholder syntax to inject environment variables. In `.env.local` or system environment variables, set these values:

```env
# OAuth Credentials
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret
AZURE_CLIENT_ID=your_azure_client_id
AZURE_CLIENT_SECRET=your_azure_client_secret
AZURE_TENANT_ID=your_azure_tenant_id

# Database
MONGO_URI=mongodb://localhost:27018/auth_db

# Server
SERVER_PORT=8081

# JWT Keys
JWT_PRIVATE_KEY_PATH=keys/auth-private.pem
JWT_PUBLIC_KEY_PATH=keys/auth-public.pem
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# CORS and Redirects
AUTH_ALLOWED_ORIGIN_1=http://localhost:3000
AUTH_ALLOWED_ORIGIN_2=http://localhost:8080
AUTH_ALLOWED_REDIRECT_1=http://localhost:3000

# Cookie Security
COOKIE_SECURE=false
COOKIE_SAME_SITE=Lax

# Features
LOCAL_AUTH_ENABLED=true
REFRESH_TOKEN_HASHING=true
REFRESH_TOKEN_ROTATION=true
```

The `application.yaml` uses `${ENV_VAR:default}` syntax to support both environment variables (for CI/CD and docker containers) and hardcoded defaults (for local development without a `.env.local` file).

### MongoDB UUID Representation (Spring Boot 4.0)

Spring Boot 4.0 changed the default MongoDB UUID representation from `JAVA_LEGACY` to `STANDARD`. To maintain compatibility with existing MongoDB data and avoid migration issues, the auth service configuration includes:

```yaml
spring:
  mongodb:
    representation:
      uuid: JAVA_LEGACY
```

This setting ensures that:
- Existing UUID fields in your MongoDB database remain readable and compatible
- No data migration is required when upgrading to Spring Boot 4.0
- New inserts also use the `JAVA_LEGACY` format for consistency

If you're starting a fresh database, you can optionally change this to `STANDARD` for Spring Boot 4.0 standards compliance, but `JAVA_LEGACY` is recommended for existing deployments.

## 5. Configure Backend API
Navigate to the `backend` directory and create or update the `application.yaml` file. The backend is **stateless** and requires no database or OAuth2 client configuration:

```yaml
spring:
  application:
    name: oauth

server:
  port: ${SERVER_PORT:8080}

# Auth service JWKS endpoint for JWT verification (fetched at startup)
auth:
  service:
    jwks-url: ${AUTH_SERVICE_JWKS_URL:http://localhost:8081}

# Frontend URL for CORS configuration
frontend:
  url: ${FRONTEND_URL:http://localhost:3000}
```

### Backend Environment Variables

The backend API accepts the following environment variables (all optional with sensible defaults):

```env
# Server
SERVER_PORT=8080

# Auth Service
AUTH_SERVICE_JWKS_URL=http://localhost:8081

# Frontend CORS
FRONTEND_URL=http://localhost:3000
```

The backend is a stateless REST API with no database. It verifies all incoming JWTs by fetching the public key from the auth service's JWKS endpoint at startup. If the auth service is unreachable at startup, the backend will fail with an `IllegalStateException`.

**Backend Stateless Architecture**:
- The backend has **no database** and is completely **stateless**
- Token verification relies entirely on RS256 public key fetched from auth service's JWKS endpoint
- Protected endpoints check the `type="access"` claim to reject malformed or refresh tokens
- Refresh token blacklist checks are NOT performed by the backend (no MongoDB access); only the auth service maintains the blacklist

**For Production** (Both Services):
- Generate RSA key pair (auth service): `openssl genpkey -algorithm RSA -out keys/auth-private.pem && openssl rsa -in keys/auth-private.pem -pubout -out keys/auth-public.pem`
- Set `COOKIE_SECURE=true` and `COOKIE_SAME_SITE=Strict` for HTTPS-only cookies
- Set `AUTH_ALLOWED_ORIGIN_1`, `AUTH_ALLOWED_ORIGIN_2`, and `AUTH_ALLOWED_REDIRECT_1` to your production domain(s)
- Configure MongoDB with authentication and SSL/TLS in `MONGO_URI`
- Configure `AUTH_SERVICE_JWKS_URL` to point to the production auth service
- Ensure Java 17+ and Spring Boot 4.0.3 compatible dependencies

## 6. Configure Frontend
Navigate to the `frontend` directory and create a `.env.local` file with the following environment variables. This file is Git-ignored and contains deployment-specific settings:

```env
NEXT_PUBLIC_AUTH_URL='http://localhost:8081'
NEXT_PUBLIC_API_URL='http://localhost:8080'
NODE_ENV='development'
```

Configuration parameters:
- `NEXT_PUBLIC_AUTH_URL`: Base URL of the auth service (default: `http://localhost:8081`). Used by `authClient` for authentication calls and OAuth2 redirect construction. Must be accessible from the browser.
- `NEXT_PUBLIC_API_URL`: Base URL of the backend API (default: `http://localhost:8080`). Used by `apiClient` for application data requests. Must be accessible from the browser.
- `NODE_ENV`: Environment setting (`development` or `production`) — controls Tailwind CSS tree-shaking and React warnings

**Frontend Notes**:
- The frontend uses **two separate Axios clients**:
  - `authClient`: Points to the auth service (8081) for authentication, token refresh, provider discovery, and OAuth2 flows
  - `apiClient`: Points to the backend API (8080) for application data; includes a 401 interceptor that delegates token refresh to `authClient`
- The `.env.local` file is Git-ignored for security and should never be committed to version control
- All `NEXT_PUBLIC_*` variables are exposed to the browser; do not include secrets here

## 7. Install Frontend Dependencies
```sh
cd frontend
npm install
```

## 8. Build the Services
```sh
# Build auth service
cd authentication
./gradlew build

# Build backend API
cd ../backend
./gradlew build
```

## 9. Run the Application

**IMPORTANT**: The services must be started in this specific order, as the backend depends on the auth service being available to fetch the JWKS endpoint at startup.

### Start Auth Service (First)
In a terminal:
```sh
cd authentication
./gradlew bootRun
```

The auth service should now be running on `http://localhost:8081`. Wait for it to fully start before proceeding.

### Start Backend API (Second)
In a new terminal:
```sh
cd backend
./gradlew bootRun
```

The backend will fetch the JWKS endpoint from the auth service at startup (`http://localhost:8081/.well-known/jwks.json`). **The auth service must be running before the backend starts**; if unreachable, the backend will fail fast with an `IllegalStateException`. Once running, the backend should be available on `http://localhost:8080`.

### Start Frontend (Third)
In another new terminal:
```sh
cd frontend
npm run dev
```

The frontend should now be running on `http://localhost:3000`

# Docker Deployment

This project includes complete Docker and Podman support for running all services in isolated containers. Docker streamlines local development, testing, and deployment by eliminating manual service setup and managing dependencies automatically.

## Prerequisites

Ensure you have one of the following installed:
- **Docker**: [Install Docker Desktop](https://www.docker.com/products/docker-desktop) (includes Docker Compose)
- **Podman**: [Install Podman](https://podman.io/docs/installation) with Podman Compose (`podman-compose`)

Verify installation:
```sh
docker --version  # or: podman --version
docker-compose --version  # or: podman-compose --version
```

## Environment Variables

Create a `.env` file in the project root with OAuth credentials and service configuration:

```env
# GitHub OAuth
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# Microsoft Entra ID OAuth
AZURE_CLIENT_ID=your_azure_client_id
AZURE_CLIENT_SECRET=your_azure_client_secret
AZURE_TENANT_ID=your_azure_tenant_id

# Auth Service
AUTH_PORT=8081
AUTH_ALLOWED_ORIGIN_1=http://localhost:3000
AUTH_ALLOWED_ORIGIN_2=http://localhost:8080
AUTH_ALLOWED_REDIRECT_1=http://localhost:3000

# Backend API
BACKEND_PORT=8080
FRONTEND_URL=http://localhost:3000

# Frontend
FRONTEND_PORT=3001
NEXT_PUBLIC_AUTH_URL=http://localhost:8081
NEXT_PUBLIC_API_URL=http://localhost:8080

# MongoDB
MONGO_PORT=27018
MONGO_DB=auth_db
```

## Starting All Services

To start MongoDB, auth service, backend API, and frontend with a single command:

```sh
docker-compose up --build
```

Services will be available at:
- **MongoDB**: `localhost:27018`
- **Auth Service**: `http://localhost:8081`
- **Backend API**: `http://localhost:8080`
- **Frontend**: `http://localhost:3001`

View logs from all services:
```sh
docker-compose logs -f
```

View logs from a specific service:
```sh
docker-compose logs -f auth  # or: backend, frontend, mongo
```

## Development Workflow (Database + Backend Only)

For frontend development without running the Next.js container, start only MongoDB and the backend API:

```sh
docker-compose up --build mongo auth backend
```

Then start the frontend locally in another terminal:
```sh
cd frontend
npm install
npm run dev
```

This approach:
- Uses containerised MongoDB and backend services
- Enables faster frontend development with HMR (hot module reloading)
- Simplifies troubleshooting of frontend code changes
- Still provides the full backend stack for testing

## Service Port Reference

| Service      | Port  | Container  | Access                      |
| ------------ | ----- | ---------- | --------------------------- |
| MongoDB      | 27018 | `mongo`    | `mongodb://localhost:27018` |
| Auth Service | 8081  | `auth`     | `http://localhost:8081`     |
| Backend API  | 8080  | `backend`  | `http://localhost:8080`     |
| Frontend     | 3001  | `frontend` | `http://localhost:3001`     |

**Note**: Frontend runs on port 3001 inside containers (vs. 3000 when running locally with `npm run dev`).

## Volume Management and Data Persistence

MongoDB data is persisted in a named Docker volume (`mongodb_data`) by default. This ensures data survives container restarts.

View volumes:
```sh
docker volume ls | grep oauth
```

Remove MongoDB data (WARNING: this deletes all stored data):
```sh
docker volume rm oauth-springboot-nextjs_mongodb_data
```

## Rebuilding Specific Services

Rebuild a single service after code changes:

```sh
# Rebuild auth service
docker-compose build auth

# Rebuild backend API
docker-compose build backend

# Rebuild frontend
docker-compose build frontend

# Rebuild all services
docker-compose build
```

Then start services:
```sh
docker-compose up
```

## Viewing Logs

Real-time logs for all services:
```sh
docker-compose logs -f
```

Logs for a specific service with timestamp:
```sh
docker-compose logs --timestamps auth
```

Last 50 lines from a service:
```sh
docker-compose logs --tail=50 backend
```

## Stopping and Cleaning Up

Stop all running services (containers remain):
```sh
docker-compose stop
```

Stop and remove all containers:
```sh
docker-compose down
```

Stop, remove containers, and delete volumes (WARNING: MongoDB data will be lost):
```sh
docker-compose down -v
```

## Podman Compatibility

This project supports rootless Podman with automatic user namespace mapping. To use Podman instead of Docker:

```sh
# Use podman-compose instead of docker-compose
podman-compose up --build

# All other commands remain the same
podman-compose logs -f
podman-compose down
```

Podman automatically handles:
- User namespace remapping (rootless containers run as your user)
- Volume permission mapping
- Port binding without root privileges

If you encounter permission issues, use `sudo podman-compose` or configure Podman to run rootless:
```sh
podman system migrate
```

# Usage

## Logging In

### OAuth Authentication
1. Navigate to the home page at `http://localhost:3000`
2. The application automatically fetches the list of enabled OAuth providers from the **auth service** (`http://localhost:8081/api/auth/providers`)
3. Click either the **"Sign in with GitHub"** or **"Sign in with Microsoft"** button (or any other enabled provider)
4. You will be redirected to the auth service to complete the OAuth flow with your chosen provider
5. After authorisation, the auth service redirects you to the dashboard on the frontend
6. You will be redirected to the dashboard upon successful authentication

### Email/Password Authentication
If enabled on the auth service:
1. Navigate to the home page at `http://localhost:3000`
2. Use the email/password form to sign up or log in
3. Authentication is handled by the **auth service** (`http://localhost:8081`)
4. You will be redirected to the dashboard upon successful authentication

## Accessing Protected Routes
The dashboard at `/dashboard` is a protected route. Attempting to access it without authentication will redirect you to the login page.

## Performing Protected Actions
On the dashboard, you can:
- View your OAuth provider profile information (GitHub, Microsoft, or local account)
- Access protected data from the backend API
- Perform authenticated actions using the action buttons
- Log out to end your session

## API Endpoints

### Auth Service Endpoints (Port 8081)

#### Token Refresh
When access token expires, the frontend automatically calls the **auth service** to obtain a new access token:
```http
POST http://localhost:8081/api/auth/refresh
Cookie: refresh_token=<refresh_token>
```

The auth service validates the refresh token, issues a new access token, and optionally rotates the refresh token. The frontend's `apiClient` interceptor handles this automatically.

#### Check Authentication Status
```http
GET http://localhost:8081/api/auth/status
Cookie: jwt=<access_token>
```

Returns the current authentication status and user information from the **auth service**.

#### Discover Available Providers
```http
GET http://localhost:8081/api/auth/providers
```

Returns available OAuth providers from the **auth service**:
```json
[
  {
    "key": "github",
    "name": "GitHub"
  },
  {
    "key": "azure",
    "name": "Microsoft Entra ID"
  },
  {
    "key": "local",
    "name": "Email & Password"
  }
]
```

#### Local Auth — Signup
```http
POST http://localhost:8081/api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

#### Local Auth — Login
```http
POST http://localhost:8081/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

#### Logout
```http
POST http://localhost:8081/logout
Cookie: jwt=<access_token>; refresh_token=<refresh_token>
```

### Backend API Endpoints (Port 8080)

All backend endpoints require the `jwt` access token cookie and return `401 Unauthorized` if the access token is missing or invalid.

#### Get User Information
```http
GET http://localhost:8080/api/user
Cookie: jwt=<access_token>
```

#### Get Protected Data
```http
GET http://localhost:8080/api/protected/data
Cookie: jwt=<access_token>
```

#### Perform Action
```http
POST http://localhost:8080/api/protected/action
Cookie: jwt=<access_token>
Content-Type: application/json

{
  "action": "refresh_data"
}
```

#### Public Health Check
```http
GET http://localhost:8080/api/public/health
```

## Token Management
The application automatically handles token refresh. When your access token expires, the frontend's `apiClient` interceptor calls the auth service to obtain a new access token without requiring re-authentication.

# Spring Boot 4.0 Migration Notes

This project has been upgraded to **Spring Boot 4.0.3**. If you're upgrading from Spring Boot 3.x, be aware of the following breaking changes:

## Updated Dependencies

The following Spring Boot starters have been renamed or replaced:

| Spring Boot 3.x                     | Spring Boot 4.0.3                            |
| ----------------------------------- | -------------------------------------------- |
| `spring-boot-starter-web`           | `spring-boot-starter-webmvc`                 |
| `spring-boot-starter-oauth2-client` | `spring-boot-starter-security-oauth2-client` |

These changes are reflected in the `build.gradle` files for both the auth service and backend API.

## Testing Annotations

In test files, update test annotations:
- `@MockBean` → `@MockitoBean` (when using Mockito)
- Ensure `spring-boot-starter-test` is included in `testImplementation`

## MongoDB UUID Representation

Spring Boot 4.0 changed the default UUID representation in MongoDB from `JAVA_LEGACY` to `STANDARD`. The auth service configuration includes `spring.mongodb.representation.uuid: JAVA_LEGACY` to maintain compatibility with existing data. See the "MongoDB UUID Representation (Spring Boot 4.0)" section in the configuration documentation above.

## Java Version Requirement

Spring Boot 4.0.3 requires **Java 17 or higher**. Ensure your `JAVA_HOME` environment variable points to Java 17+:

```sh
java -version
# openjdk version "17.x.x" or higher
```

# References
- [Next.js Documentation](https://nextjs.org/docs)
- [React.js Documentation](https://react.dev/reference/react)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Shadcn UI Documentation](https://ui.shadcn.com/)
- [Spring Boot 4.0 Documentation](https://docs.spring.io/spring-boot/documentation.html)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [GitHub OAuth Documentation](https://docs.github.com/en/apps/oauth-apps)
- [Microsoft Entra ID OAuth Documentation](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring Data MongoDB Documentation](https://docs.spring.io/spring-data/mongodb/reference/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)