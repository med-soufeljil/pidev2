# MailerSend setup (API key + sender identity)

This project sends offer emails via `EmailApiService`.

## 1) Add your API key

`EmailApiService` reads the key in this order:

1. Environment variable: `MAILERSEND_API_KEY`
2. JVM property: `-Dmailersend.api.key=...`

If both are missing, sending emails fails.

## 2) Configure sender email/domain (important for 422 errors)

If you get:

`422 ... from email domain must be verified in your account to send emails`

it means the sender domain/email is not verified in your MailerSend account.

### Required steps in MailerSend dashboard

1. Open **Domains** and add your domain.
2. Add/confirm DNS records (SPF/DKIM/Tracking).
3. Wait until domain status becomes **Verified**.
4. Create or confirm a sender mailbox under that domain (for example `hr@yourdomain.com`).

### Configure this app to use that sender

This project is now locked to this MailerSend sender domain:

- `test-3m5jgroevrxgdpyo.mlsender.net`

`EmailApiService` reads sender values in this order:

- `MAILERSEND_FROM_EMAIL` (or `-Dmailersend.from.email`)
- `MAILERSEND_FROM_NAME` (or `-Dmailersend.from.name`)

If you provide `MAILERSEND_FROM_EMAIL` with another domain, the app automatically rewrites it to the required domain above (keeping the local-part, for example `team@other.com` -> `team@test-3m5jgroevrxgdpyo.mlsender.net`).

If not set, defaults are:

- from email: `hr@test-3m5jgroevrxgdpyo.mlsender.net`
- from name: `RecruitFlow RH`

## 3) Local run examples

### Linux/macOS terminal

```bash
export MAILERSEND_API_KEY="your_token_here"
export MAILERSEND_FROM_EMAIL="hr@test-3m5jgroevrxgdpyo.mlsender.net"
export MAILERSEND_FROM_NAME="RecruitFlow RH"
mvn javafx:run
```

### Maven with JVM properties

```bash
mvn javafx:run \
  -Dmailersend.api.key="your_token_here" \
  -Dmailersend.from.email="hr@test-3m5jgroevrxgdpyo.mlsender.net" \
  -Dmailersend.from.name="RecruitFlow RH"
```

## 4) IntelliJ / VS Code

- **IntelliJ Run Configuration**:
  - Environment: `MAILERSEND_API_KEY`, `MAILERSEND_FROM_EMAIL`, `MAILERSEND_FROM_NAME`
  - OR VM options with `-Dmailersend.*`
- **VS Code launch.json**:
  - Add env vars in the Java launch configuration.

Keep tokens out of git history (`.env` files should not be committed).


## 5) Offer accept/reject links callback

Offer emails contain Accepter/Rejeter links that call a local callback server in this app.

- Default base URL: `http://localhost:8090`
- You can override with environment variable `OFFER_RESPONSE_BASE_URL`
  or JVM property `-Doffer.response.base.url=...`

For remote candidates, point this to a publicly reachable URL that forwards to your app callback endpoints:

- `/offer/accept?token=...`
- `/offer/reject?token=...`
