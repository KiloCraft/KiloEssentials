package org.kilocraft.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.minecraft.SharedConstants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.MotdConfigSection;
import org.kilocraft.essentials.servermeta.ServerMetaManager;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.stream.Collectors;

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
            ServerMetadata.Players players = this.metadata.getPlayers();
            if (players != null) {
                List<OnlineUser> online = KiloEssentials.getServer().getUserManager().getOnlineUsersAsList(false);
                int sampleSize = Math.min(12, online.size());
                GameProfile[] gameProfiles = new GameProfile[sampleSize];
                for (int i = 0; i < sampleSize; i++) {
                    gameProfiles[i] = online.get(i).asPlayer().getGameProfile();
                }
                ServerMetadata.Players new_players = new ServerMetadata.Players(players.getPlayerLimit(), online.size());
                new_players.setSample(gameProfiles);
                this.metadata.setPlayers(new_players);
            }
    }
}
