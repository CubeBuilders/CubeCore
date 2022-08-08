package io.siggi.cubecore.bungee.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface BungeeBrigadierArgumentProvider {
    LiteralCommandNode provideArguments(ProxiedPlayer player, LiteralArgumentBuilder<?> node, int protocolVersion);
}
