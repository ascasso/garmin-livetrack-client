package io.github.ascasso.garmin.livetrack.internal;

import io.github.ascasso.garmin.livetrack.model.SavedSession;
import io.github.ascasso.garmin.livetrack.model.SessionReference;
import java.net.URI;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProfileSessionListParser {
    private static final Pattern CSRF_TOKEN = Pattern.compile(
            "<meta\\b[^>]*\\bname=\"csrf-token\"[^>]*\\bcontent=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern GARMIN_GUID = Pattern.compile(
            "\\\\?\"garminGuid\\\\?\"\\s*[:\\\\]+\\s*\\\\?\"([0-9a-fA-F-]{36})",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern COMPLETED_SESSION = Pattern.compile(
            "<article\\b[^>]*data-sentry-component=\"CompletedSession\"[^>]*>(.*?)</article>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern SESSION_LINK = Pattern.compile(
            "<a\\b(?=[^>]*\\bhref=\"([^\"]*/session/[A-Za-z0-9-]+/token/[A-Za-z0-9]+[^\"]*)\")"
                    + "(?=[^>]*(?:\\btitle=\"([^\"]*)\"))?[^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern START_TIME = Pattern.compile(
            "<time\\b[^>]*\\bdateTime=\"([^\"]+)\"",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern TAG = Pattern.compile("<[^>]+>");

    public List<SavedSession> parse(URI profileUri, String html) {
        if (profileUri == null) {
            throw new IllegalArgumentException("profileUri is required");
        }
        if (html == null || html.isBlank()) {
            return List.of();
        }

        List<SavedSession> sessions = new ArrayList<>();
        Matcher articleMatcher = COMPLETED_SESSION.matcher(html);
        while (articleMatcher.find()) {
            parseArticle(profileUri, articleMatcher.group(1)).ifPresent(sessions::add);
        }
        if (!sessions.isEmpty()) {
            return List.copyOf(sessions);
        }

        Matcher linkMatcher = SESSION_LINK.matcher(html);
        while (linkMatcher.find()) {
            sessions.add(toSavedSession(profileUri, linkMatcher, Optional.empty()));
        }
        return List.copyOf(sessions);
    }

    public Optional<ProfileSessionsEndpoint> findProfileSessionsEndpoint(URI profileUri, String html) {
        if (profileUri == null) {
            throw new IllegalArgumentException("profileUri is required");
        }
        if (html == null || html.isBlank()) {
            return Optional.empty();
        }

        Optional<String> csrfToken = firstMatch(CSRF_TOKEN, html);
        Optional<String> garminGuid = firstMatch(GARMIN_GUID, html);
        if (csrfToken.isEmpty() || garminGuid.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new ProfileSessionsEndpoint(
                profileUri.resolve("/api/user/" + garminGuid.get() + "/profile-sessions"),
                csrfToken.get()));
    }

    private static Optional<SavedSession> parseArticle(URI profileUri, String articleHtml) {
        Matcher linkMatcher = SESSION_LINK.matcher(articleHtml);
        if (!linkMatcher.find()) {
            return Optional.empty();
        }
        return Optional.of(toSavedSession(profileUri, linkMatcher, parseStartedAt(articleHtml)));
    }

    private static SavedSession toSavedSession(
            URI profileUri,
            Matcher linkMatcher,
            Optional<Instant> startedAt) {
        String sessionPath = htmlDecode(linkMatcher.group(1));
        Optional<String> sessionName = Optional.ofNullable(linkMatcher.group(2))
                .or(() -> Optional.ofNullable(linkMatcher.group(3)).map(TAG::matcher).map(matcher -> matcher.replaceAll("")))
                .map(ProfileSessionListParser::htmlDecode)
                .map(String::strip)
                .filter(value -> !value.isEmpty());
        return new SavedSession(
                new SessionReference(profileUri, profileUri.resolve(sessionPath)),
                sessionName,
                startedAt);
    }

    private static Optional<Instant> parseStartedAt(String articleHtml) {
        Matcher matcher = START_TIME.matcher(articleHtml);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Instant.parse(htmlDecode(matcher.group(1))));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static String htmlDecode(String value) {
        return value.replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">");
    }

    private static Optional<String> firstMatch(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(htmlDecode(matcher.group(1)));
    }

    public record ProfileSessionsEndpoint(URI uri, String csrfToken) {
    }
}
