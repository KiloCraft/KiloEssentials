package org.kilocraft.essentials.mixin;

import net.minecraft.client.network.packet.PlayerListHeaderS2CPacket;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.server.ModifiablePlayerListMeta;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerListHeaderS2CPacket.class)
public abstract class PlayerListHeaderS2CPacketMixin implements ModifiablePlayerListMeta {
    private Text header;

    private Text footer;

    @Override
    public void setHeader(Text text) {
        header = text;
    }

    @Override
    public void setFooter(Text text) {
        footer = text;
    }

}
