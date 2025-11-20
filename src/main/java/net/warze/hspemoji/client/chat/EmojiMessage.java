package net.warze.hspemoji.client.chat;

import net.minecraft.text.Text;

import java.util.Collections;
import java.util.List;

public final class EmojiMessage {
    private static final EmojiMessage EMPTY = new EmojiMessage(Collections.singletonList(new EmojiSegment.TextSegment(Text.empty())), false);
    private final List<EmojiSegment> segments;
    private final boolean hasEmoji;

    public EmojiMessage(List<EmojiSegment> segments) {
        this(segments, segments.stream().anyMatch(segment -> segment instanceof EmojiSegment.EmojiEntry));
    }

    private EmojiMessage(List<EmojiSegment> segments, boolean hasEmoji) {
        this.segments = List.copyOf(segments);
        this.hasEmoji = hasEmoji;
    }

    public static EmojiMessage empty() {
        return EMPTY;
    }

    public List<EmojiSegment> segments() {
        return segments;
    }

    public boolean hasEmoji() {
        return hasEmoji;
    }

}
