# **Next.JS & Spring Boot OAuth System**

A modern full-stack OAuth 2.0 authentication application built with Next.js 15 and Spring Boot 3. This system showcases secure OAuth integration with **GitHub** and **Microsoft Entra ID**, along with optional email/password authentication, protected routes, and seamless user authentication with JWT-based session management.

The application implements a dual-token authentication system with short-lived access tokens and long-lived refresh tokens, both stored as httpOnly cookies to prevent XSS attacks. MongoDB provides persistent storage for refresh tokens and invalidated access tokens, ensuring secure session management and proper token revocation. CORS is properly configured to enable secure cross-origin communication between the frontend and backend, whilst automatic token refresh mechanisms ensure uninterrupted user sessions without requiring re-authentication.

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
Secure token generation, validation, and lifecycle management:
- Dual-token system with access tokens (15 minutes by default) and refresh tokens (7 days by default)
- Automatic token generation upon successful authentication
- Token validation on protected endpoints
- Custom JWT claims with user information (ID, username, email, avatar)
- Token expiry handling and validation
- Automatic access token refresh using refresh tokens
- Refresh token rotation for enhanced security
- Persistent refresh token storage in MongoDB

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
- Java 17 or higher
- MongoDB 4.4 or higher
- OAuth Application credentials for one or both providers:
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

## Back-End
- [**Java**](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html): An object-oriented programming language with strong typing and extensive libraries.
- [**Spring Boot**](https://spring.io/projects/spring-boot): A framework for building production-ready applications with minimal configuration.
- [**Spring Security**](https://spring.io/projects/spring-security): Comprehensive security framework providing authentication and authorisation.
- [**Spring Security OAuth2 Client**](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html): OAuth 2.0 client implementation for Spring applications.
- [**Spring Data MongoDB**](https://spring.io/projects/spring-data-mongodb): Provides integration with MongoDB for data persistence.
- [**JJWT**](https://github.com/jwtk/jjwt): Java JWT library for creating and parsing JSON Web Tokens.
- [**Gradle**](https://gradle.org/): Build automation tool for dependency management and project building.

## Database
- [**MongoDB**](https://www.mongodb.com/): NoSQL database for storing refresh tokens and invalidated access tokens with TTL-based expiry.

# Design

## Token Storage Strategy
The application uses httpOnly cookies for token storage rather than localStorage. This approach prevents XSS attacks as JavaScript cannot access httpOnly cookies. Access tokens have a 15-minute lifespan whilst refresh tokens last 7 days by default. Both tokens are transmitted securely with the Secure flag in production.

## Database Architecture
MongoDB stores two collections:
- `refresh_tokens`: Stores valid refresh tokens with username, creation time, last used time, and expiry date
- `invalidated_access_tokens`: Stores invalidated access tokens until their natural expiry

Both collections use MongoDB's TTL indexes to automatically delete expired documents, eliminating the need for manual cleanup.

## JWT Token Structure
Access tokens contain user claims (ID, login, name, email, avatar URL) and a type field set to `access`. Refresh tokens contain minimal information with type set to "refresh". All tokens are signed using HMAC-SHA256 with a secret key.

## CORS Configuration
CORS is configured to accept requests from the frontend URL (default: `http://localhost:3000`) with credentials enabled. Allowed methods include GET, POST, PUT, DELETE, and OPTIONS. This enables secure cross-origin communication whilst preventing unauthorised access.

## Authentication Flow
1. User initiates OAuth login (GitHub, Microsoft Entra ID, or email/password)
2. Spring Security handles OAuth callback from the selected provider
3. Backend extracts user attributes using provider-agnostic logic
4. Backend generates access and refresh tokens with user claims
5. Tokens are set as httpOnly cookies
6. User is redirected to frontend dashboard
7. Subsequent requests include cookies automatically
8. JWT filter validates access tokens on protected endpoints

## Token Refresh Flow
1. Frontend detects expired access token (`401` response)
2. Frontend calls refresh endpoint with refresh token cookie
3. Backend validates refresh token from database
4. Backend generates new access token
5. New access token is set as httpOnly cookie
6. Original request is retried with new token

## Logout Flow
1. User initiates logout
2. Backend retrieves both tokens from cookies
3. Access token is added to invalidation list in MongoDB
4. Refresh token is deleted from database
5. Both cookies are deleted
6. User is redirected to login page

# Setting Up Project
These are the steps to run the full application locally.

## 1. Clone the Project Locally
```sh
git clone https://github.com/mbeps/oauth-springboot-nextjs.git
cd oauth-springboot-nextjs
```

## 2. Set Up MongoDB
Ensure MongoDB is running locally on `mongodb://localhost:27017` or configure your MongoDB connection string. The application will automatically create the required collections and indexes.

## 3. Create OAuth Applications (Optional)
You can configure one or both OAuth providers. If you only want to support one provider, simply remove the other provider's configuration block from `application.yaml`:

### GitHub OAuth Application
Create a GitHub OAuth application with the following settings:
- **Homepage URL**: `http://localhost:8080`
- **Authorisation callback URL**: `http://localhost:8080/login/oauth2/code/github`

Note your Client ID and Client Secret for the next step.

### Microsoft Entra ID App Registration
Create an Entra ID app registration with the following settings:
- **Redirect URI (SPA)**: `http://localhost:8080/login/oauth2/code/azure`
- **Supported account types**: Single tenant or multitenant as required
- **API permissions**: `openid`, `profile`, `email`, `offline_access`
- **Authentication**: Enable PKCE and implicit flow for SPA

Note your Application (client) ID, Client Secret (create one in Certificates & secrets), and Directory (tenant) ID.

**For Production**: Update redirect URIs to your production domain (e.g., `https://yourdomain.com` instead of `http://localhost:8080`).

## 4. Configure Backend
Navigate to the backend directory and copy the `example.application.yaml` file, renaming it to `application.yaml`:

```yaml
spring:
  application:
    name: oauth
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: GITHUB_CLIENT_ID_HERE
            client-secret: GITHUB_CLIENT_SECRET_HERE
            scope:
              - user:email
              - read:user
          azure:
            client-id: AZURE_CLIENT_ID_HERE
            client-secret: AZURE_CLIENT_SECRET_HERE
            scope:
              - openid
              - profile
              - email
              - offline_access
        provider:
          azure:
            issuer-uri: https://login.microsoftonline.com/TENANT_ID_HERE/v2.0
  data:
    mongodb:
      uri: MONGODB_URI_HERE
      # Alternative configuration:
      # host: localhost
      # port: 27017
      # database: oauth_app

server:
  port: 8080

# JWT Configuration
jwt:
  secret: JTW_SECRET_HERE_256
  expiration: 86400000  # 24 hours in milliseconds
  access-token-expiration: 900000  # 15 minutes in milliseconds
  refresh-token-expiration: 604800000  # 7 days in milliseconds

# Frontend URL for redirects
frontend:
  url: http://localhost:3000

cookie:
  secure: false  # Set to true in production (requires HTTPS)
  same-site: Lax  # Options: Strict, Lax, None

app:
  security:
    local-auth:
      enabled: true # Set to false to disable email/password login
    refresh-token:
      hashing-enabled: true
      rotation-enabled: true
```

### Configuration Parameters

`spring.security.oauth2.client.registration.github`:
- `client-id`: Your GitHub OAuth application Client ID obtained from GitHub Developer Settings
- `client-secret`: Your GitHub OAuth application Client Secret obtained from GitHub Developer Settings
- `scope`: OAuth scopes requesting access to user profile and email information

`spring.security.oauth2.client.registration.azure`:
- `client-id`: Your Microsoft Entra ID Application (client) ID from Azure Portal
- `client-secret`: Your Microsoft Entra ID Client Secret created in Certificates & secrets
- `scope`: OIDC scopes for user profile, email, and offline access (refresh tokens)

`spring.security.oauth2.client.provider.azure`:
- `issuer-uri`: Microsoft identity platform issuer URI containing your Tenant ID

`spring.data.mongodb`:
- `uri`: MongoDB connection string specifying the database location and name (e.g., `mongodb://localhost:27017/oauth_db`)

`jwt`:
- `secret`: Secret key for signing JWT tokens (minimum 32 characters for HS256 algorithm)
- `access-token-expiration`: Lifespan of access tokens in milliseconds (default: 900000 = 15 minutes)
- `refresh-token-expiration`: Lifespan of refresh tokens in milliseconds (default: 604800000 = 7 days)

`frontend`:
- `url`: The URL of your frontend application for CORS configuration and redirects (e.g., `http://localhost:3000`)

`cookie`:
- `secure`: Whether cookies should only be sent over HTTPS (set to `false` for local development, `true` for production)
- `same-site`: Cookie SameSite attribute for CSRF protection (use `Lax` or `Strict`)

`app.security.local-auth`:
- `enabled`: Enables or disables email/password authentication (set to `true` to enable)

`app.security.refresh-token`:
- `hashing-enabled`: Stores refresh tokens as SHA-256 hashes in MongoDB when `true` (recommended for production)
- `rotation-enabled`: Issues a brand new refresh token on every refresh request and revokes the old one when `true`

**For Production**: 
- Set `cookie.secure` to `true`
- Update `frontend.url` to your production frontend domain
- Use a strong, randomly generated `jwt.secret`
- Configure MongoDB with authentication and SSL/TLS

## 5. Configure Frontend
Navigate to the frontend directory, copy the `.env.example` file and rename it to `.env.local`:

```env
NEXT_PUBLIC_API_URL='http://localhost:8080'
NODE_ENV='development'
```

Configuration parameters:
- `NEXT_PUBLIC_API_URL`: Your backend API URL (default: `http://localhost:8080`)
- `NODE_ENV`: Environment setting (`development` or `production`)

## 6. Install Frontend Dependencies
```sh
cd frontend
npm install
```

## 7. Build the Backend
```sh
cd backend
./gradlew build
```

## 8. Run the Application

### Start the Backend
```sh
cd backend
./gradlew bootRun
```

The backend should now be running on `http://localhost:8080`

### Start the Frontend
In a new terminal:
```sh
cd frontend
npm run dev
```

Alternatively, you can build and start the frontend:
```sh
npm run build-start
```

The frontend should now be running on `http://localhost:3000`

# Usage

## Logging In

### OAuth Authentication
1. Navigate to the home page at `http://localhost:3000`
2. The application automatically fetches the list of enabled OAuth providers from the backend
3. Click either the **"Sign in with GitHub"** or **"Sign in with Microsoft"** button (or any other enabled provider)
4. Authorise the application with your chosen provider
5. You will be redirected to the dashboard upon successful authentication

### Email/Password Authentication
If enabled on the backend:
1. Navigate to the home page at `http://localhost:3000`
2. Use the email/password form to sign up or log in
3. You will be redirected to the dashboard upon successful authentication

## Accessing Protected Routes
The dashboard at `/dashboard` is a protected route. Attempting to access it without authentication will redirect you to the login page.

## Performing Protected Actions
On the dashboard, you can:
- View your OAuth provider profile information (GitHub, Microsoft, or local account)
- Access protected data from the backend
- Perform authenticated actions using the action buttons
- Log out to end your session

## Using Backend API Directly

### Get User Information
```http
GET /api/user
Cookie: jwt=<access_token>
```

### Get Protected Data
```http
GET /api/protected/data
Cookie: jwt=<access_token>
```

### Perform Action
```http
POST /api/protected/action
Cookie: jwt=<access_token>
Content-Type: application/json

{
  "action": "refresh_data"
}
```

### Token Refresh
When access token expires:
```http
POST /api/auth/refresh
Cookie: refresh_token=<refresh_token>
```

### Check Authentication Status
```http
GET /api/auth/status
Cookie: jwt=<access_token>
```

### Discover Available Providers
```http
GET /api/auth/providers
```

Returns available OAuth providers:
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

### Local Authentication Endpoints

**Signup**
```http
POST /api/auth/signup
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

**Login**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

### Logging Out
```http
POST /logout
Cookie: jwt=<access_token>; refresh_token=<refresh_token>
```

### Public Health Check
```http
GET /api/public/health
```

## Token Management
The application automatically handles token refresh. When your access token expires, the system will use your refresh token to obtain a new access token without requiring re-authentication.

# References
- [Next.js Documentation](https://nextjs.org/docs)
- [React.js Documentation](https://react.dev/reference/react)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Shadcn UI Documentation](https://ui.shadcn.com/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/documentation.html)
- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [GitHub OAuth Documentation](https://docs.github.com/en/apps/oauth-apps)
- [Microsoft Entra ID OAuth Documentation](https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-auth-code-flow)
- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [Spring Data MongoDB Documentation](https://docs.spring.io/spring-data/mongodb/reference/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)