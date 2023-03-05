package io.siggi.cubecore.util.text;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class TextPiece {
    public static final TypeAdapter<TextPiece> typeAdapter = new TypeAdapter<TextPiece>() {
        @Override
        public TextPiece read(JsonReader reader) throws IOException {
            TextPiece piece = new TextPiece();
            reader.beginObject();
            while (reader.peek() != JsonToken.END_OBJECT) {
                String s = reader.nextName();
                switch (s) {
                    case "start": {
                        piece.start = reader.nextInt();
                    }
                    break;
                    case "end": {
                        piece.end = reader.nextInt();
                    }
                    break;
                    case "literal": {
                        piece.literal = reader.nextString();
                    }
                    break;
                    case "color": {
                        piece.color = chatColorTypeAdapter.read(reader);
                    }
                    break;
                    case "fallbackColor": {
                        piece.fallbackColor = chatColorTypeAdapter.read(reader);
                    }
                    break;
                    case "bold": {
                        piece.bold = reader.nextBoolean();
                    }
                    break;
                    case "italic": {
                        piece.italic = reader.nextBoolean();
                    }
                    break;
                    case "underline": {
                        piece.underline = reader.nextBoolean();
                    }
                    break;
                    case "strike": {
                        piece.strike = reader.nextBoolean();
                    }
                    break;
                    case "magic": {
                        piece.magic = reader.nextBoolean();
                    }
                    break;
                    case "link": {
                        piece.link = reader.nextString();
                    }
                    break;
                    case "tooltip": {
                        piece.tooltip = FormattedText.typeAdapter.read(reader);
                    }
                    default:
                        reader.skipValue();
                }
            }
            reader.endObject();
            return piece;
        }

        @Override
        public void write(JsonWriter writer, TextPiece piece) throws IOException {
            if (piece == null) {
                writer.nullValue();
                return;
            }
            writer.beginObject();
            writer.name("start").value(piece.start);
            writer.name("end").value(piece.end);
            writer.name("literal").value(piece.literal);
            writer.name("color");
            chatColorTypeAdapter.write(writer, piece.color);
            writer.name("fallbackColor");
            chatColorTypeAdapter.write(writer, piece.fallbackColor);
            if (piece.bold)
                writer.name("bold").value(piece.bold);
            if (piece.italic)
                writer.name("italic").value(piece.italic);
            if (piece.underline)
                writer.name("underline").value(piece.underline);
            if (piece.strike)
                writer.name("strike").value(piece.strike);
            if (piece.magic)
                writer.name("magic").value(piece.magic);
            writer.name("link").value(piece.link);
            writer.name("tooltip");
            FormattedText.typeAdapter.write(writer, piece.tooltip);
            writer.endObject();
        }
    };
    public static final TypeAdapter<ChatColor> chatColorTypeAdapter = new TypeAdapter<ChatColor>() {
        @Override
        public ChatColor read(JsonReader reader) throws IOException {
            String value = reader.nextString();
            if (value == null) return null;
            try {
                return ChatColor.of(value.startsWith("#") ? value.toLowerCase(Locale.ROOT) : value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public void write(JsonWriter writer, ChatColor color) throws IOException {
            if (color == null) {
                writer.nullValue();
            } else {
                writer.value(color.name().toLowerCase(Locale.ROOT));
            }
        }
    };

    public TextPiece() {
    }

    public TextPiece(
        int start, int end,
        String literal,
        ChatColor color, ChatColor fallbackColor,
        boolean bold, boolean italic, boolean underline,
        boolean strike, boolean magic,
        String link, FormattedText tooltip
    ) {
        this.start = start;
        this.end = end;
        this.literal = literal;
        this.color = color;
        this.fallbackColor = fallbackColor;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
        this.strike = strike;
        this.magic = magic;
        this.link = link;
        this.tooltip = tooltip;
    }

    public int start;
    public int end;
    public String literal;
    public ChatColor color;
    public ChatColor fallbackColor;
    public boolean bold;
    public boolean italic;
    public boolean underline;
    public boolean strike;
    public boolean magic;
    public String link;
    public FormattedText tooltip;

    public String getText(String rawText) {
        return literal == null ? rawText.substring(start, end) : literal;
    }

    public TextComponent toTextComponent(String rawText, boolean useFallbackColor) {
        TextComponent component = new TextComponent(getText(rawText));
        if (useFallbackColor && fallbackColor != null) {
            component.setColor(fallbackColor);
        } else if (color != null) {
            component.setColor(color);
        }
        component.setBold(bold);
        component.setItalic(italic);
        component.setUnderlined(underline);
        component.setStrikethrough(strike);
        component.setObfuscated(magic);
        link:
        if (link != null) {
            int colonPosition = link.indexOf(":");
            if (colonPosition == -1) break link;
            String protocol = link.substring(0, colonPosition);
            String linkPayload = link.substring(colonPosition + 1);
            switch (protocol) {
                case "copy":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, linkPayload));
                    break;
                case "page":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, linkPayload));
                    break;
                case "suggest":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, linkPayload));
                    break;
                case "command":
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, linkPayload));
                    break;
                default:
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, link));
                    break;
            }
        }
        if (tooltip != null) {
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(tooltip.toTextComponents(useFallbackColor).toArray(empty))));
        }
        return component;
    }

    private static final TextComponent[] empty = new TextComponent[0];

    public TextPiece copy() {
        return new TextPiece(
            start, end,
            literal,
            color, fallbackColor,
            bold, italic, underline,
            strike, magic,
            link, tooltip == null ? null : tooltip.copy()
        );
    }
    public TextPiece createBlankWithSameFormat(int initialPosition) {
        return new TextPiece(
            initialPosition, initialPosition,
            null,
            color, fallbackColor,
            bold, italic, underline,
            strike, magic,
            null, null
        );
    }
}
