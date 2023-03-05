package io.siggi.cubecore.util.text.processor;

import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.TextPiece;
import net.md_5.bungee.api.ChatColor;

public abstract class TextProcessor {
    public final FormattedText process(String text, ChatColor defaultColor, ChatColor defaultFallbackColor) {
        FormattedText formattedText = doProcess(text, 0, null, defaultColor, defaultFallbackColor);
        formattedText.pieces.removeIf(p -> p.start == p.end && (p.literal == null || p.literal.isEmpty()));
        return formattedText;
    }

    protected abstract FormattedText doProcess(String text, int start, TextPiece startingPiece, ChatColor defaultColor, ChatColor defaultFallbackColor);

    public static class StringReader {
        private final String text;
        private int position = 0;

        public boolean hasNext() {
            return position < text.length();
        }

        public int getPosition() {
            return position;
        }

        public int remaining() {
            return text.length() - position;
        }

        public char next() {
            if (position >= text.length()) {
                return (char) 0;
            }
            return text.charAt(position++);
        }

        public char peek() {
            if (position >= text.length()) {
                return (char) 0;
            }
            return text.charAt(position);
        }

        public void back(int count) {
            position -= 1;
            if (position < 0) position = 0;
        }

        public void forward(int count) {
            position += count;
            if (position > text.length()) position = text.length();
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getText() {
            return text;
        }

        public StringReader(String text) {
            this.text = text;
        }
    }
}
