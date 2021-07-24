package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;

public class BrandedServer {

    public static void update() {
        KiloEssentials.getInstance().sendGlobalPacket(toPacket());
    }

    public static void provide(ServerPlayerEntity player) {
        player.networkHandler.sendPacket(toPacket());
    }

    public static String getFinalBrandName() {
        String configBrand = KiloConfig.main().server().displayBrandName;
        boolean useDefault = configBrand.equals("default");

        return ComponentText.translateOld(useDefault ? ModConstants.getProperties().getProperty("server.brand") :
                String.format(ModConstants.getProperties().getProperty("server.brand.custom"), configBrand + "&r"));
    }

    private static CustomPayloadS2CPacket toPacket() {
        return new CustomPayloadS2CPacket(
                CustomPayloadS2CPacket.BRAND,
                (new PacketByteBuf(Unpooled.buffer())).writeString(getFinalBrandName())
        );
    }

}
