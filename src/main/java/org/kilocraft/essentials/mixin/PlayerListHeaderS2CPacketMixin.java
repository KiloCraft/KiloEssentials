package org.kilocraft.essentials.mixin;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.server.ModifiablePlayerListMeta;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerListHeaderS2CPacket.class)
public abstract class PlayerListHeaderS2CPacketMixin implements ModifiablePlayerListMeta {
    private Text text = new LiteralText("Test");

    @Shadow private Text header = new LiteralText("STUPID");

    @Shadow private Text footer;

//    @Inject(method = "write", at = @At(value = "RETURN", target = "Lnet/minecraft/client/network/packet/PlayerListHeaderS2CPacket;write(Lnet/minecraft/util/PacketByteBuf;)V"))
//    private void fix(PacketByteBuf packetByteBuf, CallbackInfo ci) {
//        System.out.println(packetByteBuf.readText().asFormattedString());
//    }

    @Override
    public void setHeader(Text text) {
        header = text;
    }

    @Override
    public void setFooter(Text text) {
        footer = text;
    }

}
