package io.siggi.cubecore.util.text.processor;

import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.TextPiece;
import net.md_5.bungee.api.ChatColor;

public class NullTextProcessor extends TextProcessor {
    private NullTextProcessor() {
    }

    public static final NullTextProcessor instance = new NullTextProcessor();

    @Override
    protected FormattedText doProcess(String text, int start, TextPiece startingPiece, ChatColor defaultColor, ChatColor defaultFallbackColor) {
        FormattedText formattedText = new FormattedText();
        formattedText.rawText = text;
        TextPiece piece = new TextPiece();
        piece.start = start;
        piece.end = text.length();
        formattedText.pieces.add(piece);
        return formattedText;
    }
}
