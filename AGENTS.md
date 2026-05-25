# garmin-livetrack-client

This repository is a modern Java open-source library for accessing Garmin LiveTrack / inReach public session telemetry.

## Project Constraints

- Use Java 25+.
- Keep Gradle build scripts in Groovy DSL.
- Do not introduce Kotlin source, Kotlin Gradle DSL, or Kotlin dependencies.
- Keep the core library framework-agnostic.
- Do not add Spring dependencies to the core module.
- Design APIs so they are easy to use from Spring applications later, for example via constructor-injected clients and immutable configuration objects.
- Prefer Java records for immutable domain and configuration data where appropriate.
- Use Java `HttpClient` for HTTP.
- Use Jackson for JSON parsing.
- Keep dependencies minimal and OSS-friendly.
- Do not add schedulers, background polling loops, or framework lifecycle behavior to the core library.
- Do not log, expose, or stringify Garmin session tokens unnecessarily.

## Expected Package Shape

Use `io.github.ascasso.garmin.livetrack` unless the published Maven coordinates later require a different base package.

Recommended packages:

- `io.github.ascasso.garmin.livetrack` for the public client API.
- `io.github.ascasso.garmin.livetrack.model` for immutable domain records.
- `io.github.ascasso.garmin.livetrack.config` for immutable client options.
- `io.github.ascasso.garmin.livetrack.exception` for public exceptions.
- `io.github.ascasso.garmin.livetrack.internal` for HTTP, URI, redirect, and Jackson implementation details.

Keep `internal` package types package-private where possible.

## API Design Principles

- Public APIs should be small, explicit, and stable.
- Prefer constructor injection over static globals.
- Expose synchronous methods first. Async wrappers can be added later using `CompletableFuture` if needed.
- Treat the stable Garmin user URL and redirected session URL as different concepts.
- Make empty telemetry payloads valid results, not errors.
- Use defensive JSON parsing: tolerate unknown fields, reject missing required coordinate fields where necessary, and handle absent `trackPoints` as an empty result only if this behavior is documented.
- Avoid putting session tokens in exception messages, log messages, or `toString()` output.

## Testing Expectations

- Use JUnit Jupiter.
- Test parsing of valid telemetry, empty `trackPoints`, unknown fields, malformed JSON, missing required fields, redirect/session resolution, and token redaction behavior.
- Prefer Java's built-in lightweight HTTP server or local test doubles over real Garmin network calls.
- Do not require live Garmin credentials or active sessions for normal test execution.

## Versioning and Changelog

- Follow Semantic Versioning 2.0.0: https://semver.org/.
- Treat the documented public Java API as the compatibility contract for SemVer.
- During initial development, prefer `0.y.z` versions until the public API is stable enough for `1.0.0`.
- Use GitHub-friendly release practices: signed or annotated version tags where practical, GitHub Releases for published artifacts, and release notes that clearly call out breaking changes, new features, fixes, and dependency updates.
- Keep a `CHANGELOG.md` once releases begin.
- The changelog does not have to strictly follow Keep a Changelog, but https://keepachangelog.com/en/1.1.0/ is a good default structure.
- Do not publish mutable release artifacts. If a released version needs changes, release a new version.

## Build Notes

- Remove Gradle init sample dependencies unless they become genuinely needed.
- Jackson should be an implementation dependency in the core library.
- JUnit Jupiter remains a test dependency through Gradle's test suite configuration.
