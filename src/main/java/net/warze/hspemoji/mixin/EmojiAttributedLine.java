package net.warze.hspemoji.mixin;

import net.warze.hspemoji.client.chat.EmojiMessage;

public interface EmojiAttributedLine {
    void hspemoji$setEmojiMessage(EmojiMessage message);
    EmojiMessage hspemoji$getEmojiMessage();
}
