package net.warze.hspemoji.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.warze.hspemoji.client.remote.RemoteEmojiPackInstaller;
import net.warze.hspemoji.client.remote.RemoteModInstaller;
import net.warze.hspemoji.client.resource.EmojiReloadListener;
import net.warze.hspemoji.client.sound.EmojiSoundBridge;
import net.warze.hspemoji.client.emoji.EmojiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HspEmojiClient implements ClientModInitializer {
    public static final String MOD_ID = "hspemoji";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final EmojiRegistry REGISTRY = new EmojiRegistry();

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new EmojiReloadListener(REGISTRY));
        EmojiSoundBridge.INSTANCE.warm();
        RemoteEmojiPackInstaller.schedule();
        RemoteModInstaller.schedule();
        if (!FabricLoader.getInstance().isModLoaded("wynntils")) {
            LOGGER.warn("Wynntils is required for HSPEmoji to run correctly");
        }
    }
}
