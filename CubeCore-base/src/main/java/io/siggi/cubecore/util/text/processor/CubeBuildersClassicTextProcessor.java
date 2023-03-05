package io.siggi.cubecore.util.text.processor;

import net.md_5.bungee.api.ChatColor;

public class CubeBuildersClassicTextProcessor extends SimpleColorCodeTextProcessor {

    private final LinkFixer linkFixer;
    private final LinkLabeler linkLabeler;
    private final SimpleColorCodeTextProcessor colorOnlyProcessor = new SimpleColorCodeTextProcessor();
    private boolean linksAllowed = true;
    private boolean allowCustomTooltip = false;
    private boolean linksIncludeTooltip = true;

    public void setLinksAllowed(boolean linksAllowed) {
        this.linksAllowed = linksAllowed;
    }

    public void setAllowCustomTooltip(boolean allowCustomTooltip) {
        this.allowCustomTooltip = allowCustomTooltip;
    }

    public void setLinksIncludeTooltip(boolean linksIncludeTooltip) {
        this.linksIncludeTooltip = linksIncludeTooltip;
    }

    public void setAllowColors(boolean allowColors) {
        super.setAllowColors(allowColors);
        colorOnlyProcessor.setAllowColors(allowColors);
    }

    public void setAllowHexColors(boolean allowHexColors) {
        super.setAllowHexColors(allowHexColors);
        colorOnlyProcessor.setAllowHexColors(allowHexColors);
    }

    public void setAllowBold(boolean allowBold) {
        super.setAllowBold(allowBold);
        colorOnlyProcessor.setAllowBold(allowBold);
    }

    public void setAllowItalic(boolean allowItalic) {
        super.setAllowItalic(allowItalic);
        colorOnlyProcessor.setAllowItalic(allowItalic);
    }

    public void setAllowUnderline(boolean allowUnderline) {
        super.setAllowUnderline(allowUnderline);
        colorOnlyProcessor.setAllowUnderline(allowUnderline);
    }

    public void setAllowStrike(boolean allowStrike) {
        super.setAllowStrike(allowStrike);
        colorOnlyProcessor.setAllowStrike(allowStrike);
    }

    public void setAllowMagic(boolean allowMagic) {
        super.setAllowMagic(allowMagic);
        colorOnlyProcessor.setAllowMagic(allowMagic);
    }

    public CubeBuildersClassicTextProcessor(LinkFixer linkFixer, LinkLabeler linkLabeler) {
        this(linkFixer, linkLabeler, ChatColor.AQUA, null);
    }

    public CubeBuildersClassicTextProcessor(LinkFixer linkFixer, LinkLabeler linkLabeler, ChatColor linkColor, ChatColor linkFallbackColor) {
        super();
        this.linkFixer = linkFixer;
        this.linkLabeler = linkLabeler;

        // The original CubeBuilders link format
        // also happens to support Markdown links in angled brackets
        registerHandler('<', (formattedText, reader, currentPiece, defaultColor, defaultChatColor) -> {
            if (!linksAllowed) {
                currentPiece.end = reader.getPosition();
                return;
            }
            String rawText = reader.getText();
            int start = reader.getPosition();
            int end = rawText.indexOf(">", start);
            if (end == -1) {
                currentPiece.end = reader.getPosition();
                return;
            }
            int textStart = -1;
            int textEnd = -1;
            String link = rawText.substring(start, end);
            String linkText = null;
            int newPosition = end + 1;
            if (rawText.length() > end + 2 && rawText.charAt(end + 1) == '<') {
                int possibleTextStart = end + 2;
                int possibleTextEnd = rawText.indexOf(">", possibleTextStart);
                if (possibleTextEnd != -1) {
                    textStart = possibleTextStart;
                    textEnd = possibleTextEnd;
                    linkText = rawText.substring(textStart, textEnd);
                    newPosition = possibleTextEnd + 1;
                }
            }

            if (!isLinkValid(linkFixer, link)) {
                currentPiece.end = reader.getPosition();
                return;
            }

            insertLink(
                linkFixer, linkLabeler,
                formattedText, currentPiece,
                linkColor, linkFallbackColor,
                rawText,
                link, start, end,
                linkText, textStart, textEnd,
                colorOnlyProcessor, linksIncludeTooltip, null
            );

            reader.setPosition(newPosition);
            formattedText.pieces.add(currentPiece.createBlankWithSameFormat(newPosition));
        });

        // Markdown link format
        registerHandler('[', (formattedText, reader, currentPiece, defaultColor, defaultChatColor) -> {
            if (!linksAllowed) {
                currentPiece.end = reader.getPosition();
                return;
            }
            String rawText = reader.getText();
            int start = reader.getPosition();
            int end = rawText.indexOf("](", start);
            if (end == -1) {
                currentPiece.end = reader.getPosition();
                return;
            }
            int linkStart = end + 2;
            int linkEnd = rawText.indexOf(")", linkStart);
            if (linkEnd == -1) {
                currentPiece.end = reader.getPosition();
                return;
            }
            int newPosition = linkEnd + 1;
            String linkText = rawText.substring(start, end);
            String link = rawText.substring(linkStart, linkEnd);

            if (!isLinkValid(linkFixer, link)) {
                currentPiece.end = reader.getPosition();
                return;
            }

            String customTooltip = null;
            if (allowCustomTooltip && rawText.length() > newPosition && rawText.charAt(newPosition) == '<') {
                int tooltipStart = newPosition + 1;
                int tooltipEnd = rawText.indexOf(">", tooltipStart);
                if (tooltipEnd != -1) {
                    newPosition = tooltipEnd + 1;
                    customTooltip = rawText.substring(tooltipStart, tooltipEnd);
                }
            }

            insertLink(
                linkFixer, linkLabeler,
                formattedText, currentPiece,
                linkColor, linkFallbackColor,
                rawText,
                link, linkStart, linkEnd,
                linkText, start, end,
                colorOnlyProcessor, linksIncludeTooltip, customTooltip
            );

            reader.setPosition(newPosition);
            formattedText.pieces.add(currentPiece.createBlankWithSameFormat(newPosition));
        });
    }
}
