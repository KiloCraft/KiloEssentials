package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.config.KiloConfig;

public class BrandedServer {
    public static void set() {
        KiloServer.getServer().setDisplayBrandName(getFinalBrandName());
    }

    public static void load() {
        set();
        KiloServer.getServer().sendGlobalPacket(getPacket());
    }

    public static void provide(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(getPacket());
    }

    private static String getFinalBrandName() {
        String configBrand = KiloConfig.getProvider().getMain().getStringSafely("server.displayBrandName", "default");
        boolean useDefault = configBrand.equals("default");

        return TextFormat.translate(useDefault ? ModConstants.getProperties().getProperty("server.brand") :
                String.format(ModConstants.getProperties().getProperty("server.brand.custom"), configBrand + "&r"));
    }

    private static CustomPayloadS2CPacket getPacket() {
        return new CustomPayloadS2CPacket(
                CustomPayloadS2CPacket.BRAND,
                (new PacketByteBuf(Unpooled.buffer())).writeString(getFinalBrandName()));
    }

}
