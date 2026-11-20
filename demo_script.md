# Demo: continuously modernising an app

## Presenter run of show

**Story in one sentence:** Caldova has a useful but ageing production-line application; the demo shows how we can use Agentic AI development with github copilot to understand the application, set a starting point, and make modernisation a regular part of youd SDLC so your application are constantly evolving with technology your system depends and is built on .


### 1. Set the scene

**Action:** Open the project in the GitHub Copilot app. Show the the `caldova_Pharmaceuticals` folder. 

**Say:** “We will use the GitHub Copilot app to explore this repository and run the app. This was made from the design spec we got from our last call. 

**Action:** Crete a terminal. cd into enviroment using `cd C:\caldova_pharma_demo\caldova_Pharmaceuticals` run `pwsh .\scripts\start-demo.ps1`. open browser and go to the terminal 
enter password in env file

**Say:** Caldova is a fictional pharmaceutical manufacturer. It has four plants and 160 production lines. The current application gives operators a place to investigate line stops, but the process is manual and the system is difficult to change. Our goal today is to show both a path to modernise and keep it current.”

**Action:** In the GitHub Copilot app, ask Copilot to summarise the repository and identify the application folder, its runtime stack, and the available workflows. Check its answer against the files before repeating it.

**Say:** “Copilot helps us explore the repository, but we verify important details in the source files and workflow definitions. It is an assistant, not the source of truth.”

**Say:** “The application and Oracle run locally in Docker. The startup script prepares the fictional Oracle data, builds the Java 8 application, starts it, and checks that it is ready. The database uses persistent local storage, so a normal restart does not remove the demo data.”

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

### 7. Close with the point of the demo

**Action:** Return to the app or the continuous-modernisation workflow.

**Say:** “The legacy application is the starting point, not the end state. First, we understand and validate what exists. Then we can make smaller, reviewed changes as part of how we plan, build, review, ship, and operate software. That creates a foundation for the future database migration and capabilities such as AI-assisted troubleshooting.”

To stop without deleting data, run `.\scripts\stop-demo.ps1` from the same application folder. Keep `.env` with the database volume; do not share or commit it. See the [run guide](./caldova%20Pharmaceuticals/README.md) for tests, reset commands, and error recovery. This old stack is a local demo only; do not expose it to a shared network.


