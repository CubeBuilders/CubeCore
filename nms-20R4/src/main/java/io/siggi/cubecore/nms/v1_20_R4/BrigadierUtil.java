package io.siggi.cubecore.nms.v1_20_R4;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;

public class BrigadierUtil extends io.siggi.cubecore.nms.BrigadierUtil {

    @Override
    public ArgumentType argumentTypeChat() {
        return MessageArgument.message();
    }

    @Override
    public SuggestionProvider suggestionProviderAskServer() {
        return SuggestionProviders.ASK_SERVER;
    }
}
