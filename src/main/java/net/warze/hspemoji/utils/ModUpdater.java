package net.warze.hspemoji.utils;

import net.warze.hspemoji.utils.LoggerUtils;
import net.warze.hspemoji.utils.VersionUtils;
import net.minecraft.client.MinecraftClient;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class ModUpdater {
    private static final String PACK_BASE_URL = "https://u.warze.org/w/";
    private static final String PACK_INFO_URL = PACK_BASE_URL + "hspemojipacklatest.txt";
    private static final String MOD_INFO_URL = PACK_BASE_URL + "hspemojilatest.txt";

    public static void run() {
        new Thread(() -> {
            try {
                updateMod();
                updateResourcePack();
            } catch (Exception e) {
                LoggerUtils.error("Error: " + e.getMessage());
            }
        }).start();
    }

    private static void updateResourcePack() throws Exception {
        Path mcDir = MinecraftClient.getInstance().runDirectory.toPath();
        Path rpDir = mcDir.resolve("resourcepacks");
        Files.createDirectories(rpDir);

        String remoteVersion = new String(new URI(PACK_INFO_URL).toURL().openStream().readAllBytes()).trim();
        String packFileName = "hspemojiv" + remoteVersion + ".zip";
        Path outFile = rpDir.resolve(packFileName);
        String packEntry = "file/" + packFileName;

        if (Files.exists(outFile)) {
            LoggerUtils.info("Emoji resource pack already up to date, skipping.");
            return;
        }

        try (Stream<Path> stream = Files.list(rpDir)) {
            stream.filter(p -> {
                String name = p.getFileName().toString();
                return name.startsWith("hspemoji") && !name.endsWith(".sh") && !name.endsWith(".txt");
            }).forEach(p -> {
                try {
                    Files.delete(p);
                    LoggerUtils.info("Deleted old resource pack: " + p.getFileName());
                } catch (Exception e) {
                    LoggerUtils.error("Failed to delete resource pack: " + e.getMessage());
                }
            });
        }

        try (InputStream in = new URI(PACK_BASE_URL + packFileName).toURL().openStream()) {
            Files.copy(in, outFile, StandardCopyOption.REPLACE_EXISTING);
            LoggerUtils.info("Downloaded resource pack to " + outFile);
        }

        Path optionsFile = mcDir.resolve("options.txt");
        List<String> lines = Files.exists(optionsFile) ? Files.readAllLines(optionsFile) : new ArrayList<>();
        boolean found = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("resourcePacks:[")) {
                found = true;
                List<String> packs = new ArrayList<>(Arrays.asList(line.substring(15, line.length() - 1).split(",")));
                packs.replaceAll(String::trim);
                packs.removeIf(p -> p.startsWith("\"file/hspemoji"));
                packs.add("\"" + packEntry + "\"");
                lines.set(i, "resourcePacks:[" + String.join(",", packs) + "]");
                break;
            }
        }
        if (!found) lines.add("resourcePacks:[\"" + packEntry + "\"]");
        Files.write(optionsFile, lines);
        LoggerUtils.info("Updated resource pack entry in options.txt");
    }

    public static void updateMod() {
        try {
            Path mcDir = MinecraftClient.getInstance().runDirectory.toPath();
            Path modDir = mcDir.resolve("mods");
            Files.createDirectories(modDir);

            String remoteVersion = new String(new URI(MOD_INFO_URL).toURL().openStream().readAllBytes()).trim();
            if (!remoteVersion.matches("[0-9.]+")) {
                LoggerUtils.error("Invalid remote version: " + remoteVersion);
                return;
            }

            String modFileName = "hspemojiv" + remoteVersion + ".jar";
            Path jarOut = modDir.resolve(modFileName);

            if (!Files.exists(jarOut)) {
                try (InputStream in = new URI(PACK_BASE_URL + modFileName).toURL().openStream()) {
                    Files.copy(in, jarOut, StandardCopyOption.REPLACE_EXISTING);
                    LoggerUtils.info("Downloaded new mod version: " + remoteVersion);
                } catch (Exception e) {
                    LoggerUtils.error("Failed to download mod: " + e.getMessage());
                    return;
                }
            } else {
                LoggerUtils.info("Latest version of HSPEmoji already exists, skipping download");
            }

            try (Stream<Path> stream = Files.list(modDir)) {
                stream.filter(mod -> {
                    String name = mod.getFileName().toString();
                    return name.startsWith("hspemojiv") && name.endsWith(".jar");
                }).forEach(mod -> {
                    String fileName = mod.getFileName().toString();
                    String version = fileName.substring(9, fileName.length() - 4); // after "hspemojiv"
                    if (!version.equals(remoteVersion) && !VersionUtils.isNewer(version, remoteVersion)) {
                        try {
                            Files.delete(mod);
                            LoggerUtils.info("Deleted old mod version: " + version);
                        } catch (Exception e) {
                            LoggerUtils.error("Failed to delete old mod: " + e.getMessage());
                        }
                    }
                });
            }

        } catch (Exception e) {
            LoggerUtils.error("Failed to check or download mod: " + e.getMessage());
        }
    }
}
