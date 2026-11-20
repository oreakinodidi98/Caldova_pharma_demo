# Project Plan

**Status**: Planning
**Created**: 2026-11-20
**Mode**: NEW

---

## 1. Project Overview

**Goal**: Build the fictional Caldova Pharmaceuticals legacy production line demo. Deliver one on-premises Java 8 and Spring Boot 2.7.18 monolith backed by Oracle Database, with a 2008-era browser interface and a read-only manual investigation workflow. The project is designed so that every module is independently testable.

**App Type**: Full-stack SSR

**API Login**: No

**Mode**: NEW

**Deployment Plan**: No deployment plan found. Run locally on the presenter's machine. Azure deployment is not part of this plan.

The confirmed answers in `requirements.json` are the source of truth. The requirements identify one service, `caldova-monolith`. Its server-rendered pages, controllers, business logic, and Oracle access belong to one executable JAR. Section 3 describes the browser interface within that same service, not a second application.

The demo contains four fictional plants and exactly 160 production lines, with 40 lines per plant and two shifts per day. Operators filter lines by plant and status, open a line, and review its alarm history, last recorded equipment signals, readiness checks, and fictional manual excerpts. All operational records come from Oracle. This is a fixed historical snapshot, not live monitoring or equipment control.

Out of scope: operator edits, saved notes, alarm acknowledgement, readiness updates, file uploads, PLC connections, AI diagnosis, production recommendations, application modernisation, PostgreSQL migration, cloud resources, and Azure deployment. The older stack is intentional and is not suitable for a new production system.

---

## 2. Caldova Monolith - Spring Boot 2.7.18

| Component | Technology |
|-----------|-----------|
| **Language** | Java 8 |
| **Runtime** | JVM |
| **Framework** | Spring Boot 2.7.18 |
| **Package Manager** | Maven; commit a pinned Maven Wrapper during scaffolding |
| **Test Runner** | JUnit 5 with Spring Boot Test; Maven Surefire and Failsafe |
| **Mocking Library** | Mockito, using the version managed by Spring Boot 2.7.18 |
| **Test Command** | `.\mvnw.cmd verify` on Windows; `./mvnw verify` on Linux |
| **Orchestration** | docker-compose |

Use Spring MVC, Thymeleaf, Bean Validation, and Spring JDBC with the Boot-managed connection pool. Use an Oracle `ojdbc8` release verified to support both Java 8 and the chosen Oracle server. Pin that version during scaffolding. Do not select a driver that needs Java 11 or later. Do not introduce a separate JavaScript application, REST service, or Node toolchain.

Keep controller, service, and repository responsibilities separate. Controllers validate query values and select views. Services apply filtering and map records into view models. Repositories use bound SQL parameters. Escape all text through Thymeleaf, including manual excerpts; never render stored HTML.

Use one normal Oracle query path for both the overview and line detail. Do not fall back to H2, in-memory lists, static JSON, or generated browser records when Oracle is unavailable. Use bounded connection and query timeouts. Close JDBC resources through Spring and release the connection pool on application shutdown.

Use versioned Oracle SQL scripts owned by this application. A local setup command applies the schema and deterministic fictional demo data before application startup. The setup command must use the same pinned scripts in automated tests. Run application reads with a dedicated account that has only `SELECT` access to demo tables; schema creation and dataset setup use separate credentials. Do not run destructive initialisation on normal application startup.

Data model:

| Entity | Key and relationships | Required content |
|--------|-----------------------|------------------|
| Plant | `plant_id`; unique `plant_code` | Name; exactly four records |
| Production line | `line_id`; `plant_id` foreign key; unique `line_code` | Name, process, equipment reference, status, snapshot time, two shifts per day |
| Alarm | `alarm_id`; `line_id` foreign key | Recorded timestamp, code, severity, short message; a small history per line |
| Equipment signal | `signal_id`; `line_id` foreign key | Signal name, recorded value, unit, recorded timestamp; no live update |
| Readiness check | `check_id`; `line_id` foreign key | Check name, recorded result, recorded timestamp; no editable checkbox |
| Manual excerpt | `excerpt_id`; `line_id` foreign key | Fictional document reference, title, revision, plain-text excerpt |

Keep identifiers compatible with Java 8 and Oracle. Use foreign keys, explicit column types, and status constraints. Avoid reserved identifiers and SQL syntax from a different database. Order overview rows by plant code then line code, and alarm history by timestamp then ID. Display all timestamps with an explicit UTC label.

The repeatable dataset contains 124 Running, 24 Attention, and 12 Stopped lines: each plant has 31 Running, six Attention, and three Stopped lines. These are illustrative snapshot counts, not measured operating results. Repeated setup must not duplicate records. An explicit local reset command requires confirmation, targets only the named demo schema, and restores the same records. It must not delete unrelated schemas or Docker volumes.

---

## 3. Frontend - Server-rendered Web Pages

| Component | Technology |
|-----------|-----------|
| **Framework** | Thymeleaf templates served by the monolith |
| **Rendering** | Server-rendered HTML with GET forms and normal links |
| **Styles** | Locally bundled Pico.css, with scoped legacy-theme overrides |
| **Browser behavior** | No JavaScript required for filters, navigation, or manual excerpts |
| **Validation** | Spring MVC HTML integration tests and a desktop browser keyboard check |

There is no independent frontend service, package manager, or deployment. Bundle all CSS with the JAR; no CDN or remote fonts. Use native HTML tables, labelled select controls, buttons, headings, and links. Keep filters in the query string so pages can be bookmarked.

The overview shows the complete filtered result, up to 160 rows, without pagination. Show the total result count and current filters. A line link preserves plant and status filters for the return link. Validate return-filter values rather than accepting an arbitrary redirect URL.

An empty result is a successful page with a clear message and a Clear filters link. An unknown line returns a 404 page. Invalid filters return a 400 page with a correction link. An Oracle failure returns a 503 page with a Retry link, not an empty table or a healthy status. Log a request reference and safe context; never display connection strings, SQL, stack traces, or credentials.

---

## 4. Services Required

| Azure Service | Role in App | Environment Variable | Default Value (Local) | Classification |
|---------------|------------|---------------------|----------------------|----------------|
| None - Oracle Database Free, local Linux container | Real Oracle datastore for the legacy demo; not an emulator or Azure service | `SPRING_DATASOURCE_URL` | `jdbc:oracle:thin:@localhost:1521/FREEPDB1` | Essential |
| None - Oracle read account | Read-only access for application queries | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Required local values; no password default | Essential |
| None - Oracle setup account | Local schema creation and repeatable dataset setup; never used for application requests | `ORACLE_SETUP_USER`, `ORACLE_SETUP_PASSWORD` | Required local values; no password default | Essential |
| None - Oracle container bootstrap | Initialise the local container according to its verified image contract | `ORACLE_PWD` | Required local value; no password default | Essential |

Use one named volume, `caldova-oracle-data`, for Oracle persistence. Select and pin the Oracle Free container image only after checking its published CPU architecture, environment variables, health check, and database service name. The detected host is Windows ARM64. Do not assume that a Linux AMD64 Oracle image will run on it. If an ARM64 image is unavailable, verify supported AMD64 emulation before proceeding. Any required change to a VM or existing Oracle instance needs user agreement.

Use Docker with `docker compose` as confirmed in the requirements. Bind application HTTP and the Oracle listener to loopback only, using ports 8080 and 1521 after checking that they are free. Do not expose the demo to a shared network without a separate access-control decision.

Read secrets from local environment variables or an ignored local configuration file. Commit only a template with variable names and blank secret values. Never include administrative credentials in the application connection pool or health response.

---

## 5. Prerequisites

Read-only detection ran on 2026-11-20. Installed tools are marked with a check. A question mark means the requirement is not confirmed, not that the tool is absent.

### Run

| Tool | Service(s) | Installed | Version |
|------|------------|-----------|---------|
| Java 8 JDK | caldova-monolith | ❓ | Java and javac 17.0.18 detected; Java 8 was not found in PATH or the checked JDK folders |
| Maven | caldova-monolith | ✅ | 3.9.15; currently uses Java 17.0.18 |
| Docker engine with Linux containers | caldova-monolith | ✅ | Client 29.9.3; reachable Linux engine 29.9.3 |
| Docker Compose | caldova-monolith | ✅ | v5.7.0 |
| Microsoft Edge | caldova-monolith | ✅ | 157.0.4341.60 |
| Oracle Free image compatible with this host | caldova-monolith | ❓ | Windows ARM64 host detected; image architecture and runtime compatibility not yet verified |

### Debug

| Tool | Service(s) | Installed | Version |
|------|------------|-----------|---------|
| Docker engine with Linux containers | caldova-monolith | ✅ | Reachable Linux engine 29.9.3 |
| Docker Compose | caldova-monolith | ✅ | v5.7.0 |
| Microsoft Edge | caldova-monolith | ✅ | 157.0.4341.60 |
| `redhat.java` | caldova-monolith | ❓ | Not confirmed in the checked VS Code extension folder |
| `vscjava.vscode-java-debug` | caldova-monolith | ❓ | Not confirmed in the checked VS Code extension folder |

Confirm all question-mark requirements before implementation or startup. Configure Maven, tests, and the application launch to use Java 8; compilation under Java 17 alone does not prove Java 8 runtime compatibility. A Java language-server extension may need a separate newer JDK. Keep its tooling runtime separate from the Java 8 application runtime.

Docker is a Run prerequisite because the selected local datastore runs in a container. Detection did not pull an image, start a container, change any runtime, or install software.

---

## 6. Design System & UI

**Component Library**: Pico.css

**Style Direction**: A restrained 2008-era pharmaceutical intranet dashboard. Use a cream page background, deep red navigation, white table panels, square corners, thin rules, compact spacing, muted amber warnings, and muted green healthy statuses. Do not introduce a modern SPA shell, oversized KPI cards, gradients, or decorative charts.

**Typography**: Arial, Helvetica, sans-serif

### Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| `primary` | `#721F2B` | Caldova navigation, line links, and filter action |
| `accent` | `#806022` | Attention text and warning accents |
| `surface` | `#F5F1E7` | Cream page background around white table panels |
| `text` | `#292521` | Line identifiers, manual excerpts, and table values |
| `muted` | `#625B52` | Snapshot labels, equipment metadata, and secondary text |
| `border` | `#C9C0B2` | Table rules, panel borders, and form control outlines |

Use white for table panels. Use `#356348` for Running text and a pale green badge background. Use `#806022` for Attention and a pale amber background. Use the deep red primary color for Stopped with a pale red background. Every status includes a text label. Verify text contrast and visible keyboard focus after theme overrides.

### Pages

| Page | Route | Purpose | Layout |
|------|-------|---------|--------|
| Production Lines | `/` | Filter 160 lines across four plants and open a line for manual investigation | `header + nav + main + form + table + footer` |
| Line Detail | `/lines/{lineId}` | Read a line's alarms, recorded signals, readiness checks, and fictional manual excerpts | `header + nav + action-bar + main + two-column(table+list) + footer` |

Keep the main pages populated in planning previews. Under a clearly marked Preview states disclosure, depict a loading placeholder, a no-matching-lines result, and a database-unavailable banner with a Retry link. These are separate examples, not simultaneous live faults. The real server-rendered application shows loading through normal browser navigation; it does not need artificial delays or a state-switcher control.

Use a skip link, one main heading, visible focus, explicit form labels, table captions, and scoped table headers. All routes, filters, and disclosure controls must work with a keyboard. At desktop width, detail uses two columns; at narrower widths, stack panels without hiding their content. Allow horizontal scrolling for tables when needed. Do not use disabled-looking controls for read-only check results; show recorded text.

### Sample Content

All names, measurements, alarms, and excerpts below are fictional. Use snapshot time `2026-11-19 14:20 UTC`. Plant names are Northbridge, Westhaven, Eastmere, and Southwick; each contains 40 lines.

Production Lines - representative rows:

| Line ID | Line code | Plant | Process | Status | Last recorded issue |
|---------|-----------|-------|---------|--------|---------------------|
| 1 | NB-001 | Northbridge | Tablet packaging | Stopped | ALM-204: Carton feed not detected |
| 2 | NB-002 | Northbridge | Blister packing | Running | No active alarm recorded |
| 41 | WH-001 | Westhaven | Bottle labelling | Attention | ALM-118: Label registration variation |
| 81 | EM-001 | Eastmere | Secondary packaging | Running | No active alarm recorded |
| 121 | SW-001 | Southwick | Case packing | Stopped | ALM-307: Outfeed accumulation |

For NB-001, show three alarm records:

| Recorded time (UTC) | Code | Severity | Message |
|---------------------|------|----------|---------|
| 2026-11-19 14:18:50 | ALM-204 | Stop | Carton feed not detected |
| 2026-11-19 14:18:12 | ALM-091 | Warning | Infeed spacing variation |
| 2026-11-19 13:47:03 | ALM-204 | Stop | Carton feed not detected |

Line Detail - NB-001 recorded signals:

| Signal | Value | Recorded time (UTC) |
|--------|-------|---------------------|
| Carton-present sensor | Not detected | 2026-11-19 14:18:50 |
| Infeed speed | 0 cartons/min | 2026-11-19 14:18:50 |
| Guard circuit | Closed | 2026-11-19 14:18:50 |

Line Detail - NB-001 readiness checks:

| Check | Recorded result | Recorded time (UTC) |
|-------|-----------------|---------------------|
| Material availability | Recorded ready | 2026-11-19 14:00:00 |
| Line clearance | Recorded complete | 2026-11-19 14:00:00 |
| Start permission | Not recorded | 2026-11-19 14:18:50 |

Line Detail - NB-001 fictional manual excerpts:

| Reference | Title | Revision | Excerpt |
|-----------|-------|----------|---------|
| CP-12 / 4.2 | Carton feed alarm | Demo A | ALM-204 records that the carton-present signal was not detected during the recorded feed cycle. |
| CP-12 / 2.1 | Signal history | Demo A | Signal values describe the last stored sample. They do not confirm the current equipment condition. |
| CP-12 / 1.1 | Demo limitations | Demo A | Fictional reference text for this software demonstration only. Not an equipment operating or safety procedure. |

Do not provide machine restart steps, bypass instructions, or claims that a recorded readiness check proves equipment safety. State that investigation is manual and that stored samples can be incomplete.

---

## 7. Project Structure

```text
caldova_pharma_demo\
  README.md
  pom.xml
  mvnw
  mvnw.cmd
  .mvn\wrapper\
  .env.example
  compose.yaml
  scripts\
    setup-demo.ps1
    reset-demo.ps1
  src\
    main\
      java\com\caldova\production\
        CaldovaApplication.java
        controller\
        service\
        repository\
        model\
      resources\
        application.yml
        db\oracle\
          V001__schema.sql
          V002__fictional_demo_data.sql
          reset-demo.sql
        templates\
          fragments\
          lines.html
          line-detail.html
          error\
        static\css\
          pico.min.css
          legacy.css
    test\
      java\com\caldova\production\
        service\
        integration\
      resources\
  .github\workflows\validate-demo.yml
  .azure\
    requirements.json
    project-plan.md
    .preview-temp\
```

This is the proposed structure, not an inventory of implemented files. Scaffolding must preserve the existing documentation and instructions. Do not create a second top-level application or commit local credentials.

---

## 8. Route Definitions

| # | Method | Path | Description | Request Body | Response Body | Status Codes |
|---|--------|------|-------------|--------------|---------------|--------------|
| 1 | GET | `/` | Overview; optional `plant` code and `status` enum filters | None | HTML with plant options, selected filters, result count, snapshot time, and line table or empty message | 200, 400, 503 |
| 2 | GET | `/lines/{lineId}` | Line details; optional validated plant/status return filters | None | HTML with line metadata, alarm history, recorded signals, readiness checks, manual excerpts, and return link | 200, 400, 404, 503 |
| 3 | GET | `/api/health` | Readiness check with a bounded Oracle query | None | JSON: `{"status":"UP","services":{"oracle":"UP"}}` or `{"status":"DOWN","services":{"oracle":"DOWN"}}` | 200, 503 |
| 4 | GET | `/css/{asset}` | Bundled static CSS | None | CSS | 200, 404 |

Allowed line statuses are `RUNNING`, `ATTENTION`, and `STOPPED`. Missing filters mean all plants or all statuses; empty values from the All options mean the same. Reject other values with 400. A valid filter combination with no matches returns 200. Malformed or nonpositive line IDs return 400; valid IDs that are not present return 404. Reject unsupported mutation methods. No user-data write routes are included.

---

## 9. Next Steps

1. Review and explicitly approve this plan before scaffolding. Verify the Java 8 runtime and Oracle container architecture prerequisites first.

2. After approval, hand off to **azure-project-scaffold**. Build one Maven application, its server-rendered pages, Oracle SQL scripts, local setup, and focused tests. Do not substitute Azure Functions, React, PostgreSQL, or H2. Treat the legacy Java framework as a custom stack; validate its compatibility rather than assuming it.

3. Use **azure-project-integrate** for real Oracle connectivity and complete read paths. The confirmed requirement for a repeatable fictional dataset requires demo data setup in addition to schema creation; do not omit it or replace it with browser mock data. Preserve read-only runtime access.

4. Validate on Java 8 against the selected real Oracle server. The Maven `verify` lifecycle must run both unit and Oracle-backed integration tests; a missing Oracle test environment must fail the functional validation job, not skip it. Tests must assert exactly four plants, 160 lines, 40 lines per plant, two shifts per line, and the agreed status totals. Exercise combined filters, filter preservation, empty results, representative line details, unknown IDs, invalid inputs, escaped manual text, database failure with 503, and denied writes through the application account. Check that repeated setup does not duplicate records and that the same dataset survives an Oracle container restart.

5. Add the functional validation job to `.github/workflows/validate-demo.yml` for changes, manual runs, and a scheduled run on a host that can run the pinned Oracle image. Use synthetic data only. Install or select Java 8 explicitly and apply the same SQL scripts used locally. Report the actual image, host architecture, runtime, commands, and outcomes; do not treat a skipped suite as a pass.

6. Run a desktop browser check for all filters, line navigation, back links, keyboard access, visible focus, text contrast, empty results, and error pages. Confirm that no network request reaches a PLC, cloud service, CDN, or remote font host.

7. Update the existing README with exact Java and Oracle prerequisites, setup/build/run/test commands, environment variables, snapshot limitations, safe repeat-demo reset steps, and recovery from Oracle startup failure. Preserve its distinction between the legacy demo and future ambitions.

8. Use **azure-debug-plan** and **azure-debug-generate** only for local Java debugging and the chosen Oracle container. Never auto-start Docker resources or change shared environment settings during planning. Check port availability and keep all listeners local.

9. Stop after the legacy demo is verified. Azure deployment, infrastructure generation, database migration, and AI features require a separate request and plan.
