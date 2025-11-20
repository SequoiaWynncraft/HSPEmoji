package net.warze.hspemoji.client.remote;

import net.minecraft.client.MinecraftClient;
import net.warze.hspemoji.client.HspEmojiClient;
import net.warze.hspemoji.client.update.VersionComparator;
import ooo.sequoia.http.HttpClient;

import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class RemoteModInstaller {
    private static final String BASE_URL = "https://u.warze.org/w/";
    private static final String VERSION_ENDPOINT = BASE_URL + "hspemojilatest.txt";
    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9.]+");
    private static final String FILE_PREFIX = "hspemojiv";
    private static final String FILE_SUFFIX = ".jar";
    private final HttpClient client = HttpClient.newHttpClient();

    public static void schedule() {
        CompletableFuture.runAsync(() -> new RemoteModInstaller().installLatestMod());
    }

    private void installLatestMod() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft == null || minecraft.runDirectory == null) {
            return;
        }
        Path modsDir = minecraft.runDirectory.toPath().resolve("mods");
        String version = fetchRemoteVersion();
        if (version == null) {
            return;
        }
        String fileName = FILE_PREFIX + version + FILE_SUFFIX;
        Path jarTarget = modsDir.resolve(fileName);
        try {
            Files.createDirectories(modsDir);
            boolean present = Files.exists(jarTarget);
            if (!present) {
                byte[] payload = client.getBinary(BASE_URL + fileName);
                if (payload == null) {
                    HspEmojiClient.LOGGER.warn("Failed to download mod jar {}", fileName);
                    return;
                }
                Files.write(jarTarget, payload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            purgeLegacyMods(modsDir, version);
            HspEmojiClient.LOGGER.info("HSPEmoji mod jar ready ({})", version);
        } catch (Exception exception) {
            HspEmojiClient.LOGGER.warn("Unable to prepare mod jar", exception);
        }
    }

    private String fetchRemoteVersion() {
        HttpResponse<String> response = client.get(VERSION_ENDPOINT);
        if (response == null || response.body() == null) {
            return null;
        }
        String body = response.body().trim();
        if (!VERSION_PATTERN.matcher(body).matches()) {
            return null;
        }
        return body;
    }

    private void purgeLegacyMods(Path modsDir, String latestVersion) {
        try (Stream<Path> entries = Files.list(modsDir)) {
            entries.filter(path -> path.getFileName().toString().startsWith(FILE_PREFIX))
                    .filter(path -> path.getFileName().toString().endsWith(FILE_SUFFIX))
                    .filter(path -> shouldDelete(path.getFileName().toString(), latestVersion))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    private boolean shouldDelete(String fileName, String latestVersion) {
        String version = extractVersion(fileName);
        if (version == null) {
            return false;
        }
        return !Objects.equals(version, latestVersion) && !VersionComparator.isNewer(version, latestVersion);
    }

    private String extractVersion(String fileName) {
        int prefixIndex = fileName.indexOf(FILE_PREFIX);
        int suffixIndex = fileName.lastIndexOf(FILE_SUFFIX);
        if (prefixIndex != 0 || suffixIndex <= FILE_PREFIX.length()) {
            return null;
        }
        return fileName.substring(FILE_PREFIX.length(), suffixIndex);
    }
}
