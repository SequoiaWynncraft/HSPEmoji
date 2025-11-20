package net.warze.hspemoji.client.resource;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.warze.hspemoji.client.HspEmojiClient;
import net.warze.hspemoji.client.emoji.EmojiRegistry;
import net.warze.hspemoji.client.emoji.EmojiSprite;

import java.util.HashMap;
import java.util.Map;

public final class EmojiReloadListener implements SimpleSynchronousResourceReloadListener {
    private final EmojiRegistry registry;

    public EmojiReloadListener(EmojiRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(HspEmojiClient.MOD_ID, "emoji_reload");
    }

    @Override
    public void reload(ResourceManager manager) {
        Map<String, EmojiSprite> collected = new HashMap<>();
        manager.findResources("emoji", path -> path.getPath().endsWith(".png")).forEach((id, resource) -> {
            String path = id.getPath();
            int slash = path.lastIndexOf('/') + 1;
            int dot = path.lastIndexOf('.');
            if (dot <= slash) {
                return;
            }
            String name = path.substring(slash, dot);
            String token = ":" + name + ":";
            collected.put(token, new EmojiSprite(id));
        });
        registry.replaceAll(collected);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.inGameHud != null) {
            client.execute(() -> client.inGameHud.getChatHud().reset());
        }
    }
}
