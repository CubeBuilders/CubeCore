package io.siggi.cubecore.util.text.processor;

import io.siggi.cubecore.util.text.FormattedText;
import io.siggi.cubecore.util.text.TextPiece;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;

public abstract class AbstractSimpleTextProcessor extends TextProcessor {

    private final Handler[][] handlers = new Handler[256][];
    private final Set<String> trustedProtocols = new HashSet<>();

    public AbstractSimpleTextProcessor() {
        addTrustedProtocol("http");
        addTrustedProtocol("https");
        addTrustedProtocol("copy");
        addTrustedProtocol("command");
        addTrustedProtocol("suggest");
        addTrustedProtocol("page");
        addTrustedProtocol("notalink");
    }

    protected void registerHandler(char c, Handler handler) {
        if (c == '\\') {
            throw new IllegalArgumentException("Cannot register a handler to the escape character.");
        }
        int x = (c >> 8) & 0xff;
        int y = c & 0xff;
        if (handlers[x] == null) {
            handlers[x] = new Handler[256];
        }
        handlers[x][y] = handler;
    }

    protected Handler getHandler(char c) {
        int x = (c >> 8) & 0xff;
        int y = c & 0xff;
        if (handlers[x] == null) {
            return null;
        }
        return handlers[x][y];
    }

    @Override
    protected FormattedText doProcess(String text, int startFrom, TextPiece startingPiece, ChatColor defaultColor, ChatColor defaultFallbackColor) {
        FormattedText formattedText = new FormattedText();
        formattedText.rawText = text;
        TextPiece currentPiece;
        if (startingPiece == null) {
            currentPiece = new TextPiece();
            currentPiece.start = currentPiece.end = startFrom;
        } else {
            currentPiece = startingPiece.createBlankWithSameFormat(startFrom);
        }
        formattedText.pieces.add(currentPiece);
        currentPiece.color = defaultColor;
        currentPiece.fallbackColor = defaultFallbackColor;
        StringReader reader = new StringReader(text);
        reader.setPosition(startFrom);
        while (reader.hasNext()) {
            currentPiece = formattedText.pieces.get(formattedText.pieces.size() - 1);
            char c = reader.next();
            if (c == '\\' && reader.hasNext()) {
                char peek = reader.peek();
                if (peek == '\\' || getHandler(peek) != null) {
                    TextPiece newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    formattedText.pieces.add(newPiece);
                    newPiece.end += 1;
                    reader.forward(1);
                } else {
                    currentPiece.end = reader.getPosition();
                }
                continue;
            }
            Handler handler = getHandler(c);
            if (handler == null) {
                currentPiece.end = reader.getPosition();
                continue;
            }
            handler.handle(formattedText, reader, currentPiece, defaultColor, defaultFallbackColor);
        }
        formattedText.pieces.removeIf(p -> p.start == p.end);
        return formattedText;
    }

    protected void insertLink(
        LinkFixer linkFixer, LinkLabeler linkLabeler,
        FormattedText formattedText, TextPiece currentPiece,
        ChatColor linkColor, ChatColor linkFallbackColor,
        String rawText,
        String link, int start, int end,
        String linkText, int textStart, int textEnd,
        TextProcessor linkTextProcessor, boolean linksIncludeTooltip, String customTooltip
    ) {
        String originalLink = link;
        String newLink = linkFixer == null ? null : linkFixer.fixLink(link);
        if (linkText == null) {
            linkText = linkLabeler == null ? null : linkLabeler.getLabel(link);
            if (linkText == null) {
                linkText = link;
            }
        }
        if (newLink != null) link = newLink;
        if (link.startsWith("/")) {
            if (link.endsWith("...")) {
                link = "suggest:" + link.substring(0, link.length() - 3);
            } else {
                link = "command:" + link;
            }
        }
        List<TextPiece> newPieces = new ArrayList<>();
        TextPiece newPiece = currentPiece.createBlankWithSameFormat(0);
        newPieces.add(newPiece);
        newPiece.color = linkColor;
        newPiece.fallbackColor = linkFallbackColor;
        if (textStart != -1 && rawText.substring(textStart, textEnd).equals(linkText)) {
            if (linkTextProcessor == null) {
                newPiece.start = textStart;
                newPiece.end = textEnd;
            } else {
                FormattedText processed = linkTextProcessor.doProcess(rawText.substring(0, textEnd), textStart, currentPiece, linkColor, linkFallbackColor);
                newPieces.clear();
                newPieces.addAll(processed.pieces);
            }
        } else if (rawText.substring(start, end).equals(linkText)) {
            newPiece.start = start;
            newPiece.end = end;
        } else {
            newPiece.start = newPiece.end = start;
            newPiece.literal = linkText;
        }
        FormattedText tooltip = null;
        if (customTooltip != null) {
            tooltip = (linkTextProcessor == null ? NullTextProcessor.instance : linkTextProcessor)
                .process(customTooltip, null, null);
        } else if (linksIncludeTooltip) {
            String tooltipText = null;
            if (link.startsWith("copy:")) {
                tooltipText = "Copy to clipboard: " + link.substring(5);
            } else if (link.startsWith("command:")) {
                tooltipText = "Run command immediately: " + link.substring(8);
            } else if (link.startsWith("suggest:")) {
                tooltipText = "Use command template: " + link.substring(8);
            } else if (link.startsWith("page:")) {
                tooltipText = "Go to page " + link.substring(5);
            } else if (linkText.contains(originalLink)) {
                tooltipText = "Click to open this link";
            } else {
                tooltipText = link;
            }
            tooltip = NullTextProcessor.instance.process(tooltipText, null, null);
        }
        if (tooltip != null) {
            for (TextPiece piece : newPieces) {
                piece.tooltip = tooltip;
                if (!link.toLowerCase(Locale.ROOT).startsWith("notalink:")) {
                    piece.link = link;
                }
            }
        }
        formattedText.pieces.addAll(newPieces);
    }

    protected boolean isLinkValid(LinkFixer linkFixer, String link) {
        if (link.startsWith("/")) return true;
        if (linkFixer != null) link = linkFixer.fixLink(link);
        int colon = link.indexOf(":");
        if (colon == -1) return false;
        String protocol = link.substring(0, colon);
        return isProtocolTrusted(protocol);
    }

    public void addTrustedProtocol(String protocol) {
        trustedProtocols.add(protocol.toLowerCase(Locale.ROOT));
    }

    public boolean isProtocolTrusted(String protocol) {
        return trustedProtocols.contains(protocol.toLowerCase(Locale.ROOT));
    }

    @FunctionalInterface
    protected interface Handler {
        void handle(FormattedText formattedText, StringReader reader, TextPiece currentPiece, ChatColor defaultColor, ChatColor defaultChatColor);
    }

    @FunctionalInterface
    public interface LinkFixer {
        String fixLink(String link);
    }

    @FunctionalInterface
    public interface LinkLabeler {
        String getLabel(String link);
    }
}
