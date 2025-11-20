package net.warze.hspemoji.mixin;

import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.warze.hspemoji.client.chat.EmojiSuggestionProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class ChatInputSuggestorMixin {
    @Shadow @Final TextFieldWidget textField;
    @Shadow @Nullable private CompletableFuture<Suggestions> pendingSuggestions;

    @Unique
    private CompletableFuture<Suggestions> hspemoji$emojiFuture;

    @Inject(method = "refresh", at = @At("TAIL"))
    private void hspemoji$appendEmojiSuggestions(CallbackInfo ci) {
        if (this.pendingSuggestions == null || this.pendingSuggestions == this.hspemoji$emojiFuture) {
            return;
        }
        final String input = this.textField.getText();
        final int cursor = this.textField.getCursor();
        CompletableFuture<Suggestions> merged = this.pendingSuggestions.thenApply(base ->
                EmojiSuggestionProvider.appendEmojiSuggestions(base, input, cursor)
        );
        this.pendingSuggestions = merged;
        this.hspemoji$emojiFuture = merged;
    }
}
