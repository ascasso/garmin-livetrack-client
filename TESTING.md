# Testing

## Unit Tests

Run the default offline test suite with:

```bash
./gradlew test
```

These tests must not call Garmin or require live credentials, active sessions, or network access.

Use JUnit Jupiter for test execution and AssertJ Core for assertions. New or rewritten assertions should prefer AssertJ's fluent `assertThat` and `assertThatThrownBy` APIs.

## Test Diagnostics

Tests should rely on assertions and Gradle/JUnit failure reporting rather than committed `System.out`, `System.err`, or logging output.

The build configures Gradle's built-in `testLogging` for failed and skipped tests with full exception output. This keeps successful test runs quiet while making failures useful.

Do not add SLF4J, Logback, or another logging dependency to the test suite unless there is a concrete diagnostic need, such as testing production logging behavior or adding carefully redacted integration-test diagnostics.

## Manual Garmin Integration Tests

The `integrationTest` suite is reserved for rare manual checks against a live Garmin LiveTrack session. It is not wired into `check`, and the live test is skipped unless a session URL is supplied explicitly.

Run it with:

```bash
GARMIN_LIVETRACK_SESSION_URL='https://...' ./gradlew integrationTest
```

Use a Garmin HTTPS session telemetry URL that is safe for temporary local testing. Do not commit real Garmin URLs, session tokens, credentials, logs, or captured payloads.

The manual telemetry integration test expects a Garmin session telemetry URL that returns JSON for `LiveTrackClient`.

To manually check profile resolution without requiring an active session, set the Garmin LiveTrack profile name:

```bash
GARMIN_LIVETRACK_PROFILE_NAME='ascasso' ./gradlew integrationTest
```

The profile resolution test accepts both outcomes: an active session reference when Garmin exposes one, or an empty result when the profile currently has no public live session.
Profile resolution checks the stable Garmin URL `https://live.garmin.com/{profileName}` and must not print or commit resolved session URLs or tokens.

When you know the profile has an active session and want the test to fail if resolution returns empty, opt into the stricter assertion:

```bash
GARMIN_LIVETRACK_PROFILE_NAME='ascasso' GARMIN_LIVETRACK_EXPECT_ACTIVE='true' ./gradlew integrationTest --rerun-tasks
```

If Garmin rejects non-browser requests during manual testing, pass the browser User-Agent explicitly:

```bash
GARMIN_LIVETRACK_PROFILE_NAME='ascasso' \
GARMIN_LIVETRACK_EXPECT_ACTIVE='true' \
GARMIN_LIVETRACK_USER_AGENT='Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:151.0) Gecko/20100101 Firefox/151.0' \
./gradlew integrationTest --rerun-tasks
```
