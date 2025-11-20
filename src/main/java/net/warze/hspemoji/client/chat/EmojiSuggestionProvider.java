package net.warze.hspemoji.client.chat;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;
import net.warze.hspemoji.client.HspEmojiClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EmojiSuggestionProvider {
    private static final int MAX_RESULTS = 20;

    private EmojiSuggestionProvider() {
    }

    public static Suggestions appendEmojiSuggestions(Suggestions base, SuggestionsBuilder builder) {
        if (builder == null) {
            return base;
        }
        String query = builder.getRemaining();
        if (query == null || query.isBlank()) {
            return base;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith(":")) {
            return base;
        }
        Collection<String> emojiKeys = HspEmojiClient.REGISTRY.keys();
        if (emojiKeys.isEmpty()) {
            return base;
        }
        StringRange range = base.getRange();
        if (range.getStart() == range.getEnd() && !query.isEmpty()) {
            int start = Math.max(0, builder.getInput().length() - query.length());
            int end = builder.getInput().length();
            range = StringRange.between(start, end);
        }
        List<Suggestion> merged = new ArrayList<>(base.getList());
        int added = 0;
        for (String token : emojiKeys) {
            if (!token.startsWith(normalized)) {
                continue;
            }
            merged.add(new Suggestion(range, token, Text.literal("Emoji")));
            if (++added >= MAX_RESULTS) {
                break;
            }
        }
        if (added == 0) {
            return base;
        }

        Map<String, Suggestion> dedup = new LinkedHashMap<>();
        for (Suggestion suggestion : merged) {
            dedup.putIfAbsent(suggestion.getText(), suggestion);
        }
        return new Suggestions(range, new ArrayList<>(dedup.values()));
    }
}
