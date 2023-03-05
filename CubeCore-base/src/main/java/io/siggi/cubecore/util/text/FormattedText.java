package io.siggi.cubecore.util.text;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.TextComponent;

public class FormattedText {
    public static final TypeAdapter<FormattedText> typeAdapter = new TypeAdapter<FormattedText>() {
        @Override
        public FormattedText read(JsonReader reader) throws IOException {
            FormattedText formattedText = new FormattedText();
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String s = reader.nextName();
                switch (s) {
                    case "rawText": {
                        formattedText.rawText = reader.nextString();
                    }
                    break;
                    case "pieces": {
                        formattedText.pieces = new ArrayList<>();
                        reader.beginArray();
                        while (reader.peek() != JsonToken.END_ARRAY) {
                            formattedText.pieces.add(TextPiece.typeAdapter.read(reader));
                        }
                        reader.endArray();
                    }
                    break;
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            return formattedText;
        }

        @Override
        public void write(JsonWriter writer, FormattedText formattedText) throws IOException {
            if (formattedText == null) {
                writer.nullValue();
                return;
            }
            writer.beginObject();
            writer.name("rawText").value(formattedText.rawText);
            writer.name("pieces");
            if (formattedText.pieces == null) {
                writer.nullValue();
            } else {
                writer.beginArray();
                for (TextPiece piece : formattedText.pieces) {
                    TextPiece.typeAdapter.write(writer, piece);
                }
                writer.endArray();
            }
            writer.endObject();
        }
    };

    public String rawText;
    public List<TextPiece> pieces;

    public FormattedText() {
        pieces = new ArrayList<>();
    }

    public FormattedText(String rawText, List<TextPiece> pieces) {
        this.rawText = rawText;
        this.pieces = pieces;
    }

    public List<TextComponent> toTextComponents(boolean useFallbackColor) {
        ArrayList<TextComponent> components = new ArrayList<>(pieces.size());
        for (TextPiece piece : pieces) {
            components.add(piece.toTextComponent(rawText, useFallbackColor));
        }
        return components;
    }

    public String getPlainText() {
        StringBuilder sb = new StringBuilder();
        for (TextPiece piece : pieces) {
            sb.append(piece.getText(rawText));
        }
        return sb.toString();
    }

    public void setPlainText(String newText) {
        try {
            char[] newChars = newText.toCharArray();
            char[] rawChars = rawText.toCharArray();
            int newCharsOffset = 0;
            for (TextPiece piece : pieces) {
                if (piece.literal == null) {
                    int length = piece.end - piece.start;
                    System.arraycopy(newChars, newCharsOffset, rawChars, piece.start, length);
                    newCharsOffset += length;
                } else {
                    int length = piece.literal.length();
                    piece.literal = newText.substring(newCharsOffset, length);
                    newCharsOffset += length;
                }
            }
            if (newCharsOffset != newText.length()) {
                throw new IllegalArgumentException("New text must be same length as old text");
            }
            rawText = new String(rawChars);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("New text must be same length as old text");
        }
    }

    public FormattedText append(FormattedText formattedText) {
        int positionAdjustment = rawText.length();
        rawText += formattedText.rawText;
        if (formattedText.pieces != null) {
            formattedText.pieces.forEach(piece -> {
                TextPiece copy = piece.copy();
                copy.start += positionAdjustment;
                copy.end += positionAdjustment;
                pieces.add(copy);
            });
        }
        return this;
    }

    public FormattedText copy() {
        FormattedText newFormattedText = new FormattedText();
        newFormattedText.rawText = rawText;
        if (pieces != null) {
            newFormattedText.pieces = new ArrayList<>();
            pieces.forEach(piece -> newFormattedText.pieces.add(piece.copy()));
        }
        return newFormattedText;
    }
}
