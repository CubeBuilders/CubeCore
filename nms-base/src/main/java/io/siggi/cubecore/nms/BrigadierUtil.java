package io.siggi.cubecore.nms;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

public abstract class BrigadierUtil {

    public abstract ArgumentType argumentTypeChat();

    public abstract SuggestionProvider suggestionProviderAskServer();
}
