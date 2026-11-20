# Caldova Pharmaceuticals: Legacy Production Line Demo

> Caldova Pharmaceuticals is a fictional company. This page describes the customer background and legacy demo. The operating figures are illustrative, not measured results.

## Run the application

1. Download and extract the repository. Install and start Docker Desktop with Linux containers. You also need PowerShell 5.1 or newer, free ports 8080 and 1521, and at least 4 GB of memory allocated to Docker. No separate Java or Oracle installation is needed.
2. Open PowerShell in the extracted repository folder and run:

   ```powershell
   Set-Location -LiteralPath '.\caldova_Pharmaceuticals'
   .\scripts\start-demo.ps1
   ```

3. Open `http://127.0.0.1:8080`. Sign in as `presenter` using `PRESENTER_PASSWORD` from the `.env` file created in the application folder.

The script starts Oracle, creates the schema and fictional records, builds the Java 8 application, and waits until it is ready. The first run needs internet access and takes longer to download images and dependencies.

To stop without deleting data, run `.\scripts\stop-demo.ps1` from the same application folder. Keep `.env` with the database volume; do not share or commit it. See the [run guide](./caldova%20Pharmaceuticals/README.md) for tests, reset commands, and error recovery. This old stack is a local demo only; do not expose it to a shared network.

### What the startup script does

`Set-Location` changes the current folder. `.\scripts\start-demo.ps1` then runs the startup script from that folder. It performs these steps:

1. **Prepare credentials:** create `.env` with random local passwords if needed. Preserve existing credentials and stop if an existing Oracle volume has lost its `.env`.
2. **Start Oracle:** start the database container and wait for its health check.
3. **Prepare the database:** run the versioned SQL files to create the owner/read accounts and tables, and insert or update the fixed fictional records without duplicates. The app receives read-only access.
4. **Prepare sign-in:** run a Java setup command in a temporary Java 8 container. It creates the `presenter` account with a BCrypt hash, or verifies the existing hash without changing it.
5. **Build and start the app:** Docker builds the Java 8 JAR and starts it. The script checks `/api/health` and reports success only when the app can read Oracle. Failed Docker commands or readiness checks stop startup.

The script runs these commands for you. It does not connect to equipment, deploy to Azure, or delete the database volume. Setup reapplies the fixed demo values, so it can overwrite manual changes to seeded records; it preserves existing account passwords.

### Start manually without the helper scripts

These PowerShell commands use the same Docker configuration but do not run the repository's PowerShell scripts. Run each step separately and continue only if it succeeds.

**1. Open the application folder.** Start Docker Desktop with Linux containers, then run this from the extracted repository folder:

```powershell
Set-Location -LiteralPath '.\caldova Pharmaceuticals'
docker info
```

`docker info` checks that the Docker engine is available.

**2. Prepare local credentials.** For a new installation with no `.env` or existing demo volume, copy the blank template:

```powershell
Copy-Item -LiteralPath '.env.example' -Destination '.env'
```

Edit `.env` locally and fill in all four variables: `ORACLE_PWD`, `CALDOVA_OWNER_PASSWORD`, `CALDOVA_READ_PASSWORD`, and `PRESENTER_PASSWORD`. Use four different passwords from a password manager. For compatibility with the scripts and SQL setup, use 24-64 letters/digits for each password, starting with a letter. Do not leave values blank or commit this file.

If `.env` already exists, keep it and skip the copy. If `caldova-oracle-data` exists but `.env` is missing, restore the original file; do not create replacement passwords or delete the volume. Compose reads `.env` automatically.

**3. Start Oracle and create the tables.**

```powershell
docker compose up -d --wait --wait-timeout 420 oracle
docker compose exec -T oracle sh -c 'sqlplus -s / as sysdba @/opt/caldova/sql/setup.sql "$CALDOVA_OWNER_PASSWORD" "$CALDOVA_READ_PASSWORD"'
```

The first command starts Oracle in the background (`-d`) and waits until it is healthy. The second runs SQLPlus inside that container. Password variables are expanded there, not typed into your command history. The mounted `setup.sql` creates the `CALDOVA_OWNER` schema, loads fictional records, and grants SELECT access to `CALDOVA_READ`. It does not delete existing records.

**4. Create or verify the presenter account.**

```powershell
docker compose run --rm --no-deps verify mvn -B -ntp compile exec:exec '-Dexec.executable=java' '-Dexec.args=-classpath %classpath com.caldova.production.setup.PresenterAccountSetup'
```

This uses the `verify` tools service to compile and run the account setup with Java 8; it does not run the test suite. It reads credentials from the container environment and stores only a BCrypt hash in Oracle. `--rm` removes the temporary container when finished; `--no-deps` uses the Oracle container you already started. Existing presenter passwords are verified, not replaced.

**5. Build, start, and check the application.**

```powershell
docker compose up -d --build --wait --wait-timeout 180 app
Invoke-RestMethod -Uri 'http://127.0.0.1:8080/api/health'
```

Docker builds the JAR and starts the app with `CALDOVA_READ`, not the schema owner. `--wait` checks container health; the HTTP check should report `UP` for the app and Oracle. Open `http://127.0.0.1:8080` and sign in as `presenter` using your `PRESENTER_PASSWORD`.

For later starts, keep `.env` and the database volume and repeat step 5; Compose also starts Oracle if needed. Stop both services without deleting data:

```powershell
docker compose stop app oracle
```

Do not use `docker compose down --volumes`: it deletes the stored demo database.

## Run a reviewed modernisation

The [Reviewed modernization workflow](.github/workflows/reviewed-modernization.yml)
is separate from **Continuous modernization**. It runs on Tuesdays at 10:00 UTC
or on demand from the default branch. The scheduled run uses `claude-sonnet-5`.
It plans an upgrade to the latest supported Java version while keeping Oracle
and preserving application behaviour.

**Continuous modernization** runs without a plan approval gate; a person reviews
the resulting pull request before merge. **Reviewed modernization** adds a human
check before the CLI changes application files. Its three jobs create the plan,
execute the approved plan, and publish the result. Plan approval does not replace
code review or passing tests.

**Set up the repository once:**

1. Check that the repository owner's GitHub plan supports environment required
   reviewers. On GitHub Free, Pro, and Team, this feature is available only for
   public repositories. Private or internal repositories require GitHub
   Enterprise. Environment secrets and branch restrictions can be available
   even when required reviewers are not. See
   [GitHub's required-reviewer rules](https://docs.github.com/en/actions/reference/workflows-and-actions/deployments-and-environments#required-reviewers).
2. In **Settings → Secrets and variables → Actions**, set the repository secret
   `GH_TOKEN` to a token authorised for Copilot. Do not grant repository write
   access to this token.
3. In **Settings → Environments**, open `modernization-review` if it exists;
   otherwise, create it. The repository owner or an administrator must configure
   it. Under deployment protection rules, select **Required reviewers**, add the
   presenter, and select **Save protection rules**. Leave **Prevent self-review**
   off for this demo. If it is on, another configured reviewer must approve a run
   you start. Disable administrator bypass and restrict the environment to the
   default branch. Add an environment secret named `MODERNIZE_EXECUTE_TOKEN`
   for Copilot access after approval. Enter its value directly in GitHub; do not
   grant it repository write access.
4. In **Settings → Actions → General**, allow GitHub Actions to create pull
   requests. The publish job uses the built-in GitHub token to push the result
   branch and open a draft pull request.

**If the environment exists but Required reviewers is missing:**

1. Keep the existing environment. A workflow can create it automatically without
   protection rules or secrets. You can configure it later. Deleting it also
   deletes `MODERNIZE_EXECUTE_TOKEN`; recreating it does not change feature
   availability.
2. Confirm that you are the repository owner or have administrator access.
   Check the repository visibility and the owner's GitHub plan against the
   required-reviewer rules above.
3. If your plan supports the feature, configure and save the required reviewer
   on the existing environment. Adding a secret alone does not require approval
   or create an approval record.
4. If your plan does not support the feature, use a supported plan or agree on a
   separate manual approval design before changing the workflow. That alternative
   is not implemented here. Do not make a private repository public unless its
   contents are approved for public release. Do not remove the approval check
   to make the run pass.
5. After setup, select **Run workflow** to start a new run, not **Re-run failed
   jobs**. The workflow rejects reruns and stops before CLI execution when no
   approval is recorded. A previous failed run cannot receive approval after
   the fact.

**Run and review:**

1. In **Actions → Reviewed modernization**, select **Run workflow**. Enter a
   goal and a model ID available to your Copilot account. Use
   `modernize help models` to find model IDs. Do not enter secrets in the goal
   or include sensitive data in the repository or reports.
2. The CLI assesses the application. It uses the assessment to create a plan.
   The workflow adds a download link to the run summary.
3. Download the assessment, `plan.md`, and `tasks.json`. Check the scope,
   target Java version, Oracle compatibility, and tests. The execution job waits
   for the configured reviewer. In the run, select **Review deployments**, select
   `modernization-review`, then select **Approve and deploy** only if the plan
   is acceptable. This button releases the execution job; it does not deploy
   the application to Azure. If you reject it or need a different plan, start
   a new run with **Run workflow**.
4. After approval, the CLI applies the saved plan to the same source commit.
   It does not create a new plan. If it changes application files, the workflow
   pushes a new branch and opens a draft pull request. The pull request links
   to the assessment and plan for 14 days. The workflow does not open an issue
   or merge the pull request.
5. The publish job starts [legacy demo validation](.github/workflows/validate-demo.yml)
   on the result branch. Review the diff and wait for the test results before
   merging. The request to start validation is not a passing test result.

## Customer background

Caldova Pharmaceuticals manufactures medicines for global markets. It operates four plants with a total of 160 production lines, each running two shifts per day.

Production teams depend on an on-premises application to monitor lines and investigate stops. The application has changed little in years. Several of its original engineers have left, and the remaining team has limited knowledge of the code and its design.

Caldova wants to reduce production delays, but it must first understand the application that its teams use today.

## Current application

The legacy app is one Java application with server-rendered Thymeleaf pages, an older intranet interface, and manual troubleshooting. It contains four fictional plants and 160 production lines, with two shifts per line each day.

| Area | Intended legacy setup |
| --- | --- |
| Hosting | Local Docker containers representing an on-premises system |
| Application | Java 8 and Spring Boot 2.7.18 |
| Database | Real Oracle Database Free; service `FREEPDB1` |
| Interface | Legacy tabs for overview, production lines, maintenance, and employee rota |
| Sign-in | One presenter account stored in Oracle; no public registration |
| Diagnostic information | Limited alarm history, equipment signals, readiness checks, and equipment manuals |

An operator can identify a line with a problem and open its details. However, the operator must review the available information manually to determine what to check next. The application does not provide AI-assisted diagnosis.

Oracle stores line records, alarms, maintenance history, employees, shift assignments, and the presenter's BCrypt password hash. The app reads these tables through a read-only database account. Records persist in a named Docker volume across restarts. If Oracle is unavailable, the app reports an error rather than showing substitute data.

After sign-in, use the Overview, Production lines, Maintenance, and Shift rota tabs. Operational records are read-only and are not live equipment data. The rota defaults to the fixed demo date `2026-11-19`; no equipment control, automatic scheduling, or AI is included.

## Business problem

Short, recurring stops create a substantial loss of production time across the plants. Information is available, but finding and interpreting it takes time. Gaps in application knowledge also make changes harder to deliver.

Oracle Database licensing costs and the cost of the current on-premises setup add pressure to change. Caldova wants to address these costs while making the application easier to maintain and extend.

### Illustrative production impact

| Assumption | Value |
| --- | --- |
| Plants | 4 |
| Production lines across all plants | 160 |
| Shifts per line per day | 2 |
| Stops per line per shift | 30 |
| Average stop duration | 70 seconds |

These assumptions give:

- **9,600 stops per day:** 160 lines x 2 shifts x 30 stops.
- **186.7 lost line-hours per day:** 9,600 stops x 70 seconds / 3,600.
- **93.3 recovered line-hours per day** if average stop duration falls from 70 to 35 seconds.

A line-hour is one hour of operating time on one production line. These figures add time across all lines; they do not represent elapsed time at a single plant. Recovered time is a potential benefit, not a demonstrated result or a guarantee of additional output.

## Future ambitions

Caldova's first priority is to modernise its application. This work should make the application easier to maintain and provide a foundation for future AI capabilities. The following goals describe the intended direction, not implemented features.

- **Modernise the application first:** Update the legacy application and reduce barriers to safe, incremental changes before adding new capabilities.
- **Migrate the database:** Move from on-premises Oracle Database to [Azure Database for PostgreSQL](https://learn.microsoft.com/en-us/azure/postgresql/flexible-server/overview), with the aim of reducing licensing costs and overall operating costs. Actual savings will depend on the target design, workload, and migration effort.
- **Make modernisation continuous:** Move from a once-a-decade rewrite to an ongoing part of the software development lifecycle (SDLC). Keep the application current through regular, manageable changes rather than waiting for another large migration project.

- **Faster troubleshooting:** Use AI models and agents to help teams investigate recurring stops and return lines to service sooner.
- **Better planning:** Explore AI-assisted inventory forecasting and shift scheduling.
- **Model choice:** Retain the option to use different models, including open-weight models that could be tuned on Caldova's reviewed incidents.
- **Infrastructure choice:** Support different hardware vendors and a mix of on-premises and cloud environments. Azure Kubernetes Service (AKS) is one deployment option, not a fixed requirement.
- **Data control:** Choose where models run and where data is stored and processed, subject to Caldova's regulatory requirements.

## Demo message: continuous modernisation

The legacy application is the starting point for a wider story: modernisation should be a continuous activity, not a project that happens once a decade.

Caldova should not have to wait for a large migration project to keep its application current. The demo will highlight the opportunity to make modernisation part of how teams **plan, build, review, ship, and operate** software every day. The initial application and database changes establish the foundation; regular improvements help keep it current.

## Demo scope

This repository is intended to demonstrate the legacy production line application only: its older interface, limited diagnostic information, and manual investigation workflow. It provides the starting point for explaining the modernisation opportunity.

The legacy application is implemented in [caldova Pharmaceuticals](./caldova%20Pharmaceuticals/). It uses a fixed fictional dataset and read-only investigation, maintenance, and employee rota flows. Presenter sign-in protects operational pages. Application modernisation, database migration, AI agents, model tuning, forecasting, automated shift scheduling, and cloud deployment remain future ambitions, not capabilities included in this legacy demo.

