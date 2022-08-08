package io.siggi.cubecore.nms.v1_16_R3;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.v1_16_R3.ArgumentChat;
import net.minecraft.server.v1_16_R3.CompletionProviders;

public class BrigadierUtil extends io.siggi.cubecore.nms.BrigadierUtil {

    @Override
    public ArgumentType argumentTypeChat() {
        return ArgumentChat.a();
    }

    @Override
    public SuggestionProvider suggestionProviderAskServer() {
        return CompletionProviders.a;
    }
}
