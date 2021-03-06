package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.common.util.StringUtils;

import java.util.Objects;

public final class StringMatchUtils {
    private StringMatchUtils() {
    }

    public static int indexOf(String str, char[] chars) {
        Objects.requireNonNull(str, "str");
        Objects.requireNonNull(chars, "chars");

        for (int i = 0; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (contains(c, chars)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean contains(char c, char[] validChars) {
        Objects.requireNonNull(validChars, "validChars");

        for (char validCh : validChars) {
            if (validCh == c) {
                return true;
            }
        }
        return false;
    }

    public static int endsWithCountMatches(String str, String postfix) {
        Objects.requireNonNull(str, "str");
        if (StringUtils.isEmpty(postfix)) {
            return 0;
        }

        final int postFixLength = postfix.length();

        int count = 0;
        final int lastOffset = str.length() - postFixLength;
        final int length = str.length();
        for (int i = lastOffset; i < length; i -= postFixLength) {
            final boolean found = str.startsWith(postfix, i);
            if (!found) {
                break;
            }
            count++;
        }

        return count;
    }

    public static int startsWithCountMatches(String str, char prefix) {
        if (StringUtils.isEmpty(str)) {
            return 0;
        }

        int count = 0;
        final int length = str.length();
        for (int i = 0; i < length; i++) {
            final char c = str.charAt(i);
            if (c != prefix) {
                break;
            }
            count++;
        }

        return count;
    }

    /**
     * ExperimentalApi
     */
    static void appendAndReplace(String str, int startOffset, char oldChar, char newChar, StringBuilder output) {
        Objects.requireNonNull(str, "str");
        Objects.requireNonNull(output, "output");

        for (int i = startOffset; i < str.length(); i++) {
            final char c = str.charAt(i);
            if (c == oldChar) {
                output.append(newChar);
            } else {
                output.append(c);
            }
        }
    }

    /**
     * ExperimentalApi
     */
    public static boolean startWith(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.startsWith(str2);
    }

    /**
     * ExperimentalApi
     */

    public static boolean equals(String str1, String str2) {
        if (str1 == null) {
            return false;
        }
        return str1.equals(str2);
    }

}
