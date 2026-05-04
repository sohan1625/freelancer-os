# ⚡ Quick Reference - Security Setup

## 30-Second Setup

```powershell
# 1. Generate secrets
openssl rand -base64 32  # Copy this - it's your JWT_SECRET
openssl rand -base64 20  # Copy this - it's your DB_PASSWORD

# 2. Set environment variables (Windows)
$env:JWT_SECRET = "paste_generated_secret_here"
$env:DB_PASSWORD = "paste_generated_password_here"
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your_gmail_app_password_16_chars"
$env:SERVER_ADDRESS = "127.0.0.1"

# 3. Run backend
cd freelancer-os/freelancer-os
mvn spring-boot:run

# 4. Backend runs at http://localhost:8081
```

---

## Environment Variables Required

| Variable | Required | Example | Where to Set |
|----------|----------|---------|------|
| `JWT_SECRET` | ✅ YES | `abc123def456...` (32+ chars) | `.env` or system |
| `DB_PASSWORD` | ✅ YES | `secure_password_123` | `.env` or system |
| `MAIL_USERNAME` | ✅ YES | `your-email@gmail.com` | `.env` or system |
| `MAIL_PASSWORD` | ✅ YES | `16char_app_password` | `.env` or system |
| `DB_HOST` | ❌ Optional | `localhost` | Default: localhost |
| `DB_PORT` | ❌ Optional | `5432` | Default: 5432 |
| `DB_NAME` | ❌ Optional | `freelancer_os` | Default: freelancer_os |
| `SERVER_ADDRESS` | ❌ Optional | `127.0.0.1` | Default: 127.0.0.1 |
| `SERVER_PORT` | ❌ Optional | `8081` | Default: 8081 |

---

## Common Issues & Fixes

### ❌ Error: "JWT_SECRET environment variable is not set"
```
✅ Solution: Set JWT_SECRET before running the app
$env:JWT_SECRET = "your_secret_here"
```

### ❌ Error: "JWT_SECRET must be at least 32 characters long"
```
✅ Solution: Generate a new secret
openssl rand -base64 32
```

### ❌ Error: "Cannot connect to database"
```
✅ Solution: Verify DB credentials and PostgreSQL is running
$env:DB_PASSWORD = "your_actual_password"
```

### ❌ Error: "Failed to send email"
```
✅ Solution: Use Gmail App Password (not main password)
https://myaccount.google.com/apppasswords
```

---

## Files You Need to Know

| File | Purpose | Committed | Edit Required |
|------|---------|-----------|--------|
| `application.properties.example` | Configuration template | ✅ Yes | Copy to `application.properties` |
| `.env.example` | Environment template | ✅ Yes | Copy to `.env` |
| `SECURITY_SETUP.md` | Detailed setup guide | ✅ Yes | Reference only |
| `.env` | Your local secrets | ❌ No (in .gitignore) | Create and fill |
| `application.properties` | Local configuration | ❌ No (in .gitignore) | Auto-generated from env vars |

---

## Verify It's Working

```bash
# 1. Check JWT secret is loaded (no error)
curl http://localhost:8081/api/auth/login

# 2. Try to register
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'

# Should return JSON response, not 500 error
```

---

## Important ⚠️

- ✅ `.env` files are in `.gitignore` - they WON'T be committed
- ✅ Never share `.env` file or environment variable values
- ✅ Never commit application.properties with real credentials
- ✅ Gmail App Password ≠ Gmail Password (use app password!)
- ✅ JWT Secret must be 32+ random characters
- ✅ All credentials are case-sensitive

---

## Docker Quick Start

```bash
docker run -e JWT_SECRET=your_secret \
           -e DB_PASSWORD=your_password \
           -e MAIL_USERNAME=email@gmail.com \
           -e MAIL_PASSWORD=app_password \
           -p 8081:8081 \
           freelancer-os:latest
```

---

## Production Deployment

Set these environment variables in your deployment platform:

**AWS:** Secrets Manager or EC2 Systems Manager  
**Heroku:** `heroku config:set KEY=value`  
**Docker:** Use `-e` flag or `.env` file  
**Kubernetes:** Create secrets and mount as env vars  
**GitHub Actions:** Set in repository secrets

---

## Reference Docs

- **Detailed Setup:** `SECURITY_SETUP.md`
- **Vulnerability Report:** `SECURITY_AUDIT_REPORT.md`
- **Implementation Summary:** `IMPLEMENTATION_SUMMARY.md`

**Last Updated:** May 4, 2026
