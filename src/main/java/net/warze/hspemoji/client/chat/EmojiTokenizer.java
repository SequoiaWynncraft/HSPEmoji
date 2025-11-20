package net.warze.hspemoji.client.chat;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.warze.hspemoji.client.HspEmojiClient;
import net.warze.hspemoji.client.emoji.EmojiSprite;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public EmojiMessage tokenize(OrderedText orderedText) {
        if (orderedText == null) {
            return EmojiMessage.empty();
        }
        List<EmojiSegment> segments = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        Style[] currentStyle = new Style[] { Style.EMPTY };
        orderedText.accept((index, style, codePoint) -> {
            Style resolved = style == null ? Style.EMPTY : style;
            if (!Objects.equals(resolved, currentStyle[0])) {
                hspemoji$flushBuffer(buffer, currentStyle[0], segments);
                currentStyle[0] = resolved;
            }
            buffer.appendCodePoint(codePoint);
            return true;
        });
        hspemoji$flushBuffer(buffer, currentStyle[0], segments);
        if (segments.isEmpty()) {
            return EmojiMessage.empty();
        }
        return new EmojiMessage(segments);
    }

    private void hspemoji$flushBuffer(StringBuilder buffer, Style style, List<EmojiSegment> segments) {
        if (buffer.isEmpty()) {
            return;
        }
        parseChunk(buffer.toString(), style == null ? Style.EMPTY : style, segments);
        buffer.setLength(0);
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
