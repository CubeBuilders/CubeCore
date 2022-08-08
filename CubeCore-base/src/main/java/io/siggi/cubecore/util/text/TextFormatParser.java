package io.siggi.cubecore.util.text;

import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;

public interface TextFormatParser {
    List<BaseComponent> parse(String message);
}
