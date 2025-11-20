package net.warze.hspemoji.client.emoji;

import net.warze.hspemoji.client.HspEmojiClient;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EmojiRegistry {
    private final Map<String, EmojiSprite> sprites = new ConcurrentHashMap<>();

    public void replaceAll(Map<String, EmojiSprite> payload) {
        sprites.clear();
        payload.forEach((key, value) -> sprites.put(normalize(key), value));
        HspEmojiClient.LOGGER.info("Loaded {} emojis", sprites.size());
    }

    public EmojiSprite get(String token) {
        if (token == null) {
            return null;
        }
        return sprites.get(normalize(token));
    }

    public boolean contains(String token) {
        return get(token) != null;
    }

    public int size() {
        return sprites.size();
    }

    public Collection<String> keys() {
        return sprites.keySet();
    }

    private String normalize(String token) {
        return token.toLowerCase(Locale.ROOT);
    }
}
