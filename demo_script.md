# Demo: continuously modernising an app

## Presenter run of show

**Story in one sentence:** Caldova has a useful but ageing production-line application; the demo shows how we can use Agentic AI development with github copilot to understand the application, set a starting point for a new engineer, go from idea to software and make modernisation a regular part of youd SDLC so your application are constantly evolving with technology your system depends and is built on .


i am a new engineer in the caldova dev eam, everyone is busy ahead of christmas just got hired

first go onto github projects to see the the issues, different view of what the team is working on , what i am assighned to etc,

overwhelming . but best place too start tis to understand the codebase . uses newb

### 1. Set the scene

**Action:** Open the project in the GitHub Copilot app. Show the the `caldova_Pharmaceuticals` folder. 

**Say:** “We will use the GitHub Copilot app to explore this repository and run the app. This was made from the design spec we got from our last call. 

**Action:** Crete a terminal. cd into enviroment using `cd C:\caldova_pharma_demo\caldova_Pharmaceuticals` run `pwsh .\scripts\start-demo.ps1`. open browser and go to the terminal 
enter password in env file

or 

```
$env:JAVA_HOME = 'C:\Users\oreakinodidi\AppData\Local\jdks\jdk-17.0.18'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn -version
docker compose stop app
docker compose up -d --wait --wait-timeout 420 oracle
$env:SPRING_DATASOURCE_URL = 'jdbc:oracle:thin:@localhost:1521/FREEPDB1'
$env:SPRING_DATASOURCE_USERNAME = 'CALDOVA_READ'
$env:SPRING_DATASOURCE_PASSWORD = (
    Get-Content -LiteralPath '.env' |
    Where-Object { $_ -match '^CALDOVA_READ_PASSWORD=' }
) -replace '^CALDOVA_READ_PASSWORD=', ''
mvn spring-boot:run
```

**Say:** Caldova is a fictional pharmaceutical manufacturer. It has four plants and 160 production lines. The current application gives operators a place to investigate line stops, but the process is manual and the system is difficult to change. Our goal today is to show both a path to modernise and keep it current.”

**Action:** In the GitHub Copilot app, ask Copilot to summarise the repository and identify the application folder, its runtime stack, and the available workflows. Check its answer against the files before repeating it.

**Say:** “Copilot helps us explore the repository, but we verify important details in the source files and workflow definitions. It is an assistant, not the source of truth.”

**Say:** “The application and Oracle run locally in Docker (instead of connect to an installed Oracle server or an Oracle instance in a VM.). The startup script prepares the fictional Oracle data, builds the Java 8 application, starts it, and checks that it is ready. The database uses persistent local storage, so a normal restart does not remove the demo data.”

**Show:** Overview, production-line filters, one line's alarms/signals/readiness/manual excerpts, maintenance, and the fictional shift rota.

**Say:** “This is a fixed, fictional snapshot. Operators investigate recorded information manually. There is no live equipment connection, AI diagnosis, or automated scheduling in this demo.”

### 3. Explain how the repository is organised

**Action:** Return to VS Code. Show `caldova_Pharmaceuticals\pom.xml`, then briefly show `src\main\java`, `src\main\resources\templates`, and `src\main\resources\db\oracle`.

**Say:** “This is one monolithic Java application. Spring Boot serves the server-rendered Thymeleaf pages, and the application reads the Oracle database through JDBC. The SQL files define the fictional demo schema and data. There is no separate frontend service.”


### 4. Explain the baseline

**Say:** “ The application works but to achieve all the future plans we discussed about from errorr diagnostic to using CCTV feeds an AI to fix line failores and building predictive models for worker shift We needto move beyond Java 8. Potential upgrade targets include **Java 11, 17 or 25**. OracleDB Free is a local development database for this proof of concept, not a production licensing recommendation.”

One way to achive this is i have have a **Continuous modernization** workflow that uses the CLI tool from GitHub Copilot app modernisation. The workflow downloads and installs the latest version of the CLI, then runs the modernisation step using the workflow input to guide the agentic workflow.

We can run it manually and provide an instruction in the form of upgrade targets, for example:

- Upgrade Log4j to the latest version.
- Upgrade to Spring Boot 4.
- Upgrade to a specific Java version.

also most importantly it has a schedule, so we can allow it to run weekley or on a regular bassis. This allows ups to inject modernisation into SDLC so the application is constantly evolving with technology your system depends and is built on

**Action:** Show `.github\workflows\ci.yml` and explain the intended baseline check. If it is correctly configured, manually run the **Build and test** workflow and open its logs.

**Say:** “Before changing an old application, we need a known starting point. CI workflow checks whether the current code builds and tests. The baseline lets us compare a future change with what worked before. This workflow will also Run 
for the generated baranch and PR for different modernisation targets as the modernisation workflow later triggers  ”

### 5. Show the Copilot environment setup

**Action:** Open `.github\workflows\copilot-setup-steps.yml`. Explain that this prepares the GitHub Copilot cloud-agent environment and checks the Java toolchains. Prepares the Copilot cloud agent environment with both legacy and modern JDKs, while keeping Java 8 as the default for the current app. 

**Say:** “The application stays on Java 8 for this legacy demo. The Copilot environment also has newer Java toolchains available, so future modernisation work can target a newer runtime without changing how we run the current app.”

### 6. Show continuous modernisation

**Action:** Open `.github\workflows\continuous-modernization.yml`. Point out the manual trigger and weekly schedule. In the Actions tab, select **Continuous modernization**, choose **Run workflow**, and enter a specific agreed target. Do not start a live run unless the required repository secrets and permissions are configured.

For the input, use either:

- `latest` for the CLI defaults.
- `Java 21` for a demo target.
- `Spring boot 3`
- `Java 11`
- `Spring boot 4`
- `Java 25`

**Say:** “Modernisation does not have to wait for a once-a-decade rewrite. This workflow can run on a schedule or on demand. The input tells the Modernize CLI what upgrade to consider. For this demo we will use upgrade log 4j to latest version. .”

**Action:** If the run completes, show its logs, generated branch, pull request, and the follow-up CI run. If it does not complete, show the configuration and explain what prerequisite is missing; do not imply that a PR was created.

**Say:** “The workflow proposes changes and opens a pull request from a new branch. The change is not merged automatically. Reviewers still need to inspect the diff and validation results before deciding whether to merge.”

##### What happens inside the modernise workflow

The workflow can run on a schedule or be triggered manually. Either way, it follows the same steps.

**1. Install and start the CLI**

- The workflow downloads the install script, which installs the Modernize CLI as a local binary.
- It then runs the CLI.
- In the **Run Modernize CLI** step, the log output looks similar to what you would see running the CLI yourself.
- The output shows the CLI version being used.

**2. Choose the model and set the input**

- You can select the model, as long as GitHub Models is accessible through your subscription.
- You can also set the input, for example the upgrade target.

**3. Prepare the upgrade**

In the preparing upgrade stage, the CLI works out what it is dealing with:

- It identifies that this is a single repository.
- It identifies the project directory.
- It identifies that the language is Java.

**4. Create the upgrade plan**

- The CLI uses the **create java upgrade plan** skill, which is part of the Modernize CLI. Behind it are several steps and prompts that execute the modernisation.
- Being in control of the plan means a human has to approve it. Because this workflow is fully automated, there is no approval gate: it accepts whatever plan comes up. This is a trade-off to point out in the demo, and the PR review at the end is where the human check happens.

**5. Run the upgrade and validate**

- The CLI continues until the upgrade is finished.
- It runs the tests and records the build result.

#### Step 5: Show the generated branch and PR

The workflow creates a branch like:

```text
modernize-upgrade-<run_id>-<attempt>
```

Then it opens a PR titled:

```text
chore: modernize application (Java 21)
```

Open the PR and review the changed files.

#### Step 6: Show CI running on the result branch

The modernisation workflow triggers `ci.yml` against the modernisation branch.

> The modernisation result is not merged automatically. It is validated through the normal build and test workflow, then reviewed as a PR.

### 7. Add a human check before execution

**Action:** After showing the autonomous **Continuous modernization** workflow,
open [Reviewed modernization](.github/workflows/reviewed-modernization.yml).
Show the `plan`, `execute`, and `publish` jobs, and the `modernization-review`
environment on the execution job.

**Say:** "The first workflow creates and applies a plan without waiting for a
person to approve that plan. We review its code and tests at the pull request.
Now we add a human check earlier: the agent assesses the application and creates
a plan, but it must wait for our approval before it changes application files.
We still review the code and test results before merge."

**Show:** The separate `modernize assess`, `modernize plan create`, and
`modernize plan execute` commands. The reviewed workflow installs CLI version
`1.0.95`. It runs on Tuesdays at 10:00 UTC or on demand from the default branch.
Its default goal upgrades Java while keeping Oracle and application behaviour.

#### Check the approval setup before the demo

**Action:** Open **Settings → Environments → modernization-review**. Complete
these checks before starting a live reviewed run:

1. Confirm that the GitHub plan supports **Required reviewers** for this
    repository. On GitHub Free, Pro, and Team, reviewers are available only for
    public repositories. Private or internal repositories require GitHub
    Enterprise. See
    [GitHub's required-reviewer rules](https://docs.github.com/en/actions/reference/workflows-and-actions/deployments-and-environments#required-reviewers).
2. Open the existing environment, or create it if absent. As the repository
    owner or an administrator, select **Required reviewers**, add the presenter,
    and select **Save protection rules**. Leave **Prevent self-review** off for
    this demo. If it is on, another configured reviewer must approve your run.
    Disable administrator bypass and restrict the environment to the default
    branch.
3. Confirm that the repository Actions secret `GH_TOKEN` and the environment
    secret `MODERNIZE_EXECUTE_TOKEN` provide Copilot access without repository
    write access. Enter secrets directly in GitHub; do not show their values.
4. In **Settings → Actions → General**, allow GitHub Actions to create pull
    requests. The publish job uses the built-in GitHub token.

**Say:** "GitHub may have created this environment when we first ran the
workflow. We can configure it now; we do not need to delete it. A stored token
does not create an approval requirement. The required-reviewer rule does."

**If Required reviewers is missing:** Keep the environment and its secret.
Check your administrator access, repository visibility, and the owner's GitHub
plan. Deleting and recreating the environment deletes its secrets and does not
unlock reviewer controls. If the plan does not support reviewers, do not start
a live execution demo. Explain that a supported plan or a separately agreed
manual approval design is needed. The alternative is not implemented here;
do not remove the approval check or make private code public to continue.
The [repository setup guide](README.md#run-a-reviewed-modernisation) has the
setup and recovery steps.

#### Run, review, and approve the plan

1. In **Actions → Reviewed modernization**, select **Run workflow** on the
    default branch. Select a model available to your Copilot account. For an
    example goal, enter `Upgrade to Java 21. Keep Oracle and preserve application behaviour.` or `migrate from spring boot 2 to spring boot 3`
    Do not enter secrets or sensitive data. Start a new run rather than using
    **Re-run failed jobs**; this workflow rejects reruns.
2. After the plan job completes, open the run summary and download the
    `modernization-review` artifact. Review the assessment, `plan.md`, and
    `tasks.json` against the source commit. Check scope, Java and Spring Boot
    compatibility, Oracle access, and the proposed tests. Files remain available
    for 14 days.
3. Show the execution job waiting for approval. In the run, select
    **Review deployments**, select `modernization-review`, then select
    **Approve and deploy** only if the plan is acceptable. Reject an unacceptable
    plan and start a new run with a revised goal.
4. After approval, show the execution log. The CLI applies the saved plan to
    the same source commit; it does not create a replacement plan. The workflow
    checks for a recorded approval before it runs the CLI.
5. If application files changed, show the `modernize-reviewed-<run_id>-<attempt>`
    branch and draft pull request. The publish job does not start
    [legacy demo validation](.github/workflows/validate-demo.yml). Before
    presenting, select **Run workflow** on that result branch, or use the manual
    command in the PR or job summary. Review the actual validation results.
    No application changes means no branch or pull request.

**Say:** "Approve and deploy is GitHub's button name. Here it allows the
modernisation job to run; it does not deploy the application to Azure. We approve
the proposed scope first. Then we review what the agent changed and check the
tests. Neither plan approval nor a request to start validation proves the code
is ready to merge. The workflow does not merge the pull request."

**If execution fails with `No approval was found`:** Check and save the
required-reviewer rule on the existing environment. If unavailable, resolve the
plan restriction before continuing. Start a fresh run with **Run workflow**,
review the new artifact, and approve the waiting job. Adding the execution
secret or changing settings cannot add approval to the previous failed run.
Show the failure accurately; do not imply that execution or publication passed.


**Demo Prompts**
Use a prompt as the workflow’s `goal` input.

| Category | Prompt |
|---|---|
| Framework upgrades | `upgrade to spring boot 3` |
| Framework upgrades | `upgrade to .NET 10` |
| Framework upgrades | `upgrade to Java 11` |
| Framework upgrades | `upgrade to JDK 21` |
| Framework upgrades | `migrate from spring boot 2 to spring boot 3` |
| Database migrations | `migrate from oracle to azure postgresql` |
| Database migrations | `migrate from SQL Server to azure cosmos db` |
| Database migrations | `switch from MySQL to azure database for mysql` |
| Cloud migrations | `migrate from on-premises to azure` |
| Cloud migrations | `containerize and deploy to azure container apps` |
| Cloud migrations | `migrate from rabbitmq to azure service bus` |
| Deployment | `deploy to azure app service` |
| Deployment | `deploy to azure kubernetes service` |
| Deployment | `set up CI/CD pipeline for azure` |

**Copilot Model IDs**
Use an ID as the workflow’s `model` input. These are the alternatives that work.

| Model | Model ID | Suggested Tasks |
|---|---|---|
| GPT-5.3-Codex | `gpt-5.3-codex` | Code changes and upgrades |
| Claude Sonnet 5.5 | `claude-sonnet-5.5` | General coding and modernization |
| Claude Opus 5.5 | `claude-opus-5.5` | Complex changes and planning |
| Claude Haiku 4.5 | `claude-haiku-4.5` | Small, focused changes |

Model access can vary by account and policy; check with `modernize help models`.

### run in worktree

Use the container script for the modernised app. Keep the manual commands in
stage 1 for the original app.

1. Stop the original host app with Ctrl+C. Open a new terminal in the selected
     PR worktree. Change to its `caldova_Pharmaceuticals` folder. Start Docker
     Desktop in Linux-container mode. Run only one demo branch at a time.

2. Copy the original credentials file into this folder. This replaces an empty
     or incomplete worktree file. It does not show the passwords.

```powershell
Copy-Item -LiteralPath 'C:\caldova_pharma_demo\caldova_Pharmaceuticals\.env' -Destination '.env'
```

Continue only if the copy completes. Git does not copy this ignored file into
a worktree. The four values must match the existing database accounts. Do not
use the blank template, enter new passwords, or delete the Oracle volume. Do
not share or commit the credentials file.

3. Run the startup script with PowerShell 7:

```powershell
pwsh .\scripts\start-demo.ps1
```

Use `pwsh`, not `powershell` or `.\scripts\start-demo.ps1` alone. The script
builds the app in Docker, prepares the fictional dataset, and checks app health.
For this Java 21 PR, the container images supply Java 21. No host JDK path is
needed.

Wait for `Caldova Pharmaceuticals is ready at http://127.0.0.1:8080`. Then open
that address and sign in as `presenter` with the original `PRESENTER_PASSWORD`.

or 

```
@"
ORACLE_PWD=
CALDOVA_OWNER_PASSWORD=
CALDOVA_READ_PASSWORD=
PRESENTER_PASSWORD=
"@ | Set-Content -Path .env
$env:JAVA_HOME = 'C:\Users\oreakinodidi\AppData\Local\jdks\jdk-25.0.2'
$env:JAVA_HOME = 'C:\Users\oreakinodidi\AppData\Local\jdks\jdk-21.0.13.1+2'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
mvn -version
docker compose stop app
docker compose up -d --wait --wait-timeout 420 oracle
$env:SPRING_DATASOURCE_URL = 'jdbc:oracle:thin:@localhost:1521/FREEPDB1'
$env:SPRING_DATASOURCE_USERNAME = 'CALDOVA_READ'
$env:SPRING_DATASOURCE_PASSWORD = (
    Get-Content -LiteralPath '.env' |
    Where-Object { $_ -match '^CALDOVA_READ_PASSWORD=' }
) -replace '^CALDOVA_READ_PASSWORD=', ''
mvn spring-boot:run
```


#### highlight to show to audence it modernised framework

say: The original project targets Java 8. This pull request changes that target to Java 21. The terminal shows that the application compiles and starts on Java 21. Spring Boot remains at version 2.7.18 in this upgrade. We will now check the application pages and database features

We can see the PRs that were created from our worklows and the checks (Build and Test for the different PRs)

If we have a failure , Modernisation ofc is not flawles but the beauty is we are in github copilot enviroment, so can use Fix with a copilot cloud agent to see how it will fix it for this PR

### 8. Close with the point of the demo

**Action:** Return to the app or the continuous-modernisation workflow.

**Say:** “The legacy application is the starting point, not the end state. First, we understand and validate what exists. The autonomous workflow proposes changes for pull request review. The human-in-the-loop workflow also requires approval of the plan before execution. Both keep the merge decision with people. This makes modernisation part of how we plan, build, review, ship, and operate software, and creates a foundation for future database migration and AI-assisted troubleshooting.”

To stop without deleting data, run `.\scripts\stop-demo.ps1` from the same application folder. Keep `.env` with the database volume; do not share or commit it. See the [run guide](./caldova%20Pharmaceuticals/README.md) for tests, reset commands, and error recovery. This old stack is a local demo only; do not expose it to a shared network.


