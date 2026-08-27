# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- The plugin enables on servers whose world is not named `world`. Startup iterated the players of `getWorld("world")`, which is null on any server with a different `level-name`, so the plugin failed to enable there with a `NullPointerException`. Every online player is now iterated directly, which also covers players outside the default world.
- A malformed entry in `recipes.yml` no longer stops the plugin from enabling. A missing `symbols` section, a `recipe` pattern that is not 3 rows of 3 characters, a symbol the pattern never uses, and a recipe with no `textureBase64` each threw out of recipe loading and took the whole plugin down with them. Each is now reported against the offending entry's id, that one entry is skipped, and the remaining recipes load.
- An unusable material name in `recipes.yml` is detected where it is configured. Symbol material names were compared to the empty string by reference, so a symbol explicitly configured as `""` was accepted, and a name Bukkit does not recognise was accepted as well — both were stored as a null material and only surfaced later as a recipe that silently failed to register. Both are now caught while the entry is read, and reported with the symbol and the material name that was configured. An unrecognised `afterEatItem`, which was swallowed by a `catch` that could not fire, is now reported too.

- The version reported to Bukkit is now the real project version. `plugin.yml` carries `${project.version}`, but no resource filtering was configured, so the packaged file kept that literal string and every install reported its version as `${project.version}`. `src/main/resources/plugin.yml` is now filtered during the build; `recipes.yml` is still copied verbatim so that its Base64 texture data is left untouched.
- The project builds again on current JDKs. `pom.xml` declared `maven-compiler-plugin` twice, the second declaration pinning source and target level 7, which JDK 20 and newer reject outright. A single pinned declaration now compiles at level 8 — chosen so the packaged jar stays loadable on the Java 8 and Java 11 servers that run the Minecraft 1.16 API this plugin targets.
- The `Dev Release` workflow now retries publishing the `dev` prerelease before giving up. The release and its tag have to be deleted and recreated for the tag to move to the new commit, and a transient API failure inside that window previously left the repository with no `dev` release at all until the workflow was re-run by hand. Each attempt now starts from a clean slate, and an exhausted retry fails loudly.

### Added

- `ConfigServiceTest`, which covers how a hand-edited `recipes.yml` entry is validated: the symbol-to-material mapping and the crafting pattern are each checked against a configuration written for the test, so the handling of a server owner's mistake is exercised rather than only the bundled default.
- A first automated test, `RecipesResourceTest`, which checks the bundled `recipes.yml` against the schema documented in `CONTRIBUTING.md`: every recipe must carry a display name, a 3×3 pattern, symbols that agree with that pattern and map to materials Bukkit recognises, and a positive `hungerDecrease`. JUnit 5 and Surefire are wired into the build, so `mvn test` and `mvn clean package` run it.
- A `Dev Release` workflow, which republishes a rolling `dev` prerelease of `main` on every non-documentation push. This is what Dan's Plugin Manager's experimental channel installs from: `/dpm get medievalcookery --experimental` reads `releases/tags/dev`, so without it there is nothing for that command to download. The prerelease is unreleased, unreviewed code and is marked as such.

## [0.2.0-SNAPSHOT-8-8-2026] – 2026-08-08

### Changed
- Medieval-Cookery is now developed AI-first. Day-to-day feature work, grooming, review and maintenance run through AI agents working directly against this repository, with the maintainers setting direction and approving what lands. The version bump marks that change in how the project is built — it is not a break in behaviour, configuration or stored data, and existing installations can upgrade in place. Released as `0.2.0-SNAPSHOT-8-8-2026`: the AI-first line has not yet been verified in live operation, and the dated snapshot designation stays until it has.

### Changed
- Documentation corrected against the source. `hungerDecrease` is documented as the duration in ticks of the Saturation potion effect rather than an amount of saturation; `CONFIG.md` and `USER_GUIDE.md` now direct server owners to `plugins/MedievalCookery/recipes.yml` rather than the bundled default; per-field requiredness, defaults and load-failure behaviour are documented; `USER_GUIDE.md` records that no event handler currently consumes a Medieval Cookery food item; `CONTRIBUTING.md` records that the build requires JDK 17; and `.github/copilot-instructions.md` points at the real source package.

### Fixed
- `plugin.yml`'s `main` class pointed to `com.gmail.medievalcookery.MedievalCookery`, which does not exist, preventing the plugin from loading at all. It now points to the actual main class, `dansplugins.medievalcookery.MedievalCookery`.

## [0.1]

### Added
- Craftable food recipes: Salmon Roll, Beet Salad, Bowl of Rice, Steak Sandwich, Salted Herring.
- Custom item textures via Base64-encoded skin data.
- `hungerDecrease` and `afterEatItem` recipe properties.
