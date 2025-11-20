package net.warze.hspemoji.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.warze.hspemoji.client.chat.EmojiMessage;
import net.warze.hspemoji.client.chat.EmojiSegment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class EmojiSoundBridge {
    public static final EmojiSoundBridge INSTANCE = new EmojiSoundBridge();
    private final Map<String, SoundEvent> soundMap = new HashMap<>();
    private long lastPlay;
    private EmojiSoundBridge() {
    }

    public void warm() {
    soundMap.put(lower(":moo:"), SoundEvents.ENTITY_COW_AMBIENT);
    soundMap.put(lower(":meow:"), SoundEvents.ENTITY_CAT_AMBIENT);
    soundMap.put(lower(":doom:"), SoundEvents.AMBIENT_CAVE.value());
    soundMap.put(lower(":donot:"), SoundEvents.ENTITY_PILLAGER_AMBIENT);
    soundMap.put(lower(":slurp:"), SoundEvents.ENTITY_GENERIC_DRINK.value());
    }

    public void handle(EmojiMessage message) {
        if (!message.hasEmoji()) {
            return;
        }
        for (EmojiSegment segment : message.segments()) {
            if (segment instanceof EmojiSegment.EmojiEntry entry) {
                play(entry.token());
                return;
            }
        }
    }

    private void play(String token) {
        SoundEvent event = soundMap.get(lower(token));
        if (event == null) {
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastPlay < 500) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }
        lastPlay = now;
        client.getSoundManager().play(new PositionedSoundInstance(event, SoundCategory.VOICE, 1.0F, 1.0F, client.player.getRandom(), client.player.getX(), client.player.getY(), client.player.getZ()));
    }

    private String lower(String token) {
        return token.toLowerCase(Locale.ROOT);
    }
}
