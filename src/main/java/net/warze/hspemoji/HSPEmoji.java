package net.warze.hspemoji;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.warze.hspemoji.utils.LoggerUtils;
import net.warze.hspemoji.utils.ModUpdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HSPEmoji implements ClientModInitializer {
	public static final String MOD_ID = "emoji";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LoggerUtils.init();
		ModUpdater.run();
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new EmojiLoader());
	}
}