package io.siggi.cubecore.bungee.brigadier;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.siggi.cubecore.bungee.CubeCoreBungee;
import io.siggi.interceptbrigadier.InterceptBrigadier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class BungeeBrigadierPatcher {

    private static CubeCoreBungee cubeCorePlugin;
    private static boolean didSetup = false;
    public static void setup(CubeCoreBungee cubeCorePlugin) {
        if (didSetup || cubeCorePlugin == null) return;
        didSetup = true;
        BungeeBrigadierPatcher.cubeCorePlugin = cubeCorePlugin;
        InterceptBrigadier.addInterceptor(BungeeBrigadierPatcher::intercept);
    }

    private static void intercept(ProxiedPlayer player, RootCommandNode<?> rootCommandNode, int protocolVersion) {
        Set<Command> commands = new HashSet<>();
        Collection<Map.Entry<String, Command>> commandMap = cubeCorePlugin.getProxy().getPluginManager().getCommands();
        commandMap.iterator().forEachRemaining(entry -> commands.add(entry.getValue()));

        for (Command command : commands) {
            if (!(command instanceof BungeeBrigadierArgumentProvider)) continue;
            BungeeBrigadierArgumentProvider argumentProvider = (BungeeBrigadierArgumentProvider) command;
            Set<String> names = new HashSet<>();
            names.add(command.getName());
            String[] aliases = command.getAliases();
            if (aliases != null) {
                names.addAll(Arrays.asList(aliases));
            }
            rootCommandNode.getChildren().removeIf(node -> matches(node, names));

            LiteralArgumentBuilder newNodeBuilder = LiteralArgumentBuilder.literal(command.getName());
            LiteralCommandNode newNode = argumentProvider.provideArguments(player, newNodeBuilder, protocolVersion);
            rootCommandNode.addChild(newNode);

            if (aliases != null) {
                for (String alias : aliases) {
                    LiteralArgumentBuilder aliasNodeBuilder = LiteralArgumentBuilder.literal(alias);
                    aliasNodeBuilder.redirect(newNode);
                    LiteralCommandNode aliasNode = aliasNodeBuilder.build();
                    rootCommandNode.addChild(aliasNode);
                }
            }
        }
    }

    private static boolean matches(CommandNode<?> node, Set<String> names) {
        if (!(node instanceof LiteralCommandNode)) return false;
        LiteralCommandNode literal = (LiteralCommandNode) node;
        return names.contains(literal.getLiteral());
    }

}
