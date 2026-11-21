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

The example `.env` uses H2 for local development. To use PostgreSQL instead,
remove `SPRING_PROFILES_ACTIVE=dev` and set `DB_HOST`, `DB_PORT`, `DB_NAME`,
`DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`. Use a JWT secret of at least
32 characters. For a new local database, set `JPA_DDL_AUTO=update`.

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
