package net.warze.hspemoji.client.chat;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;

public final class EmojiMessageRenderer {
    private EmojiMessageRenderer() {
    }

    public static int render(DrawContext context, TextRenderer textRenderer, EmojiMessage message, int x, int y, int color) {
        int cursor = x;
        for (EmojiSegment segment : message.segments()) {
            if (segment instanceof EmojiSegment.TextSegment literal) {
                OrderedText ordered = literal.content().asOrderedText();
                context.drawTextWithShadow(textRenderer, ordered, cursor, y, color);
                cursor += textRenderer.getWidth(ordered);
                continue;
            }
            EmojiSegment.EmojiEntry emoji = (EmojiSegment.EmojiEntry) segment;
            if (emoji.sprite() != null) {
                emoji.sprite().draw(context, textRenderer, cursor, y);
            }
            cursor += textRenderer.fontHeight;
        }
        return cursor;
    }
}
