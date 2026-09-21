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

Building needs JDK 17 or newer: the Spigot API jar the project compiles against (1.20.4,
the API version is set in `pom.xml`) is itself compiled for Java 17, and an older `javac`
cannot read it. Sources are still compiled at Java source/target level 8. Continuous
integration builds on JDK 17. The packaged jar runs on any Spigot server from 1.18.1 on,
the first version with the `PlayerProfile` API that gives a food head its texture.

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
