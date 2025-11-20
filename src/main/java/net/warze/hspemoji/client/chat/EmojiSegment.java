package net.warze.hspemoji.client.chat;

import net.minecraft.text.Text;
import net.warze.hspemoji.client.emoji.EmojiSprite;

public sealed interface EmojiSegment permits EmojiSegment.TextSegment, EmojiSegment.EmojiEntry {
    record TextSegment(Text content) implements EmojiSegment {}
    record EmojiEntry(String token, EmojiSprite sprite) implements EmojiSegment {}
}
