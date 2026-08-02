# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Fixed
- `plugin.yml`'s `main` class pointed to `com.gmail.medievalcookery.MedievalCookery`, which does not exist, preventing the plugin from loading at all. It now points to the actual main class, `dansplugins.medievalcookery.MedievalCookery`.

## [0.1]

### Added
- Craftable food recipes: Salmon Roll, Beet Salad, Bowl of Rice, Steak Sandwich, Salted Herring.
- Custom item textures via Base64-encoded skin data.
- `hungerDecrease` and `afterEatItem` recipe properties.
