# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java
- Build tool: Maven
- Target platform: Spigot / Paper (Minecraft plugin)
- API version: 1.16+

## Project Structure

- `src/main/java/dansplugins/medievalcookery/` – Plugin source code
- `src/main/resources/plugin.yml` – Plugin metadata
- `src/main/resources/recipes.yml` – Default food recipe definitions, copied to
  `plugins/MedievalCookery/recipes.yml` on a server's first startup

## Contribution Workflow

- Branch from `main` for all changes.
- Open a pull request against `main`.
- Reference the related GitHub issue in every pull request description.
