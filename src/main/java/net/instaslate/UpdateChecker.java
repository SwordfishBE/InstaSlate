package net.instaslate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;

public final class UpdateChecker {
    private static final String PROJECT_ID = "yur8ms3Y";
    private static final String MINECRAFT_MOD_ID = "minecraft";
    private static final String FABRIC_LOADER = "fabric";
    private static final String RELEASE = "release";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private UpdateChecker() {
    }

    public static void checkForUpdates() {
        Optional<String> currentVersion = currentModVersion();
        if (currentVersion.isEmpty()) {
            InstaSlate.LOGGER.debug("{} Skipping update check because the current mod version could not be resolved.", InstaSlate.getLogPrefix());
            return;
        }

        Optional<String> minecraftVersion = currentMinecraftVersion();
        if (minecraftVersion.isEmpty()) {
            InstaSlate.LOGGER.debug("{} Skipping update check because the current Minecraft version could not be resolved.", InstaSlate.getLogPrefix());
            return;
        }

        URI uri = URI.create("https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version");
        HttpRequest request = HttpRequest.newBuilder(uri)
            .header("User-Agent", "InstaSlate/" + currentVersion.get() + " (modrinth update check)")
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();

        CompletableFuture.runAsync(() -> {
            try {
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    InstaSlate.LOGGER.debug("{} Modrinth update check returned status {}", InstaSlate.getLogPrefix(), response.statusCode());
                    return;
                }

                JsonElement parsed = JsonParser.parseString(response.body());
                if (!parsed.isJsonArray()) {
                    return;
                }

                JsonArray versionsArray = parsed.getAsJsonArray();
                if (versionsArray.isEmpty()) {
                    InstaSlate.LOGGER.debug("{} Modrinth update check returned no versions.", InstaSlate.getLogPrefix());
                    return;
                }

                Optional<VersionCandidate> latestVersion = findLatestVersion(versionsArray, minecraftVersion.get());
                if (latestVersion.isEmpty()) {
                    InstaSlate.LOGGER.debug(
                        "{} No compatible Modrinth release found for Minecraft {} during update check.",
                        InstaSlate.getLogPrefix(),
                        minecraftVersion.get()
                    );
                    return;
                }

                String latestVersionNumber = latestVersion.get().versionNumber();
                if (isNewerVersionAvailable(currentVersion.get(), latestVersionNumber)) {
                    InstaSlate.LOGGER.info("{} New version available: {} (current: {})", InstaSlate.getLogPrefix(), latestVersionNumber, currentVersion.get());
                } else {
                    InstaSlate.LOGGER.debug("{} No new version available. current={}, latest={}", InstaSlate.getLogPrefix(), currentVersion.get(), latestVersionNumber);
                }
            } catch (IOException | InterruptedException exception) {
                InstaSlate.LOGGER.debug("{} Update check failed: {}", InstaSlate.getLogPrefix(), exception.getMessage());
                if (exception instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private static Optional<String> currentModVersion() {
        return FabricLoader.getInstance().getModContainer(InstaSlate.MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .filter(version -> !version.isBlank());
    }

    private static Optional<String> currentMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer(MINECRAFT_MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .filter(version -> !version.isBlank());
    }

    private static Optional<VersionCandidate> findLatestVersion(JsonArray versionsArray, String minecraftVersion) {
        VersionCandidate newestCompatibleRelease = null;

        for (JsonElement element : versionsArray) {
            if (!element.isJsonObject()) {
                continue;
            }

            JsonObject versionObject = element.getAsJsonObject();
            if (!RELEASE.equals(getString(versionObject, "version_type").orElse(null))) {
                continue;
            }

            Optional<VersionCandidate> candidate = getVersionCandidate(versionObject);
            if (candidate.isEmpty()) {
                continue;
            }

            VersionCandidate versionCandidate = candidate.get();
            if (jsonArrayContains(versionObject, "loaders", FABRIC_LOADER)
                && jsonArrayContains(versionObject, "game_versions", minecraftVersion)
                && isNewerCandidate(versionCandidate, newestCompatibleRelease)) {
                newestCompatibleRelease = versionCandidate;
            }
        }

        return Optional.ofNullable(newestCompatibleRelease);
    }

    private static boolean isNewerCandidate(VersionCandidate candidate, VersionCandidate currentBest) {
        return currentBest == null || candidate.publishedAt().isAfter(currentBest.publishedAt());
    }

    private static Optional<VersionCandidate> getVersionCandidate(JsonObject versionObject) {
        Optional<String> versionNumber = getString(versionObject, "version_number");
        if (versionNumber.isEmpty() || !isValidVersionNumber(versionNumber.get())) {
            return Optional.empty();
        }

        Optional<Instant> publishedAt = getPublishedAt(versionObject);
        if (publishedAt.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new VersionCandidate(versionNumber.get(), publishedAt.get()));
    }

    private static Optional<String> getString(JsonObject object, String fieldName) {
        JsonElement element = object.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return Optional.empty();
        }

        try {
            String value = element.getAsString();
            return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
        } catch (UnsupportedOperationException | ClassCastException | IllegalStateException exception) {
            return Optional.empty();
        }
    }

    private static boolean jsonArrayContains(JsonObject object, String fieldName, String expectedValue) {
        JsonElement element = object.get(fieldName);
        if (element == null || !element.isJsonArray()) {
            return false;
        }

        for (JsonElement arrayElement : element.getAsJsonArray()) {
            if (!arrayElement.isJsonPrimitive()) {
                continue;
            }

            try {
                if (expectedValue.equals(arrayElement.getAsString())) {
                    return true;
                }
            } catch (UnsupportedOperationException | ClassCastException | IllegalStateException exception) {
                return false;
            }
        }

        return false;
    }

    private static Optional<Instant> getPublishedAt(JsonObject versionObject) {
        Optional<String> publishedAt = getString(versionObject, "date_published");
        if (publishedAt.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(publishedAt.get()));
        } catch (DateTimeParseException exception) {
            return Optional.empty();
        }
    }

    private static boolean isValidVersionNumber(String versionNumber) {
        try {
            Version.parse(versionNumber);
            return true;
        } catch (Exception exception) {
            return false;
        }
    }

    private static boolean isNewerVersionAvailable(String currentVersion, String candidateVersion) {
        try {
            Version current = Version.parse(currentVersion);
            Version candidate = Version.parse(candidateVersion);
            return candidate.compareTo(current) > 0;
        } catch (Exception exception) {
            InstaSlate.LOGGER.debug(
                "{} Unable to compare versions. current={}, candidate={}, reason={}",
                InstaSlate.getLogPrefix(),
                currentVersion,
                candidateVersion,
                exception.getMessage()
            );
            return false;
        }
    }

    private record VersionCandidate(String versionNumber, Instant publishedAt) {
    }
}
