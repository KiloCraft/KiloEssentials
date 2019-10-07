package org.kilocraft.essentials.craft.events;

import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.playerEvents.PlayerEvent$OnConnect;
import org.kilocraft.essentials.craft.player.KiloPlayer;

public class PlayerJoinEvent implements EventHandler<PlayerEvent$OnConnect> {
    @Override
    public void handle(PlayerEvent$OnConnect event) {
        event.getServer().getPlayerManager().sendToAll(new LiteralText(event.getPlayer().getName().asFormattedString() + " joined.").formatted(Formatting.GREEN, Formatting.ITALIC));

        KiloPlayer kiloPlayer = new KiloPlayer(event.getPlayer());
//        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(CustomPayloadS2CPacket.BRAND, (new PacketByteBuf(Unpooled.buffer())).writeString("TEST"));
//        event.getPlayer().getServer().getPlayerManager().sendToAll(packet);

        System.out.println(KiloPlayer.get(event.getPlayer()).getUuid());
        System.out.println(KiloPlayer.get(event.getPlayer()).getName());
        System.out.println(KiloPlayer.get(event.getPlayer()).getNickname());
    }
}
