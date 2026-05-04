# 🔒 Security Setup Guide - Freelancer OS

## ⚠️ CRITICAL: Remove Already Tracked Sensitive Files

Since `application.properties` was already committed to Git history, you must remove it:

```bash
# Navigate to the backend directory
cd freelancer-os/freelancer-os

# Remove the file from Git history (but keep it locally)
git rm --cached src/main/resources/application.properties

# Verify it's no longer tracked
git status

# Commit the removal
git commit -m "security: Remove application.properties from version control"

# Push to remote
git push origin main
```

⚠️ **Note:** The file is still in the Git history. For production, consider using BFG or git-filter-branch to completely remove it from history.

---

## 🔑 Generate Secrets

### 1. Generate JWT Secret (Required)

```bash
# Generate a secure 32+ character secret
openssl rand -base64 32

# Output example:
# abc123def456ghi789jkl012mno345pqr678stu901vwx234yz+/AbCdEf==

# Copy this value - you'll need it for environment variables
```

### 2. Generate Strong Database Password

```bash
openssl rand -base64 20
```

---

## 📋 Environment Variables Setup

### Local Development (Linux/Mac)

Create a `.env` file in the backend root (NOT committed):

```bash
# Backend: freelancer-os/freelancer-os/.env
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=freelancer_os
export DB_USERNAME=postgres
export DB_PASSWORD=your_secure_db_password_here
export JWT_SECRET=your_generated_jwt_secret_here
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your_gmail_app_password_here
export SERVER_ADDRESS=127.0.0.1
export JPA_DDL_AUTO=validate
export SERVER_PORT=8081
```

Load the environment:
```bash
source .env
mvn spring-boot:run
```

### Windows (PowerShell)

```powershell
# Set environment variables
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "freelancer_os"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_secure_password"
$env:JWT_SECRET = "your_jwt_secret"
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your_gmail_app_password"
$env:SERVER_ADDRESS = "127.0.0.1"

# Run Spring Boot
mvn spring-boot:run
```

### Docker Environment

Create `docker-compose.yml`:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_USER: ${DB_USERNAME:-postgres}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      POSTGRES_DB: ${DB_NAME:-freelancer_os}
    ports:
      - "${DB_PORT:-5432}:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  freelancer-os:
    build: ./freelancer-os
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: ${DB_NAME:-freelancer_os}
      DB_USERNAME: ${DB_USERNAME:-postgres}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      MAIL_HOST: ${MAIL_HOST}
      MAIL_PORT: ${MAIL_PORT}
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      SERVER_ADDRESS: 0.0.0.0
    ports:
      - "8081:8081"
    depends_on:
      - postgres

volumes:
  postgres_data:
```

Create `.env.docker`:
```
DB_HOST=postgres
DB_PORT=5432
DB_NAME=freelancer_os
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your_gmail_app_password
```

Run:
```bash
docker-compose --env-file .env.docker up
```

---

## 📱 Gmail App Password (For Email)

1. Go to: https://myaccount.google.com/apppasswords
2. Select: Mail + Windows Computer (or your device)
3. Google will generate a 16-character password
4. Use this as `MAIL_PASSWORD` (NOT your Gmail password)

⚠️ **Never use your main Gmail password in the app**

---

## ☁️ Production Deployment

### AWS Secrets Manager

```bash
# Store secrets in AWS Secrets Manager
aws secretsmanager create-secret \
  --name freelancer-os/jwt-secret \
  --secret-string "your_jwt_secret"

aws secretsmanager create-secret \
  --name freelancer-os/db-password \
  --secret-string "your_db_password"
```

### Environment in application.properties

```properties
# Fetch from environment
app.jwt.secret=${JWT_SECRET:}
spring.datasource.password=${DB_PASSWORD:}
spring.mail.password=${MAIL_PASSWORD:}
```

### Deployment Platforms

**Heroku:**
```bash
heroku config:set JWT_SECRET=your_secret
heroku config:set DB_PASSWORD=your_password
```

**AWS Elastic Beanstalk:**
```bash
# Via .ebextensions/env.config
option_settings:
  aws:elasticbeanstalk:application:environment:
    JWT_SECRET: your_secret
    DB_PASSWORD: your_password
```

**Railway/Render/Fly.io:**
Set environment variables in the web dashboard

---

## 🔐 Security Checklist

- ✅ JWT secret is 32+ characters (alphanumeric + special chars)
- ✅ Database password is 15+ characters
- ✅ Application.properties is in `.gitignore`
- ✅ `.env` files are in `.gitignore`
- ✅ No secrets in code or comments
- ✅ BCrypt is used for password hashing (PasswordConfig)
- ✅ HTTP-only cookies for tokens (frontend configured)
- ✅ CORS is configured (check CorsConfig)
- ✅ Server binds to localhost in development
- ✅ HTTPS is enforced in production
- ✅ Sensitive files removed from Git history

---

## ⚡ Quick Start

```bash
# 1. Generate secrets
JWT_SECRET=$(openssl rand -base64 32)
DB_PASSWORD=$(openssl rand -base64 20)

# 2. Set environment variables
export DB_PASSWORD=$DB_PASSWORD
export JWT_SECRET=$JWT_SECRET
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"

# 3. Run the application
cd freelancer-os/freelancer-os
mvn spring-boot:run

# Backend runs at: http://localhost:8081
```

---

## 🧪 Testing

After setup, verify secrets are properly configured:

```bash
# Check if JWT secret is loaded
curl http://localhost:8081/api/auth/login \
  -X POST \
  -H "Content-Type: application/json"

# Should NOT return a 500 error about missing JWT_SECRET
```

---

## 📞 Troubleshooting

**Error: JWT_SECRET environment variable is not set**
```
Solution: Set the JWT_SECRET environment variable before running the app
```

**Error: Database connection refused**
```
Solution: Ensure PostgreSQL is running and credentials match
```

**Error: Failed to load mail configuration**
```
Solution: Check MAIL_USERNAME and MAIL_PASSWORD (use Gmail App Password, not main password)
```

---

## 📖 References

- [Spring Boot Environment Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [OWASP Secrets Management](https://owasp.org/www-project-top-10-client-side-security-risks/)
- [12 Factor App - Config](https://12factor.net/config)
