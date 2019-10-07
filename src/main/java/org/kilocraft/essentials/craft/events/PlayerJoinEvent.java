package org.kilocraft.essentials.craft.events;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        event.getServer().getPlayerManager().sendToAll(new LiteralText(event.getPlayer().getName().asFormattedString() + " joined.").formatted(Formatting.GREEN, Formatting.ITALIC));

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString("TEST"));
        event.getPlayer().getServer().getPlayerManager().sendToAll(packet);
    }
}
