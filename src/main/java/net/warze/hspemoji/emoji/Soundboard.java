package net.warze.hspemoji.emoji;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.Map;

/**
 * @author Warze
 */
public class Soundboard {
    private long lastPlayTime = 0;
    private static final int COOLDOWN = 500;

    private static final Map<Sound, SoundEvent> SOUND_MAP = Map.of(
        Sound.MOO, SoundEvents.ENTITY_COW_AMBIENT,
        Sound.MEOW, SoundEvents.ENTITY_CAT_AMBIENT,
        Sound.DOOM, SoundEvents.AMBIENT_CAVE.value(),
        Sound.DONOT, SoundEvents.ENTITY_PILLAGER_AMBIENT,
        Sound.SLURP, SoundEvents.ENTITY_GENERIC_DRINK.value()
    );

    private static final Map<String, Sound> MESSAGE_MAP = Map.of(
        ":moo:", Sound.MOO,
        ":meow:", Sound.MEOW,
        ":doom:", Sound.DOOM,
        ":donot:", Sound.DONOT,
        ":slurp:", Sound.SLURP
    );

    public void playSound(Sound sound) {
        long now = System.currentTimeMillis();
        if (now - lastPlayTime < COOLDOWN) return;
        lastPlayTime = now;

        SoundEvent event = SOUND_MAP.get(sound);
        if (event != null && MinecraftClient.getInstance().player != null) {
            MinecraftClient client = MinecraftClient.getInstance();
            client.getSoundManager().play(
                new PositionedSoundInstance(
                    event,
                    SoundCategory.VOICE, // plays over Voice/Speech slider
                    1.0F,
                    1.0F,
                    client.player.getRandom(),
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ()
                )
            );
        }
    }

    public void playSoundFromMessage(String message) {
        for (var entry : MESSAGE_MAP.entrySet()) {
            if (message.contains(entry.getKey())) {
                playSound(entry.getValue());
                break;
            }
        }
    }
}
