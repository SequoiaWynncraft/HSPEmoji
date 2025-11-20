package net.warze.hspemoji.client.remote;

import net.minecraft.client.MinecraftClient;
import net.warze.hspemoji.client.HspEmojiClient;
import ooo.sequoia.http.HttpClient;

import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class RemoteEmojiPackInstaller {
    private static final String BASE_URL = "https://u.warze.org/w/";
    private static final String VERSION_ENDPOINT = BASE_URL + "hspemojipacklatest.txt";
    private static final Pattern VERSION_PATTERN = Pattern.compile("[0-9.]+");
    private static final String FILE_PREFIX = "hspemojiv";
    private static final String FILE_SUFFIX = ".zip";
    private final HttpClient client = HttpClient.newHttpClient();

    public static void schedule() {
        CompletableFuture.runAsync(() -> new RemoteEmojiPackInstaller().installLatestPack());
    }

    private void installLatestPack() {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        if (minecraft == null || minecraft.runDirectory == null) {
            return;
        }
        Path gameDir = minecraft.runDirectory.toPath();
        String version = fetchRemoteVersion();
        if (version == null) {
            return;
        }
        String fileName = FILE_PREFIX + version + FILE_SUFFIX;
        Path packDir = gameDir.resolve("resourcepacks");
        Path packFile = packDir.resolve(fileName);
        try {
            Files.createDirectories(packDir);
            if (Files.notExists(packFile)) {
                byte[] payload = client.getBinary(BASE_URL + fileName);
                if (payload == null) {
                    HspEmojiClient.LOGGER.warn("Failed to download emoji pack {}", fileName);
                    return;
                }
                Files.write(packFile, payload, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
            purgeLegacyPacks(packDir, fileName);
            enablePackEntry(gameDir.resolve("options.txt"), fileName);
            HspEmojiClient.LOGGER.info("Emoji resource pack ready ({})", version);
        } catch (Exception exception) {
            HspEmojiClient.LOGGER.warn("Unable to prepare emoji resource pack", exception);
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

    private void purgeLegacyPacks(Path packDir, String currentFile) {
        try (Stream<Path> entries = Files.list(packDir)) {
            entries.filter(path -> path.getFileName().toString().startsWith(FILE_PREFIX))
                    .filter(path -> !Objects.equals(path.getFileName().toString(), currentFile))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    private void enablePackEntry(Path optionsFile, String fileName) throws Exception {
        List<String> lines = Files.exists(optionsFile) ? Files.readAllLines(optionsFile, StandardCharsets.UTF_8) : new ArrayList<>();
        String entry = "\"file/" + fileName + "\"";
        boolean updated = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith("resourcePacks:[")) {
                continue;
            }
            updated = true;
            String content = line.substring(line.indexOf('[') + 1, line.lastIndexOf(']'));
            List<String> packs = new ArrayList<>();
            if (!content.isBlank()) {
                for (String token : content.split(",")) {
                    String value = token.trim();
                    if (!value.isEmpty() && !value.startsWith("\"file/hspemoji")) {
                        packs.add(value);
                    }
                }
            }
            packs.add(entry);
            lines.set(i, "resourcePacks:[" + String.join(",", packs) + "]");
            break;
        }
        if (!updated) {
            lines.add("resourcePacks:[" + entry + "]");
        }
        Files.write(optionsFile, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
