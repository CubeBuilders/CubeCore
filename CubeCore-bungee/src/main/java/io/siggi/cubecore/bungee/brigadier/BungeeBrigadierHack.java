package io.siggi.cubecore.bungee.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.Commands;

public class BungeeBrigadierHack {
    private BungeeBrigadierHack() {
    }

    static ArgumentType<?> createArgumentType(int protocolVersion, Object argumentType, Object... arguments) {
        try {
            Commands commands = createCommands(protocolVersion, argumentType, arguments);
            ArgumentCommandNode node = (ArgumentCommandNode) commands.getRoot().getChild("say").getChildren().iterator().next();
            return node.getType();
        } catch (Exception e) {
            return null;
        }
    }

    private static Commands createCommands(int protocolVersion, Object argumentType, Object... arguments) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(1000);
        buffer.writerIndex(0);

        // number of nodes
        DefinedPacket.writeVarInt(3, buffer);

        // 0: Root
        // flags
        buffer.writeByte(0x0); // root
        // children
        DefinedPacket.writeVarInt(1, buffer); // array length
        DefinedPacket.writeVarInt(1, buffer); // array index 0: idx 1

        // 1: Literal "say"
        // flags
        buffer.writeByte(0x1); // literal
        // children
        DefinedPacket.writeVarInt(1, buffer); // array length
        DefinedPacket.writeVarInt(2, buffer); // array index 0: idx 2
        // literal string
        DefinedPacket.writeString("say", buffer);

        // 2: Argument "message"
        // flags
        buffer.writeByte(0x2 | 0x4); // argument | executable
        // children
        DefinedPacket.writeVarInt(0, buffer); // array length
        // argument name
        DefinedPacket.writeString("message", buffer);
        // argument type
        if (argumentType instanceof String) {
            DefinedPacket.writeString((String) argumentType, buffer);
        } else if (argumentType instanceof Integer) {
            DefinedPacket.writeVarInt((Integer) argumentType, buffer);
        }
        if (arguments != null) {
            for (Object argument : arguments) {
                if (argument instanceof Boolean) {
                    buffer.writeBoolean((Boolean) argument);
                } else if (argument instanceof Byte) {
                    buffer.writeByte((Byte) argument);
                } else if (argument instanceof Integer) {
                    buffer.writeInt((Integer) argument);
                } else if (argument instanceof Long) {
                    buffer.writeLong((Long) argument);
                } else if (argument instanceof Float) {
                    buffer.writeFloat((Float) argument);
                } else if (argument instanceof Double) {
                    buffer.writeDouble((Double) argument);
                } else if (argument instanceof String) {
                    DefinedPacket.writeString((String) argument, buffer);
                }
            }
        }

        // indicate which is root
        DefinedPacket.writeVarInt(0, buffer);

        buffer.readerIndex(0);

        Commands commands = new Commands();
        commands.read(buffer, null, protocolVersion);
        return commands;
    }
}
