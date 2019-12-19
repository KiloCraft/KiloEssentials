package org.kilocraft.essentials.servermeta;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.server.ServerMetadata;

public class ServerMetaManager {
    private ServerMetadata metadata;
    private PlayerListHeaderS2CPacket playerListPacket;
    private String playerListHeader;
    private String playerListFooter;

    public ServerMetaManager(ServerMetadata metadata) {
        this.metadata = metadata;
    }

    public void provide() {
//        PlayerListHeaderS2CPacket packet = new PlayerListHeaderS2CPacket();

//        ((ModifiablePlayerListMeta) playerListPacket).setHeader(new LiteralText("Test HEADER"));
//
//        ((ModifiablePlayerListMeta) playerListPacket).setFooter(new LiteralText("Test2"));
//
//        try {
//            packet.write(new PacketByteBuf(Unpooled.buffer()).writeString("{\"text\":\"This is stupid\"}"));
//            KiloServer.getServer().getPlayerManager().sendToAll(packet);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    public void setPlayerListHeader(String string) {
        this.playerListHeader = string;
    }

    public void setPlayerListFooter(String string) {
        this.playerListFooter = string;
    }

    public String getPlayerListHeader() {
        return this.playerListHeader;
    }

    public String getPlayerListFooter() {
        return this.playerListFooter;
    }
}
