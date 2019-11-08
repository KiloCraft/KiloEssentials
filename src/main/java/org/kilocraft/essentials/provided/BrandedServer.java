package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModData;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.config.KiloConifg;

public  class BrandedServer {
    public static void provide() {
        String configBrand = KiloConifg.getProvider().getMain().get(true, "server.displayBrandName");
        boolean useDefault = false;
        if (configBrand.equals("DEFAULT")) useDefault = true;

        KiloServer.getServer().setDisplayBrandName(
                useDefault ? KiloServer.getServer().getBrandName() :
                        TextFormat.translateAlternateColorCodes(
                                '&',
                                String.format(
                                        ModData.getProperties().getProperty("server.brand.custom"),
                                        "&r" + configBrand + "&r",
                                        ModData.getVersion()
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
