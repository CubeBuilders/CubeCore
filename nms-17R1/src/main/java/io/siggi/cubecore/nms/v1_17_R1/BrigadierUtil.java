package io.siggi.cubecore.nms.v1_17_R1;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.arguments.ArgumentChat;
import net.minecraft.commands.synchronization.CompletionProviders;

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
