package io.siggi.cubecore.util.text;

import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;

public class ParserWrapperWithFilter implements TextFormatParser {

    private final TextFormatParser parser;
    private final TextFilter filter;

    public ParserWrapperWithFilter(TextFormatParser parser, TextFilter filter) {
        this.parser = parser;
        this.filter = filter;
    }

    @Override
    public List<BaseComponent> parse(String message) {
        return filter.filter(parser.parse(message));
    }
}
