package net.warze.hspemoji.client.emoji;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public final class EmojiSprite {
    private final Identifier texture;

    public EmojiSprite(Identifier texture) {
        this.texture = texture;
    }

    public Identifier texture() {
        return texture;
    }

    public void draw(DrawContext context, TextRenderer textRenderer, int x, int y) {
        int size = textRenderer.fontHeight;
        int offset = size / 8;
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y - offset, 0, 0, size, size, size, size);
    }
}
