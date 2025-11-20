package net.warze.hspemoji.client.update;

import java.util.Objects;

public final class VersionComparator {
    private VersionComparator() {
    }

    public static boolean isNewer(String first, String second) {
        return compare(first, second) > 0;
    }

    public static int compare(String first, String second) {
        String[] left = sanitize(first).split("\\.");
        String[] right = sanitize(second).split("\\.");
        int length = Math.max(left.length, right.length);
        for (int i = 0; i < length; i++) {
            int l = i < left.length ? parseInt(left[i]) : 0;
            int r = i < right.length ? parseInt(right[i]) : 0;
            if (l != r) {
                return Integer.compare(l, r);
            }
        }
        return 0;
    }

    private static String sanitize(String version) {
        return Objects.requireNonNullElse(version, "").trim();
    }

    private static int parseInt(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
