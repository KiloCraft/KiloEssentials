package org.kilocraft.essentials.mixin;

import net.kyori.adventure.text.Component;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.MotdConfigSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QueryResponseS2CPacket.class)
public class QueryResponseS2CPacketMixin {

    @Shadow
    private ServerMetadata metadata;

    @Inject(method = "write", at = @At(value = "HEAD"))
    public void setMetaData(PacketByteBuf buf, CallbackInfo ci) {
        MotdConfigSection motdConfig = KiloConfig.main().motd();
        if (motdConfig.enabled) {
            this.metadata.setDescription(ComponentText.toText(ComponentText.of(motdConfig.line1, false).append(Component.text("\n").append(ComponentText.of(motdConfig.line2)))));
        }
    }
}
