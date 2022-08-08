package io.siggi.cubecore.util.text;

import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;

public interface TextFilter {
    default List<BaseComponent> filter(List<BaseComponent> chatLine) {
        MappedBaseComponents mapped = new MappedBaseComponents(chatLine);
        mapped.setText(mapped.getText());
        return chatLine;
    }

    String filter(String chatLine);
}
