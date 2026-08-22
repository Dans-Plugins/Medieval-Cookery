# Contributing to Medieval Cookery

Thank you for your interest in contributing!

## Workflow

1. Fork the repository.
2. Create a feature branch from `main`: `git checkout -b feature/my-change`
3. Make your changes.
4. Open a pull request against `main`.
5. Reference the related GitHub issue in your pull request description.

## Building

```
mvn clean package
```

Sources are compiled at Java source/target level 8, so any JDK from 8 onward builds the
project. Level 8 is deliberate: the packaged jar has to stay loadable on the Java 8 and
Java 11 servers that run the Minecraft 1.16 API this plugin targets. Continuous
integration builds on JDK 17.

## Testing

```
mvn test
```

Tests are written with JUnit 5 and live in `src/test/java`. `mvn clean package` runs them
as part of the build.

## Code Style

- Language: Java
- Build tool: Maven (`mvn clean package`)
- Follow existing conventions in the codebase.

## Adding Recipes

New recipes are defined in `src/main/resources/recipes.yml`, which is the default copied
to `plugins/MedievalCookery/recipes.yml` on a server's first startup. Each recipe entry
requires:
- `name` — display name
- `recipe` — 3×3 crafting grid pattern, as a list of exactly 3 strings
- `symbols` — material mappings for pattern characters
- `hungerDecrease` — duration in ticks of the Saturation effect applied after eating
- `textureBase64` — custom item texture (optional)
- `afterEatItem` — material returned to the player after eating (optional)

See [CONFIG.md](CONFIG.md) for how each field is read and what happens when one is
missing or invalid. `RecipesResourceTest` checks the bundled file against this schema, so
a new recipe that omits a required field, declares an unused symbol or names a material
Bukkit does not recognise fails the build.

## Reporting Issues

Open a [GitHub issue](https://github.com/Dans-Plugins/Medieval-Cookery/issues) with a clear description of the bug or feature request.
