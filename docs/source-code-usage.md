# Source Code Usage Examples

These examples show the current public Java API shape. The API is still pre-`1.0.0`, so names may change before a stable release.

## Resolve an Active Session from a Profile Name

Use the stable Garmin LiveTrack profile name first. When Garmin does not expose an active public session, the result is empty.

```java
import io.github.ascasso.garmin.livetrack.LiveTrackClient;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.util.Optional;

LiveTrackClient client = new LiveTrackClient();

Optional<SessionReference> session = client.resolveActiveSession("ascasso");

if (session.isEmpty()) {
    // No active public session is currently available for this profile.
    return;
}
```

## Fetch the Current Session Payload

Once you have a `SessionReference`, fetch the current Garmin session payload. Session URLs with `/session/{id}/token/{token}` are converted internally to Garmin's session API URL.

```java
import io.github.ascasso.garmin.livetrack.LiveTrackClient;
import io.github.ascasso.garmin.livetrack.model.LiveTrackSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;

LiveTrackClient client = new LiveTrackClient();

SessionReference sessionReference = client.resolveActiveSession("ascasso")
        .orElseThrow(() -> new IllegalStateException("No active Garmin LiveTrack session"));

LiveTrackSession session = client.fetchSession(sessionReference);

session.sessionName().ifPresent(name -> {
    // Display or store the session name.
});

int pointCount = session.trackPoints().size();
```

## Fetch Telemetry Only

If you only need track points, call `fetchTelemetry`. It returns a `TelemetrySnapshot` for the same `SessionReference`.

```java
import io.github.ascasso.garmin.livetrack.LiveTrackClient;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import io.github.ascasso.garmin.livetrack.model.TelemetrySnapshot;

LiveTrackClient client = new LiveTrackClient();

SessionReference sessionReference = client.resolveActiveSession("ascasso")
        .orElseThrow(() -> new IllegalStateException("No active Garmin LiveTrack session"));

TelemetrySnapshot snapshot = client.fetchTelemetry(sessionReference);

snapshot.trackPoints().forEach(point -> {
    double latitude = point.position().latitude();
    double longitude = point.position().longitude();
});
```

## Configure HTTP Client Options

Applications can inject their own Java `HttpClient` and immutable `LiveTrackClientOptions`. This is the preferred shape for Spring or other dependency-injected applications.

```java
import io.github.ascasso.garmin.livetrack.LiveTrackClient;
import io.github.ascasso.garmin.livetrack.config.LiveTrackClientOptions;
import java.net.http.HttpClient;
import java.time.Duration;

HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

LiveTrackClientOptions options = new LiveTrackClientOptions(Duration.ofSeconds(20))
        .withUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0");

LiveTrackClient client = new LiveTrackClient(httpClient, options);
```

## Token Handling

Avoid printing or logging raw Garmin session URLs. `SessionReference.toString()` redacts known token locations, but application code should still treat session URLs as sensitive.

```java
SessionReference sessionReference = client.resolveActiveSession("ascasso")
        .orElseThrow();

String safeForDiagnostics = sessionReference.toString();
```
