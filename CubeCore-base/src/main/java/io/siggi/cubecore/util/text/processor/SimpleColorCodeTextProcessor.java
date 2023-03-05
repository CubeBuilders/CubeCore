package io.siggi.cubecore.util.text.processor;

import io.siggi.cubecore.util.text.NearestColorFinder;
import io.siggi.cubecore.util.text.TextPiece;
import net.md_5.bungee.api.ChatColor;

public class SimpleColorCodeTextProcessor extends AbstractSimpleTextProcessor {
    private static final ChatColor[] codes = new ChatColor[256];

    static {
        codes['0'] = ChatColor.BLACK;
        codes['1'] = ChatColor.DARK_BLUE;
        codes['2'] = ChatColor.DARK_GREEN;
        codes['3'] = ChatColor.DARK_AQUA;
        codes['4'] = ChatColor.DARK_RED;
        codes['5'] = ChatColor.DARK_PURPLE;
        codes['6'] = ChatColor.GOLD;
        codes['7'] = ChatColor.GRAY;
        codes['8'] = ChatColor.DARK_GRAY;
        codes['9'] = ChatColor.BLUE;
        codes['a'] = ChatColor.GREEN;
        codes['b'] = ChatColor.AQUA;
        codes['c'] = ChatColor.RED;
        codes['d'] = ChatColor.LIGHT_PURPLE;
        codes['e'] = ChatColor.YELLOW;
        codes['f'] = ChatColor.WHITE;
        codes['k'] = ChatColor.MAGIC;
        codes['l'] = ChatColor.BOLD;
        codes['m'] = ChatColor.STRIKETHROUGH;
        codes['n'] = ChatColor.UNDERLINE;
        codes['o'] = ChatColor.ITALIC;
        codes['r'] = ChatColor.RESET;
    }

    private boolean allowColors = true;
    private boolean allowHexColors = true;
    private boolean allowBold = true;
    private boolean allowItalic = true;
    private boolean allowUnderline = true;
    private boolean allowStrike = true;
    private boolean allowMagic = true;

    public void setAllowColors(boolean allowColors) {
        this.allowColors = allowColors;
    }

    public void setAllowHexColors(boolean allowHexColors) {
        this.allowHexColors = allowHexColors;
    }

    public void setAllowBold(boolean allowBold) {
        this.allowBold = allowBold;
    }

    public void setAllowItalic(boolean allowItalic) {
        this.allowItalic = allowItalic;
    }

    public void setAllowUnderline(boolean allowUnderline) {
        this.allowUnderline = allowUnderline;
    }

    public void setAllowStrike(boolean allowStrike) {
        this.allowStrike = allowStrike;
    }

    public void setAllowMagic(boolean allowMagic) {
        this.allowMagic = allowMagic;
    }

    public SimpleColorCodeTextProcessor() {
        registerHandler('&', (formattedText, reader, currentPiece, defaultColor, defaultChatColor) -> {
            if (!reader.hasNext()) {
                currentPiece.end = reader.getPosition();
                return;
            }
            TextPiece newPiece = null;
            char colorCode = reader.next();
            ChatColor newColor = colorCode >= codes.length ? null : codes[colorCode];
            if (colorCode == '#' && reader.remaining() >= 6) {
                if (allowHexColors) {
                    String hexCode = reader.getText().substring(reader.getPosition(), reader.getPosition() + 6);
                    ChatColor hexColor;
                    ChatColor fallbackColor = null;
                    try {
                        hexColor = ChatColor.of("#" + hexCode);
                    } catch (Exception e) {
                        reader.back(1);
                        currentPiece.end = reader.getPosition();
                        return;
                    }
                    reader.forward(6);
                    if (reader.peek() == '&') {
                        reader.forward(1);
                        char fallbackColorCode = reader.peek();
                        if ((fallbackColorCode >= '0' && fallbackColorCode <= '9') || (fallbackColorCode >= 'a' && fallbackColorCode <= 'f')) {
                            fallbackColor = codes[fallbackColorCode];
                            reader.forward(1);
                        } else {
                            reader.back(1);
                        }
                    }
                    if (fallbackColor == null) {
                        fallbackColor = NearestColorFinder.findNearest(hexColor);
                    }
                    newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    newPiece.color = hexColor;
                    newPiece.fallbackColor = fallbackColor;
                }
            } else if (newColor == ChatColor.BOLD) {
                if (allowBold) {
                    newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    newPiece.bold = true;
                }
            } else if (newColor == ChatColor.ITALIC) {
                newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                if (allowItalic)
                    newPiece.italic = true;
            } else if (newColor == ChatColor.UNDERLINE) {
                if (allowUnderline) {
                    newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    newPiece.underline = true;
                }
            } else if (newColor == ChatColor.STRIKETHROUGH) {
                if (allowStrike) {
                    newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    newPiece.strike = true;
                }
            } else if (newColor == ChatColor.MAGIC) {
                if (allowMagic) {
                    newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                    newPiece.magic = true;
                }
            } else if (newColor == ChatColor.RESET) {
                newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                newPiece.bold = false;
                newPiece.italic = false;
                newPiece.underline = false;
                newPiece.strike = false;
                newPiece.magic = false;
                newPiece.color = defaultColor;
                newPiece.fallbackColor = defaultChatColor;
            } else if (newColor != null) {
                newPiece = currentPiece.createBlankWithSameFormat(reader.getPosition());
                if (allowColors) {
                    newPiece.color = newColor;
                    newPiece.fallbackColor = null;
                }
            }
            if (newPiece == null) {
                reader.back(1);
                currentPiece.end = reader.getPosition();
            } else {
                formattedText.pieces.add(newPiece);
            }
        });
    }
}
