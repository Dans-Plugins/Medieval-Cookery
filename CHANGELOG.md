# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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
