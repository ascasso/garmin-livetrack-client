# GOALS

`garmin-livetrack-client` is a lightweight Java 25+ library for accessing Garmin LiveTrack / inReach public telemetry sessions.

## Goals

- Resolve Garmin LiveTrack public URLs.
- Follow redirects to active session URLs.
- Fetch and parse telemetry track points.
- Expose a clean immutable Java API.
- Keep the core library framework-agnostic.
- Remain highly Spring-friendly without depending on Spring.
- Use minimal dependencies.
- Use Java `HttpClient` and Jackson.
- Provide strong unit tests.
- Keep token handling secure.

## Design Principles

- Java 25+.
- Gradle Groovy DSL.
- No Kotlin.
- No Spring dependency in core.
- Immutable records where appropriate.
- Defensive parsing.
- Small focused public API.
- OSS-friendly structure.

## Non-Goals

The core library will not provide:

- Scheduling.
- Persistence.
- Web servers.
- Telegram integration.
- MCP integration.
- Routing or weather logic.
- Reactive frameworks.

## Future Ideas

These are possible future additions, not commitments for the core library:

- Password-protected LiveTrack support.
- Optional Spring Boot starter.
- MCP server integration.
- GPX export/import utilities.
- Maven Central publishing.
