package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;

public class BrandedServer {

    public static void update() {
        KiloEssentials.getInstance().sendGlobalPacket(toPacket());
    }

    public static void provide(ServerPlayer player) {
        player.connection.send(toPacket());
    }

    public static String getFinalBrandName() {
        String configBrand = KiloConfig.main().server().displayBrandName;
        boolean useDefault = configBrand.equals("default");

        return ComponentText.translateOld(useDefault ? ModConstants.getProperties().getProperty("server.brand") :
                String.format(ModConstants.getProperties().getProperty("server.brand.custom"), configBrand + "&r"));
    }

    private static ClientboundCustomPayloadPacket toPacket() {
        return new ClientboundCustomPayloadPacket(
                ClientboundCustomPayloadPacket.BRAND,
                (new FriendlyByteBuf(Unpooled.buffer())).writeUtf(getFinalBrandName())
        );
    }

}
