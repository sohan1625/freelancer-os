# FreelancerOS Backend

Spring Boot API for freelancer, client, project, invoice, and expense management.

## Requirements

- Java 21
- PostgreSQL for the default profile

## Run locally

From the repository root:

```powershell
Set-Location freelancer-os
Copy-Item .env.example .env
```

For local PostgreSQL, start the server and create a `freelancer_os` database
in pgAdmin. Use `localhost` and port `5432`; the username and password are
the PostgreSQL account details from installation. For hosted PostgreSQL,
copy the host, port, database name, username, and password from your provider.

Generate a JWT secret with:

```powershell
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```

Fill `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, and `DB_PASSWORD` in
`.env`, and paste the generated value into `JWT_SECRET`. The example activates
H2; to use PostgreSQL, remove `SPRING_PROFILES_ACTIVE=dev`. Set
`JPA_DDL_AUTO=update` only for a new local database. On a hosting platform,
add these values as backend environment variables instead of committing `.env`.

```powershell
.\mvnw.cmd spring-boot:run
```

The API listens on `http://localhost:8081`. The `dev` profile uses local H2
instead; do not activate it when you want PostgreSQL.

Run tests with:

```powershell
.\mvnw.cmd test
```

Never commit `.env` or put production credentials in source control.
