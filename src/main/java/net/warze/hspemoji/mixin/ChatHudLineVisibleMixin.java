package net.warze.hspemoji.mixin;

import net.minecraft.client.gui.hud.ChatHudLine;
import net.warze.hspemoji.client.chat.EmojiAttributedLine;
import net.warze.hspemoji.client.chat.EmojiMessage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChatHudLine.Visible.class)
public abstract class ChatHudLineVisibleMixin implements EmojiAttributedLine {
    private EmojiMessage hspemoji$message;

    @Override
    public void hspemoji$setEmojiMessage(EmojiMessage message) {
        this.hspemoji$message = message;
    }

    @Override
    public EmojiMessage hspemoji$getEmojiMessage() {
        return hspemoji$message;
    }
}
