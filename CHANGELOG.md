# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed

- The version reported to Bukkit is now the real project version. `plugin.yml` carries `${project.version}`, but no resource filtering was configured, so the packaged file kept that literal string and every install reported its version as `${project.version}`. `src/main/resources/plugin.yml` is now filtered during the build; `recipes.yml` is still copied verbatim so that its Base64 texture data is left untouched.
- The project builds again on current JDKs. `pom.xml` declared `maven-compiler-plugin` twice, the second declaration pinning source and target level 7, which JDK 20 and newer reject outright. A single pinned declaration now compiles at level 8 — chosen so the packaged jar stays loadable on the Java 8 and Java 11 servers that run the Minecraft 1.16 API this plugin targets.

### Added

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
