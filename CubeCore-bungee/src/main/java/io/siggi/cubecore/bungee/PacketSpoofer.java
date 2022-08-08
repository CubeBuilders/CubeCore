package io.siggi.cubecore.bungee;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import java.util.Collections;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.CancelSendSignal;
import net.md_5.bungee.connection.UpstreamBridge;
import net.md_5.bungee.protocol.ChatChain;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.Chat;
import net.md_5.bungee.protocol.packet.ClientChat;
import net.md_5.bungee.protocol.packet.ClientCommand;

class PacketSpoofer {
    static void chatAsPlayer(ProxiedPlayer p, String message) {
        UserConnection connection = (UserConnection) p;
        DefinedPacket chatPacket;
        int protocolVersion = connection.getPendingConnection().getVersion();
        if (protocolVersion >= ProtocolConstants.MINECRAFT_1_19) {
            if (message.startsWith("/")) {
                message = message.substring(1);
                chatPacket = new ClientCommand();
            } else {
                chatPacket = new ClientChat();
            }
        } else {
            chatPacket = new Chat();
        }
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        DefinedPacket.writeString(message, buffer);
        buffer.writeLong(System.currentTimeMillis());
        for (int i = 0; i < 16; i++) {
            buffer.writeLong(0L);
        }
        chatPacket.read(buffer, ProtocolConstants.Direction.TO_SERVER, protocolVersion);
        injectPacketFromClient(p, chatPacket);
        buffer.release();
    }

    private static void injectPacketFromClient(ProxiedPlayer player, DefinedPacket packet) {
        UserConnection connection = (UserConnection) player;
        try {
            UpstreamBridge upstreamBridge = ReflectionUtil.getUpstreamBridge(player);
            packet.handle(upstreamBridge);
            connection.getServer().unsafe().sendPacket(packet);
        } catch (CancelSendSignal | Exception signal) {
        }
    }
}
