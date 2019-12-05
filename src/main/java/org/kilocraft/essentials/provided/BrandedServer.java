package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.config.KiloConfig;

public  class BrandedServer {
    public static void provide() {
        String configBrand = KiloConfig.getProvider().getMain().get(true, "server.displayBrandName");

        KiloServer.getServer().setDisplayBrandName(
                configBrand.equalsIgnoreCase("DEFAULT") ? KiloServer.getServer().getBrandName() :
                        TextFormat.translateAlternateColorCodes(
                                '&',
                                String.format(
                                        ModConstants.getProperties().getProperty("server.brand.custom"),
                                        "&r" + configBrand + "&r",
                                        ModConstants.getVersionInt())));

        CustomPayloadS2CPacket customPayloadS2CPacket = new CustomPayloadS2CPacket(
                CustomPayloadS2CPacket.BRAND,
                (new PacketByteBuf(Unpooled.buffer())).writeString(KiloServer.getServer().getDisplayBrandName()));

        KiloServer.getServer().getPlayerManager().sendToAll(customPayloadS2CPacket);

    }

}
