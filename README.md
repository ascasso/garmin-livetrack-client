# garmin-livetrack-client

Lightweight Java 25+ client library for Garmin LiveTrack / inReach public session telemetry.

## Status

Early development. The public API is not stable yet and may change before `1.0.0`.

## Goals

- Resolve Garmin LiveTrack public session data.
- Fetch and parse telemetry track points.
- Expose a small immutable Java API.
- Stay framework-agnostic and Spring-friendly.
- Use Java `HttpClient` and Jackson.
- Keep dependencies minimal.
- Avoid schedulers, polling loops, persistence, and framework lifecycle behavior.
- Handle Garmin session tokens carefully.

See [GOALS.md](GOALS.md) for more detail.

## Requirements

- Java 25+
- Gradle

## Development

Run the offline test suite:

```bash
./gradlew test
```

Manual Garmin integration testing is documented in [TESTING.md](TESTING.md).
Source code usage examples are available in [docs/source-code-usage.md](docs/source-code-usage.md).

## Basic Usage

Resolve the stable Garmin LiveTrack profile URL first. The active-session result is empty when Garmin does not currently expose an active public session for that profile.

```java
LiveTrackClient client = new LiveTrackClient();

client.resolveActiveSession("ascasso")
        .ifPresent(session -> {
            LiveTrackSession activeSession = client.fetchSession(session);
            TelemetrySnapshot snapshot = activeSession.telemetrySnapshot();
            // Use snapshot.trackPoints().
        });
```

To discover completed sessions that Garmin exposes from the public profile, list saved sessions and then fetch the session you want.
The client follows Garmin's saved-session pagination when the profile page exposes it.

```java
LiveTrackClient client = new LiveTrackClient();

List<SavedSession> sessions = client.listSavedSessions("ascasso");

for (SavedSession savedSession : sessions) {
    String name = savedSession.sessionName().orElse("Unnamed session");
    LiveTrackSession session = client.fetchSession(savedSession.sessionReference());
    // Use name and session.trackPoints().
}
```

For applications that already manage configuration, inject a `HttpClient` and `LiveTrackClientOptions` through the constructor.
If Garmin requires a browser-like User-Agent for manual checks, configure one with `LiveTrackClientOptions.defaults().withUserAgent(...)`.

## License

Apache License 2.0. See [LICENSE](LICENSE).
