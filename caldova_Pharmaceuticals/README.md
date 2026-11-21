# Caldova Pharmaceuticals - legacy production line demo

A fictional, local-only application for manual investigation of production lines. One Java 8 and Spring Boot 2.7.18 JAR serves Thymeleaf pages and reads Oracle Database. There is no separate frontend or API deployment.

The database contains four plants, 40 lines per plant, and two shifts per line per day. The fixed snapshot contains 124 Running, 24 Attention, and 12 Stopped lines. All names, signals, alarms, checks, and manual excerpts are fictional. Recorded values are not live equipment conditions or safety instructions.

## Prerequisites

- Docker Desktop running Linux containers, with Docker Compose.
- PowerShell 5.1 or newer. CI uses PowerShell 7 on Linux.
- Free local ports 8080 and 1521, and sufficient Docker memory for Oracle Free. Allocate at least 4 GB to the Docker VM.
- Internet access for the first image and dependency downloads.

The verified Oracle image, `gvenzl/oracle-free:23.26.0-slim`, supports Linux ARM64 and AMD64. It exposes database service `FREEPDB1`. A host Java installation is not needed for the container workflow.

The helper scripts read `java.version` from `pom.xml` and set `CALDOVA_JAVA_VERSION` for Compose. Builds and tests use `maven:3.9-eclipse-temurin-<version>`; the application uses `eclipse-temurin:<version>-jre`. Java `1.8` maps to `8`. The validation workflow keeps this setting across all steps. After a Java upgrade, set `CALDOVA_JAVA_VERSION` to the POM Java major version before running Docker Compose directly. Changing the host JDK alone does not change the container JDK.

This deliberately old application stack is for a local demonstration, not a new production system. Do not expose it to a shared network.

## Start

From this folder in PowerShell:

```powershell
.\scripts\start-demo.ps1
```

Open `http://127.0.0.1:8080/`. The script creates local credentials if needed, starts Oracle, applies the versioned schema and repeatable fictional dataset, builds the JAR with Java 8, and starts the application. It checks readiness before reporting success. The first run takes longer while images and dependencies download.

To run the same steps without the PowerShell helper scripts, follow the [manual startup commands](../README.md#start-manually-without-the-helper-scripts).

Application HTTP and the Oracle listener are published only on `127.0.0.1`. The application container listens on its container interface so Docker can forward the loopback-bound host port.

Sign in as `presenter`. Read `PRESENTER_PASSWORD` from the local ignored `.env` file. Setup stores a BCrypt password hash in the existing Oracle database, not a plaintext password. There is no public registration or employee account creation.

The legacy tabs provide:

- **Overview:** plant and line totals, pending maintenance, and shift staffing totals.
- **Production lines:** plant/status filters and line details with alarms, recorded signals, readiness checks, and plain-text manuals. Return links preserve filters.
- **Maintenance:** read-only scheduled work and history, filtered by plant, line, and status.
- **Shift rota:** fictional employee assignments, filtered by plant, date, and DAY/NIGHT shift.

The default rota date is `2026-11-19`. Data covers November 19-21, 2026, with 16 fictional employees and 48 assignments. DAY runs 06:00-18:00 UTC; NIGHT runs 18:00-06:00 UTC the next day. There are 12 maintenance records, three per plant. Dates outside the snapshot show an explicit empty result.

All operational pages require sign-in. Sessions expire after 20 minutes of inactivity. Sign-in and sign-out use CSRF-protected forms. The readiness endpoint remains public. No operational edit routes or equipment connections exist. Colors always have status labels.

## Local credentials and database ownership

The setup script generates four random credentials in the ignored `.env` file:

| Variable | Purpose |
|----------|---------|
| `ORACLE_PWD` | Oracle container bootstrap; mapped to the image's `ORACLE_PASSWORD` variable |
| `CALDOVA_OWNER_PASSWORD` | `CALDOVA_OWNER`, which owns the demo tables and is used for setup validation |
| `CALDOVA_READ_PASSWORD` | `CALDOVA_READ`, which has only session and table-read privileges |
| `PRESENTER_PASSWORD` | Browser sign-in for application username `presenter` |

Keep `.env` with the named `caldova-oracle-data` volume. Do not commit or share it. The bootstrap/setup account is not used for browser requests. Changing values in `.env` does not change existing database account passwords. If the volume exists but `.env` is missing, setup stops rather than inventing replacement credentials.

Application accounts are stored in `CALDOVA_OWNER.app_user`. The runtime account can read accounts but cannot change them. The app container does not receive `PRESENTER_PASSWORD`. Repeat setup verifies the existing hash and does not replace it. Changing this value in `.env` does not reset the presenter password; restore the original value if setup reports a mismatch. Password recovery is not included in this local demo.

For host execution, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. The default URL is `jdbc:oracle:thin:@localhost:1521/FREEPDB1`, and the username is `CALDOVA_READ`. The password has no default. Use a Java 8 JDK explicitly when building or running on the host.

## Validation

Run the Java 8 unit tests and real-Oracle functional tests:

```powershell
.\scripts\verify-demo.ps1
```

This runs Maven `verify` in the Java 8 container. It covers exact dataset counts, combined filters, navigation, empty results, invalid input, missing lines, escaped manual text, explicit database errors, and denied writes through the read account. It also covers Oracle-backed sign-in, disabled accounts, password hashes, protected pages, CSRF checks, sign-out, maintenance, and rota filters. Oracle is required; there is no H2 or in-memory fallback and no silently skipped database suite.

The pinned Maven Wrapper is also available for a host with Java 8. Host `.\mvnw.cmd verify` additionally requires the datasource variables above and `ORACLE_SETUP_USER=CALDOVA_OWNER` plus `ORACLE_SETUP_PASSWORD` for test-only owner access.

The [Build and test workflow](../.github/workflows/ci.yml) selects the JDK from `pom.xml`, prepares real Oracle and the presenter account, and supplies the database and test credentials before running Maven `verify` on the runner. It masks passwords, saves test reports, and stops the job's containers even if tests fail. No repository secrets are needed for this fictional test database.

Browser validation needs Node.js 22 or newer as **test tooling only**. It is not needed to run the application:

```powershell
Set-Location .\browser-tests
npm ci
npm test
```

On Windows, tests use the installed Microsoft Edge browser. On Linux, run `npx playwright install --with-deps chromium` before `npm test`; CI does this automatically. Keep the app running during browser tests. Tests read the presenter password from the local `.env` without printing it. They check sign-in and sign-out, tabs, maintenance and rota filters, line details, return navigation, keyboard use, visible focus, minimum 4.5:1 contrast for status text, HTTP error pages, and absence of external page requests.

To test a real database outage and persistence, return to this application's folder and run:

```powershell
.\scripts\check-persistence.ps1
```

This signs in, stops only the demo Oracle container, checks HTTP 503 for data pages and a fresh sign-in attempt, and checks DOWN readiness. It restarts Oracle and verifies sign-in, all 160 lines, 12 maintenance records, and 16 assignments on the default day. Do not run it at the same time as other tests or a presentation.

The **Build and test** workflow starts Oracle, applies the schema and fictional records, and creates the presenter account before running Maven `verify`. It uses the Java version from `pom.xml`, passes the required database and test credentials, and connects to Oracle through `127.0.0.1`. It stores test reports and stops its containers even if a test fails. Changes to the application or that workflow trigger this check.

The **Validate Caldova legacy demo** workflow runs the full checks for application changes, manual runs, and weekly scheduled validation. It also resets fictional data and checks record totals and sign-in without reapplying setup, so setup cannot hide an incomplete reset. Local checks do not prove that the hosted workflow has run.

## Repeat or stop the demo

Normal setup is repeatable and does not create duplicate records. To restore all fictional records:

```powershell
.\scripts\reset-demo.ps1 -ConfirmReset
```

Reset restores the 160 lines, maintenance history, employees, and rota. It preserves application accounts and password hashes. It does not delete a schema, volume, or unrelated container. Stop the presentation while resetting to avoid displaying a partially changed snapshot.

To stop the app and Oracle while preserving data:

```powershell
.\scripts\stop-demo.ps1
```

Use `start-demo.ps1` to start again. Do not run `docker compose down --volumes`; that removes persistent data.

## Errors and recovery

- **Port already allocated:** identify the listener on 8080 or 1521. Do not stop an unrelated process. Select different loopback-bound ports in the Compose file and update readiness/test URLs together.
- **Oracle not ready:** check `docker compose logs --tail 60 oracle`, Docker memory, and Linux-container mode. Setup reports failure if health does not become ready within its timeout.
- **Database unavailable:** pages and `/api/health` return 503. No substitute records are shown. Start the local Oracle container, then retry.
- **Missing local credentials:** restore the original `.env` for the existing demo volume. Do not replace it with sample passwords.
- **Invalid filter, date, or line ID:** the app returns 400; use the listed filters. A valid but absent line returns 404.
- **Sign-in rejected:** use username `presenter` and the original `PRESENTER_PASSWORD` in `.env`. Database connection failures return 503, not an incorrect-password message.
- **Old host Java:** use the container scripts or configure a Java 8 JDK. Compiling on Java 17 alone does not verify Java 8 execution.

Connection and SQL reads have bounded timeouts. Error pages contain a safe request reference, not SQL, stack traces, connection strings, or credentials.

## Scope

Included: legacy desktop browser interface, overview, production line filtering and details, read-only maintenance and employee rota, Oracle-backed presenter sign-in, manual investigation, and local test/debug commands.

Not included: public registration, employee-specific accounts, password recovery, operator edits, alarm acknowledgement, live monitoring, PLC connections, AI diagnosis, forecasting, automated shift scheduling, modernisation, PostgreSQL migration, or Azure deployment.

The [scenario README](../README.md) describes the wider modernisation story. The versioned SQL in `src/main/resources/db/oracle` is the source of truth for the demo schema and records.
