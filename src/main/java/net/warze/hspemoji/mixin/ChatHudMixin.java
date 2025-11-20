package net.warze.hspemoji.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
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

import java.util.Iterator;
import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow @Final private List<ChatHudLine.Visible> visibleMessages;

    @Unique
    private EmojiMessage hspemoji$pending;
    @Unique
    private Iterator<ChatHudLine.Visible> hspemoji$renderIterator;
    @Unique
    private final EmojiTokenizer hspemoji$tokenizer = new EmojiTokenizer();

    @Inject(method = "addVisibleMessage", at = @At("HEAD"))
    private void hspemoji$prepare(ChatHudLine message, CallbackInfo ci) {
        hspemoji$pending = hspemoji$tokenizer.tokenize(message.content());
        EmojiSoundBridge.INSTANCE.handle(hspemoji$pending);
    }

    @Inject(method = "addVisibleMessage", at = @At("TAIL"))
    private void hspemoji$attach(ChatHudLine message, CallbackInfo ci) {
        if (hspemoji$pending == null || !hspemoji$pending.hasEmoji() || visibleMessages.isEmpty()) {
            hspemoji$pending = null;
            return;
        }
        ((EmojiAttributedLine) (Object) visibleMessages.get(0)).hspemoji$setEmojiMessage(hspemoji$pending);
        hspemoji$pending = null;
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void hspemoji$prepareRender(DrawContext context, int tick, int mouseX, int mouseY, boolean focused, CallbackInfo ci) {
        hspemoji$renderIterator = visibleMessages.iterator();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/OrderedText;III)I"))
    private int hspemoji$renderLine(DrawContext context, TextRenderer textRenderer, OrderedText orderedText, int x, int y, int color) {
        ChatHudLine.Visible line = hspemoji$renderIterator != null && hspemoji$renderIterator.hasNext() ? hspemoji$renderIterator.next() : null;
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
        hspemoji$renderIterator = null;
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
}
