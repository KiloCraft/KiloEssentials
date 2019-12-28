package org.kilocraft.essentials.servermeta;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.server.ModifiablePlayerListMeta;

public class ServerMetaManager {
    private ServerMetadata metadata;
    private PlayerListHeaderS2CPacket playerListPacket;
    private String playerListHeader;
    private String playerListFooter;

    public ServerMetaManager(ServerMetadata metadata) {
        this.metadata = metadata;
    }

    public void provide() {
        this.playerListPacket = new PlayerListHeaderS2CPacket();

        ((ModifiablePlayerListMeta) this.playerListPacket).setHeader(new LiteralText("This is the Header"));

        ((ModifiablePlayerListMeta) this.playerListPacket).setFooter(new LiteralText("Hi Header i'm footer"));

//        try {
//            playerListPacket.write(new PacketByteBuf(Unpooled.buffer()).writeText(new LiteralText("HELLO THERE").formatted(Formatting.OBFUSCATED)));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        KiloServer.getServer().sendGlobalPacket(this.playerListPacket);
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
