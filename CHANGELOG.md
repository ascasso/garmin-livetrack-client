# Changelog

All notable changes to this project will be documented in this file.

This project follows Semantic Versioning: https://semver.org/.
The format is inspired by Keep a Changelog: https://keepachangelog.com/en/1.1.0/.

## [Unreleased]

### Added

- Initialized Gradle Java library project targeting Java 25.
- Added Jackson 3 dependency guidance through the Gradle version catalog.
- Added project architecture and contribution guidance in `AGENTS.md`.
- Added project goals, non-goals, and future ideas in `GOALS.md`.
- Added initial project README.
- Added EditorConfig rules for consistent UTF-8, LF, final newline, trailing whitespace, and indentation behavior.
- Added Apache License 2.0.
- Added Git repository metadata and ignore rules suitable for Gradle, IntelliJ IDEA, and GitHub.
- Replaced generated sample code with the initial `io.github.ascasso.garmin.livetrack` API, immutable telemetry/configuration records, token-redacted session references, exception hierarchy, and focused JUnit tests.
- Added a manually invoked `integrationTest` suite for optional live Garmin session checks and documented the workflow in `TESTING.md`.
- Added AssertJ Core 3.27.7 as the preferred assertion library and migrated existing test assertions to AssertJ.
- Updated JUnit Jupiter from 6.0.1 to 6.1.0 after verifying no documented breaking changes affect the current test suite.
