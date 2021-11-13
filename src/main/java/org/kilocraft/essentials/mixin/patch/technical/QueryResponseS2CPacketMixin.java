package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.MotdConfigSection;
import org.kilocraft.essentials.patch.technical.VersionCompatibility;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(QueryResponseS2CPacket.class)
public abstract class QueryResponseS2CPacketMixin {

    @Final
    @Shadow
    private ServerMetadata metadata;

    // TODO: Rework this
    @Inject(
            method = "write",
            at = @At("HEAD")
    )
    public void updateMetaData(PacketByteBuf buf, CallbackInfo ci) {
        // Configurable message of the day
        MotdConfigSection motdConfig = KiloConfig.main().motd();
        if (motdConfig.enabled) {
            this.metadata.setDescription(ComponentText.toText(motdConfig.line1).append(new LiteralText("\n").append(ComponentText.toText(motdConfig.line2))));
        }
        final ServerMetadata.Players players = this.metadata.getPlayers();
        if (players != null) {
            // Exclude vanished players from sample list
            List<OnlineUser> online = KiloEssentials.getUserManager().getOnlineUsersAsList(false);
            int sampleSize = Math.min(12, online.size());
            GameProfile[] gameProfiles = new GameProfile[sampleSize];
            for (int i = 0; i < sampleSize; i++) {
                gameProfiles[i] = online.get(i).asPlayer().getGameProfile();
            }
            ServerMetadata.Players new_players = new ServerMetadata.Players(players.getPlayerLimit(), online.size());
            new_players.setSample(gameProfiles);
            this.metadata.setPlayers(new_players);
        }
        final ServerMetadata.Version version = this.metadata.getVersion();
        if (version != null && VersionCompatibility.isEnabled()) {
            ServerMetadata.Version newVersion;
            if (VersionCompatibility.shouldPretendProtocolVersion()) {
                newVersion = VersionCompatibility.getPretendMetaVersion();
            } else {
                newVersion = new ServerMetadata.Version(SharedConstants.getGameVersion().getName(), SharedConstants.getGameVersion().getProtocolVersion());
            }
            this.metadata.setVersion(newVersion);
        }
    }
}
