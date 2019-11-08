package org.kilocraft.essentials.craft.provider;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.craft.config.KiloConifg;

public class KiloBrandName {
    public static void provide() {
        String configBrand = KiloConifg.getProvider().getMain().get(false, "server.displayBrandName");
        boolean useDefault = false;
        if (configBrand.equals("DEFAULT")) useDefault = true;

        KiloServer.getServer().setDisplayBrandName(
                useDefault ?
                        KiloServer.getServer().getBrandName() :
                        TextFormat.translateAlternateColorCodes(
                                '&',
                                String.format(
                                        Mod.getProperties().getProperty("server.brand.custom"),
                                        "&r" + configBrand + "&r",
                                        Mod.getVersion()
                                )
                        )
        );

        CustomPayloadS2CPacket customPayloadS2CPacket = new CustomPayloadS2CPacket(
                CustomPayloadS2CPacket.BRAND,
                (new PacketByteBuf(Unpooled.buffer())).writeString(KiloServer.getServer().getDisplayBrandName())
        );
        KiloServer.getServer().getPlayerManager().sendToAll(customPayloadS2CPacket);
    }
}
