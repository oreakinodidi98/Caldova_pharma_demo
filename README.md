# Caldova Pharmaceuticals: Legacy Production Line Demo

> Caldova Pharmaceuticals is a fictional company. This page describes the customer background and intended legacy demo. The operating figures are illustrative, not measured results.

## Customer background

Caldova Pharmaceuticals manufactures medicines for global markets. It operates four plants with a total of 160 production lines, each running two shifts per day.

Production teams depend on an on-premises application to monitor lines and investigate stops. The application has changed little in years. Several of its original engineers have left, and the remaining team has limited knowledge of the code and its design.

Caldova wants to reduce production delays, but it must first understand the application that its teams use today.

## Current application

The intended legacy demo represents a monolithic application with an older interface and a largely manual troubleshooting process.

| Area | Intended legacy setup |
| --- | --- |
| Hosting | On-premises infrastructure |
| Application | Java 8 and Spring Boot 2.7.18 |
| Database | Oracle Database |
| Interface | Production line overview with access to individual lines |
| Diagnostic information | Limited alarm history, equipment signals, readiness checks, and equipment manuals |

An operator can identify a line with a problem and open its details. However, the operator must review the available information manually to determine what to check next. The application does not provide AI-assisted diagnosis.

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

This page describes the scenario, not a claim that the application is already implemented. Application modernisation, the database migration, AI agents, model tuning, forecasting, shift scheduling, and cloud deployment are planned outcomes or future ambitions, not capabilities included in this legacy demo.

