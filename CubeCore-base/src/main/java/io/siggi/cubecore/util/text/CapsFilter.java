package io.siggi.cubecore.util.text;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class CapsFilter implements TextFilter {

    private final Consumer<Consumer<String>> maintainCapsFunction;

    public CapsFilter() {
        this((Consumer<Consumer<String>>) null);
    }

    public CapsFilter(Consumer<Consumer<String>> maintainCapsFunction) {
        this.maintainCapsFunction = maintainCapsFunction;
    }

    public CapsFilter(List<String> maintainCapsList) {
        this((maintainCaps) -> {
            for (String item : maintainCapsList) {
                maintainCaps.accept(item);
            }
        });
    }

    public String filter(String text) {
        List<OriginalStringInfo> maintainCaps = maintainCaps(text);

        boolean beginSentence = true;
        boolean insideWord = false;
        char[] c = text.toCharArray();
        char originalCharacter;
        char newCharacter;
        for (int i = 0; i < c.length; i++) {
            originalCharacter = newCharacter = c[i];
            if (Character.isUpperCase(originalCharacter)) {
                if (insideWord) {
                    newCharacter = Character.toLowerCase(originalCharacter);
                }
                beginSentence = false;
                insideWord = true;
            } else if (Character.isLowerCase(originalCharacter)) {
                if (beginSentence) {
                    newCharacter = Character.toUpperCase(originalCharacter);
                }
                beginSentence = false;
                insideWord = true;
            } else { // Space
                insideWord = false;
            }
            if (originalCharacter == '.' || originalCharacter == '?' || originalCharacter == '!') { // End Sentence
                beginSentence = true;
            }
            if (newCharacter != originalCharacter) {
                c[i] = newCharacter;
            }
        }

        return revertCaps(maintainCaps, new String(c));
    }

    private List<OriginalStringInfo> maintainCaps(String text) {
        List<OriginalStringInfo> info = new LinkedList<>();

        if (maintainCapsFunction != null) {
            maintainCapsFunction.accept((string) -> maintainCaps(text, string, info));
        }

        return info;
    }

    private void maintainCaps(String haystack, String needle, List<OriginalStringInfo> currentList) {
        int pos = 0;
        int haystackLength = haystack.length();
        int needleLength = needle.length();
        int end = haystackLength - needleLength;
        while ((pos = haystack.indexOf(needle, pos)) != -1) {
            if ((pos == 0 || isWordBorderCharacter(haystack.charAt(pos - 1))) && (pos == end || isWordBorderCharacter(haystack.charAt(pos + needleLength)))) {
                currentList.add(new OriginalStringInfo(needle, pos));
            }
            pos += needleLength;
        }
    }

    private String revertCaps(List<OriginalStringInfo> ranges, String text) {
        for (OriginalStringInfo range : ranges) {
            text = text.substring(0, range.getStart()) + range.getOriginal() + text.substring(range.getEnd());
        }
        return text;
    }

    private boolean isWordBorderCharacter(char c) {
        return c == ' ' || c == '.' || c == ',' || c == '?' || c == '!'
            || c == '/' || c == '\\' || c == '|'
            || c == ':' || c == ';'
            || c == '"' || c == '\''
            || c == '<' || c == '>' || c == '[' || c == ']'
            || c == '{' || c == '}' || c == '(' || c == ')';
    }

    private class OriginalStringInfo {

        private final String original;
        private final int offset;

        public OriginalStringInfo(String original, int offset) {
            this.original = original;
            this.offset = offset;
        }

        public String getOriginal() {
            return original;
        }

        public int getStart() {
            return offset;
        }

        public int getEnd() {
            return offset + original.length();
        }
    }
}
