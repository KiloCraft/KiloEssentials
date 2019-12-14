package org.kilocraft.essentials.provided;

import io.netty.buffer.Unpooled;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PacketByteBuf;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.modsupport.ModSupport;

import java.util.Objects;

public class BrandedServer {
    public static void provide() {
        String configBrand = KiloConfig.getProvider().getMain().get(true, "server.displayBrandName");
        String bungeeBrand = ModConstants.getProperties().getProperty("server.brand.bungee");
        String defaultBrand = String.format(
                ModConstants.getProperties().getProperty("server.brand.custom"),
                "&r" + configBrand + "&r",
                ModConstants.getVersionInt());

        boolean useDefault = configBrand.equalsIgnoreCase("default");
        boolean bungee = Objects.requireNonNull(ModSupport.getMod("bungeecord")).isPresent();

        String finalBrand = useDefault ? (bungee ? bungeeBrand : defaultBrand) : configBrand;

        KiloServer.getServer().setDisplayBrandName(finalBrand);

        CustomPayloadS2CPacket customPayloadS2CPacket = new CustomPayloadS2CPacket(
                CustomPayloadS2CPacket.BRAND,
                (new PacketByteBuf(Unpooled.buffer())).writeString(KiloServer.getServer().getDisplayBrandName()));

        for (ServerPlayerEntity playerEntity : KiloServer.getServer().getPlayerManager().getPlayerList()) {
            playerEntity.networkHandler.sendPacket(customPayloadS2CPacket);
        }

    }

}
