package net.warze.hspemoji.client.chat;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.warze.hspemoji.client.HspEmojiClient;
import net.warze.hspemoji.client.emoji.EmojiSprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiTokenizer {
    private static final Pattern TOKEN = Pattern.compile(":[a-zA-Z0-9_\\-]+:");

    public EmojiMessage tokenize(Text text) {
        if (text == null) {
            return EmojiMessage.empty();
        }
        List<EmojiSegment> segments = new ArrayList<>();
        text.visit((style, content) -> {
            parseChunk(content, style == null ? Style.EMPTY : style, segments);
            return Optional.empty();
        }, Style.EMPTY);
        return new EmojiMessage(segments);
    }

    private void parseChunk(String chunk, Style style, List<EmojiSegment> segments) {
        Matcher matcher = TOKEN.matcher(chunk);
        int cursor = 0;
        while (matcher.find()) {
            if (matcher.start() > cursor) {
                String literal = chunk.substring(cursor, matcher.start());
                if (!literal.isEmpty()) {
                    segments.add(new EmojiSegment.TextSegment(Text.literal(literal).setStyle(style)));
                }
            }
            String token = matcher.group();
            EmojiSprite sprite = HspEmojiClient.REGISTRY.get(token);
            if (sprite != null) {
                segments.add(new EmojiSegment.EmojiEntry(token, sprite));
            } else {
                segments.add(new EmojiSegment.TextSegment(Text.literal(token).setStyle(style)));
            }
            cursor = matcher.end();
        }
        if (cursor < chunk.length()) {
            segments.add(new EmojiSegment.TextSegment(Text.literal(chunk.substring(cursor)).setStyle(style)));
        }
    }
}
