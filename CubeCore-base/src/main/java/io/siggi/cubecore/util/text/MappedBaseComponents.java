package io.siggi.cubecore.util.text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class MappedBaseComponents {
    private final List<BaseComponent> chatLine;
    private final int length;
    private final Map<Integer, StringRange> ranges;
    private final Map<Integer, TextComponent> components;

    public MappedBaseComponents(List<BaseComponent> chatLine) {
        this.chatLine = chatLine;
        ranges = new HashMap<>();
        components = new HashMap<>();
        int i = -1;
        int position = 0;
        for (BaseComponent chatComponent : chatLine) {
            i += 1;
            if (!(chatComponent instanceof TextComponent)) {
                continue;
            }
            TextComponent textComponent = (TextComponent) chatComponent;
            String text = textComponent.getText();
            ranges.put(i, new StringRange(position, position + text.length()));
            components.put(i, textComponent);
            position += text.length();
        }
        length = position;
    }

    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (BaseComponent chatComponent : chatLine) {
            if (!(chatComponent instanceof TextComponent)) {
                continue;
            }
            sb.append(((TextComponent) chatComponent).getText());
        }
        return sb.toString();
    }

    public void setText(String text) {
        if (text.length() != length) {
            throw new IllegalArgumentException("The new text must have the same length as the old text!");
        }
        for (Map.Entry<Integer, StringRange> entry : ranges.entrySet()) {
            TextComponent component = components.get(entry.getKey());
            StringRange range = entry.getValue();
            component.setText(text.substring(range.start, range.end));
        }
    }
}
