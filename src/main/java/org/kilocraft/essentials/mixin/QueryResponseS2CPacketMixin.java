package org.kilocraft.essentials.mixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.util.Rainbow;
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
    public void setMetaData(PacketByteBuf buf, CallbackInfo ci){
        MotdConfigSection motdConfig = KiloConfig.main().motd();
        if(motdConfig.enabled) {
            MutableText line1 = motdConfig.rainbow ? Rainbow.formatRainbow(TextFormat.removeAlternateColorCodes('&', motdConfig.line1), motdConfig.offset, motdConfig.ignorespaces) : TextFormat.translateToLiteralText('&', motdConfig.line1);
            MutableText line2 = motdConfig.rainbow ? Rainbow.formatRainbow(TextFormat.removeAlternateColorCodes('&', motdConfig.line2), motdConfig.offset, motdConfig.ignorespaces) : TextFormat.translateToLiteralText('&', motdConfig.line2);
            MutableText motd = line1.append(new LiteralText("\n").append(line2));
            this.metadata.setDescription(motd);
        }
    }
}
