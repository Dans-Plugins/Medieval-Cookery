# Contributing to Medieval Cookery

Thank you for your interest in contributing!

## Workflow

1. Fork the repository.
2. Create a feature branch from `main`: `git checkout -b feature/my-change`
3. Make your changes.
4. Open a pull request against `main`.
5. Reference the related GitHub issue in your pull request description.

## Code Style

- Language: Java
- Build tool: Maven (`mvn clean package`)
- Follow existing conventions in the codebase.

## Adding Recipes

New recipes are defined in `src/main/resources/recipes.yml`. Each recipe entry requires:
- `name` — display name
- `recipe` — 3×3 crafting grid pattern
- `symbols` — material mappings for pattern characters
- `hungerDecrease` — saturation provided when eaten
- `textureBase64` — custom item texture (optional)

## Reporting Issues

Open a [GitHub issue](https://github.com/Dans-Plugins/Medieval-Cookery/issues) with a clear description of the bug or feature request.
