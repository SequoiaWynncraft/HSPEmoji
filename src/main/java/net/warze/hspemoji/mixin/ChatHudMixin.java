package net.warze.hspemoji.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.util.ChatMessages;
import net.minecraft.text.OrderedText;
import net.minecraft.util.math.MathHelper;
import net.warze.hspemoji.client.chat.EmojiAttributedLine;
import net.warze.hspemoji.client.chat.EmojiMessage;
import net.warze.hspemoji.client.chat.EmojiMessageRenderer;
import net.warze.hspemoji.client.chat.EmojiTokenizer;
import net.warze.hspemoji.client.sound.EmojiSoundBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;
    @Shadow private int scrolledLines;
    @Shadow protected abstract int getWidth();
    @Shadow protected abstract double getChatScale();

    @Unique
    private boolean hspemoji$lastHadEmoji;
    @Unique
    private int hspemoji$renderIndex;
    @Unique
    private final EmojiTokenizer hspemoji$tokenizer = new EmojiTokenizer();

    @Inject(method = "addVisibleMessage", at = @At("HEAD"))
    private void hspemoji$prepare(ChatHudLine message, CallbackInfo ci) {
        EmojiMessage emojiMessage = hspemoji$tokenizer.tokenize(message.content());
        hspemoji$lastHadEmoji = emojiMessage.hasEmoji();
        EmojiSoundBridge.INSTANCE.handle(emojiMessage);
    }

    @Inject(method = "addVisibleMessage", at = @At("TAIL"))
    private void hspemoji$attach(ChatHudLine message, CallbackInfo ci) {
        if (!hspemoji$lastHadEmoji || visibleMessages.isEmpty()) {
            hspemoji$lastHadEmoji = false;
            return;
        }
        List<OrderedText> lines = hspemoji$breakRenderedLines(message);
        if (lines.isEmpty()) {
            hspemoji$lastHadEmoji = false;
            return;
        }
        int limit = Math.min(lines.size(), visibleMessages.size());
        for (int index = 0; index < limit; index++) {
            ChatHudLine.Visible visibleLine = visibleMessages.get(index);
            OrderedText orderedText = lines.get(lines.size() - 1 - index);
            EmojiMessage lineMessage = hspemoji$tokenizer.tokenize(orderedText);
            if (lineMessage.hasEmoji()) {
                ((EmojiAttributedLine) (Object) visibleLine).hspemoji$setEmojiMessage(lineMessage);
            } else {
                ((EmojiAttributedLine) (Object) visibleLine).hspemoji$setEmojiMessage(null);
            }
        }
        hspemoji$lastHadEmoji = false;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void hspemoji$prepareRender(DrawContext context, int tick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        hspemoji$renderIndex = Math.max(0, Math.min(this.scrolledLines, visibleMessages.size()));
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private int hspemoji$renderLine(DrawContext context, TextRenderer textRenderer, OrderedText orderedText, int x, int y, int color) {
        ChatHudLine.Visible line = null;
        if (hspemoji$renderIndex >= 0 && hspemoji$renderIndex < visibleMessages.size()) {
            line = visibleMessages.get(hspemoji$renderIndex++);
        }
        if (line != null) {
            EmojiAttributedLine attributed = (EmojiAttributedLine) (Object) line;
            EmojiMessage emojiMessage = attributed.hspemoji$getEmojiMessage();
            if (emojiMessage != null && emojiMessage.hasEmoji()) {
                return EmojiMessageRenderer.render(context, textRenderer, emojiMessage, x, y, color);
            }
        }
        return context.drawTextWithShadow(textRenderer, orderedText, x, y, color);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hspemoji$finishRender(CallbackInfo ci) {
        hspemoji$renderIndex = 0;
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private void hspemoji$clear(boolean clearHistory, CallbackInfo ci) {
        for (ChatHudLine.Visible line : visibleMessages) {
            ((EmojiAttributedLine) (Object) line).hspemoji$setEmojiMessage(null);
        }
    }

    @Inject(method = "refresh", at = @At("HEAD"))
    private void hspemoji$refresh(CallbackInfo ci) {
        for (ChatHudLine.Visible line : visibleMessages) {
            ((EmojiAttributedLine) (Object) line).hspemoji$setEmojiMessage(null);
        }
    }

    @Unique
    private List<OrderedText> hspemoji$breakRenderedLines(ChatHudLine message) {
    int width = MathHelper.floor((double) this.getWidth() / this.getChatScale());
        MessageIndicator.Icon icon = message.getIcon();
        if (icon != null) {
            width -= icon.width + 6;
        }
        if (width <= 0) {
            width = 0;
        }
        return ChatMessages.breakRenderedChatMessageLines(message.content(), width, this.client.textRenderer);
    }
}
