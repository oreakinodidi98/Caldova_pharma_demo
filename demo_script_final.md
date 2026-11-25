# Demo: continuously modernising an app

## Presenter run of show

**Story in one sentence:** Caldova has a useful but ageing production-line applications; the demo shows how we can use Agentic AI development with github copilot to understand the application, set a starting point for a new engineer, go from idea to software and make modernisation a regular part of our SDLC so your application are constantly evolving with technology your system depends on and is built on .

Imagine now, i get hired i'm a new engineer at Caldova, hired just before Christmas while everyone's heads-down, has to get oriented, find the initiatives that add the most value, hint hitn modernise the app, ship a backlog feature, and move it toward Azure — using agentic AI dev workflow with GitHub Copilot .

This is what your future could look like your software developers gather requirements and steer implementation, review changes and evaluate test results, then investigate what happens in production. Agents can help them work more efficiently across all of these stages: Plan, Build, Verify, and Observe. We’ll walk through what an effective agentic engineering workflow looks like on GitHub.

## step 1:
**Action**: Open GitHub.com → your Project board. Show the issue list, views, what's assigned to "you." 

**Say**: "I just joined Caldova's dev team. Everyone's busy before Christmas, and this board is my first fifteen minutes — a wall of issues, several views, and I'm not sure where to start. 

**Say**: "Normally this is where half a day disappears — the design lives in one doc, a constraint from last quarter's call is buried in an email thread, and the reason the line-stop logic works this way is explained in a two-year-old pull request nobody remembers. I'd spend the morning just reconnecting dots before I could write anything."

**Action**: Point at one existing issue with a real description/acceptance criteria. Say: "Copilot can pull that scattered context together and turn an agreed decision straight into a scoped GitHub Issue — acceptance criteria included, not a thread someone has to translate later."

**Say**: the team have essentially keept routine triage from consuming planning time. Using GitHub Agentic Workflows to label our issues and estimate task complexity. Assign low-complexity tasks directly to Copilot, then spend our time focusing on harder decisions.

**Say**: So because the team have got everyone and every agent on the same page before I open an editor, I let the repo tell me what matters."

**Say**: and heres what happens when you pick the wrong agent for the job

**Action**: go to tab 3 -> show the session of **Roadmap issues creation** not working because the agent used was not the correct one -> click dropdown next to cloud symbol to show Product managed agent used


## Step 2 (Repo):
**Action**: In GitHub.com, prompt a frontier model : "Summarise this codebase for a new engineer: what does it do, what's the current initiative, and what open issues matter most? Ignore that this is a demo." 
 
**Say**: 
- "I verify this against the actual files before I move on and start working it"
- "rembering it's an assistant, not the source of truth." 

**Say**: Also important to note we match the models to the work by selecting Copilot Fusion to choose models and reasoning depth for the task, bringing in additional models to review a solution when needed. We set the effort level to balance quality, cost, and response time.

**Payoff line**: "It tells me the main initiative is modernisation, with a backlog of features waiting behind it. That's my two jobs for today."

**Say**: 
- i am now ready to turn that plan into code

## Step 3 (Set scene):
**Action**: Go to github copilot app 

**Action:** Open the project in the GitHub Copilot app. Show the the `caldova_Pharmaceuticals` folder. 

**Say:** “Copilot app allows you to keep your work moving without being tied to the computer where you started it. lets you pick up the same Copilot session across clients, so you can work wherever it’s most convenient for you. Giving sessions isolated copies of the repository, so an implementation task and a documentation update don’t share an unfinished working tree."

**Say:** “We will use the GitHub Copilot app to explore this repository (main branch) and run the app. This was made from the design spec we got from our last call. Caldova — four plants, 160 lines, two shifts a day. This on-prem app investigates line stops, but it's manual and the people who built it have mostly left." Flip Overview → one line's alarms → shift rota. "Fixed, fictional, read-only — no live equipment, no AI diagnosis yet. That's the starting point."

**Action:** Crete a terminal. cd into enviroment using `cd C:\caldova_pharma_demo\caldova_Pharmaceuticals` run `pwsh .\scripts\start-demo.ps1`. open browser and go to the terminal 
enter password in env file

or 

```
@"
ORACLE_PWD=
CALDOVA_OWNER_PASSWORD=
CALDOVA_READ_PASSWORD=
PRESENTER_PASSWORD=
"@ | Set-Content -Path .env

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
**Show:** localhost:8080. Overview, production-line filters, one line's alarms/signals/readiness/manual excerpts, maintenance, and the fictional shift rota.

```
docker compose stop app oracle
docker compose ps -a
```

## Step 4 (Modernise):
**Say:** The app works, but to get anywhere near those future plans — AI-assisted diagnostics, predictive models for shift planning — we need to move beyond Java 8. In real life we'd modernise the database too; for this demo we're only touching the app. Potential upgrade targets include **Java 11, 17 or 25**.”

**Say:** "in real life we would modernise the DB and app but for this demo will only focus on app"

**Say:** “One way to achive this is through **Continuous modernization** using the CLI tool from GitHub Copilot app modernisation. The workflow downloads and installs the latest version of the CLI, then runs the modernisation step using the workflow input to guide the agentic workflow.

**Say:** "We can run it manually and provide an instruction in the form of upgrade targets"

**Action**: Go to github.com and go to modernisation workflow 

For the input, use either:

- `latest` for the CLI defaults.
- `Java 21` for a demo target.
- `Spring boot 3`
- `Java 11`
- `Spring boot 4`
- `Java 25`

**Action**: Go inside modernisation workflow 

**Say:** :also most importantly it has a schedule, so we can allow it to run weekley or on a regular bassis. This allows us to inject modernisation into SDLC so the application is constantly evolving with technology your system depends and is built on

**Say:** Two things make this safe to run unattended: `ci.yml` gives us a baseline — did the code build and pass tests before we touched anything — and `copilot-setup-steps.yml` preps the cloud agent's environment with every JDK version it might need, while Java 8 stays the default for the live app. Together, that's the known-good starting point.

<!-- **Action:** Show `.github\workflows\ci.yml` 
**Say:** Build pipeline for the intended baseline check. aBefore changing an old application, we need a known starting point. CI workflow checks whether the current code builds and tests. The baseline lets us compare a future change with what worked before. This workflow will also Run  for the generated baranch and PR for different modernisation targets as the modernisation workflow later triggers  ”.

**Action:** Open `.github\workflows\copilot-setup-steps.yml`. 
**Say:** This Prepares the Copilot cloud agent environment with both legacy and modern JDKs, while keeping Java 8 as the default for the current app.  -->

**Say:** “Modernisation does not have to wait for a once-a-decade rewrite. This workflow can run on a schedule or on demand. The input tells the Modernize CLI what upgrade to consider. proposes changes and opens a pull request from a new branch. The change is not merged automatically. Reviewers still need to inspect the diff and validation results before deciding whether to merge.”

**Action:** Open `.github\workflows\reviewed-modernization.yml`. 

**Say:** for anything bigger we also have the ability to add a human check earlier: the agent assesses the application and creates a plan, but it must wait for our approval before it changes application files. We still review the code and test results before merge.

## Step 5 (backlog feature):

**Action:** Go back to copilot App . open worktree of previousley modernised app

**Say:** The workflow creates a branch like:

```text
modernize-upgrade-<run_id>-<attempt>
```
Then it opens a PR titled:
```text
chore: modernize application (Java 21)
```
**Say:** we can start a new copilot session on the branch and review the changed files.

**Action:** show process but dont actuall open new session

**Say:** we can run the application thats been modernised again in the terminal. As we can see running java 21 but no ui change 

**Action:**: open terminal 

```
cd caldova_pharmaceuticals

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

```
docker compose stop app oracle
docker compose ps -a
```
**Say**: Writing code is more than shooting off a single prompt to your LLM and seeing what you get back. And with more work on your plate than ever, it can be difficult to maintain your coding standards and deliver your projects on time. Copilot helps tasks progress independently with shared project knowledge, while letting you pick up a session where you left off across devices

**Action**: in the same copilot workspace select issue #34 and write "On this branch only, draft a short implementation plan for the interactive fault-map UI issue , then implement it, run the application, and confirm in the browser that it renders real line data. Keep the existing read-only data flow — don't touch Oracle access." 

**Say**: So still on the same branch we just modernised we can now Watch it read the issue, plan, then start editing — this is the frontend dev agent, scoped to this one feature." 

Action wait for ~15–20 seconds of visible tool calls: 

**Say**: "I ran this exact prompt earlier so we're not waiting on a Java build live — here's what it produced." 

**Action**: Switch to the pre-completed worktree, show the running fault-map in the browser. (chore: modernize application (Java 21 and spring Boot 3)_UI_change)

```
cd caldova_pharmaceuticals

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

```
docker compose stop app oracle
docker compose ps -a
```

## Step 6 (A platform ask: move it toward Azure):

**Say:** The platform team were so pleased with the work we did, they handed us the next piece before we'd even finished celebrating.

**Say**: "Second backlog item: move off on-prem toward AKS. 


**Action**: Show the containerisation agent/skill conversation (Dockerfile + manifests it produced), then the azure-deploy agent's plan targeting your existing k8-dev-sc-oa-lab-01 cluster. 

**prompt**:`Help me containrise this application creating yaml files and deploy to my kubernetes cluster k8-dev-sc-oa-lab-01 creating a namespace called Caldova_pharmaceuticals`

**Action** : go to this tab https://github.com/oreakinodidi98/Caldova_pharma_demo/tree/main/.github/skills

**Say**:The agent activates the Azure-Kubernetes skill and deploys to the cluster we already run. I ran this earlier so we're not waiting on an image build and a cluster rollout live." 

**Action**: Switch to browser / ``kubectl get pods -n caldova-pharmaceuticals`` showing it already live in the cluster.

**Say**: "Same repo, same governance pattern — a plan first, then execution, nothing deployed without that step being visible. And now in Azure

## Step 7 (Verify, then close):

**Action:** create a PR and Open the repo's Pull Requests tab, filtered to open PRs.

**Say**: "Here we have the files changed, each PR has its own CI run and its own review gate. Which we set in agent.md file and documentation folder. So a reviewer's attention goes to 'does this do what we intended,' not to untangling what changed." We also set up automatic code reviews for PRs to the main branch

**Action**: Point at the green CI check and the required-reviewer history on one PR.

**Say**: "The routine part — does it build, do the tests pass — is already settled before anyone opens the diff. The human decision stays where it should: is this plan acceptable, is this change correct."

**Action:** go to https://github.com/oreakinodidi98/Caldova_pharma_demo/settings/copilot/code_review and tab after

**say**: and if there is an issue we can Resolve findings and follow-up work without babysitting every pull request. Agentic Autofix proposes repairs for security issues, while Agent Merge automatically addresses failing checks, review comments, and merge conflicts—helping us keep your pull requests moving and preparing them for merge. And, if we configure it, Copilot will even merge your pull requests once your verification steps are passing.

**Say**: "That's Plan, Build, and Verify. The fourth piece is Observe : once this is running in AKS, this is where Copilot's connectors would check real telemetry against what we assumed and draft the regression issue automatically."

**Say:** That's the whole loop — plan, build, verify, deployed — some of it autonomous, some of it gated on a person. For Caldova this isn't really about Java. It's the line stops and the knowledge that's already walked out the door. Agents don't fix that on their own, but they mean we don't have to wait for a once-a-decade rewrite to start."

**Say**: "That's the tour. back to my slides to finish off."
