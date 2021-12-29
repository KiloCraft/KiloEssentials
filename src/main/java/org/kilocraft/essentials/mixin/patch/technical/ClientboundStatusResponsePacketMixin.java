package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
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

@Mixin(ClientboundStatusResponsePacket.class)
public abstract class ClientboundStatusResponsePacketMixin {

    @Shadow @Final private ServerStatus status;

    // TODO: Rework this
    @Inject(
            method = "write",
            at = @At("HEAD")
    )
    public void updateMetaData(FriendlyByteBuf buf, CallbackInfo ci) {
        // Configurable message of the day
        MotdConfigSection motdConfig = KiloConfig.main().motd();
        if (motdConfig.enabled) {
            this.status.setDescription(ComponentText.toText(motdConfig.line1).append(new TextComponent("\n").append(ComponentText.toText(motdConfig.line2))));
        }
        final ServerStatus.Players players = this.status.getPlayers();
        if (players != null) {
            // Exclude vanished players from sample list
            List<OnlineUser> online = KiloEssentials.getUserManager().getOnlineUsersAsList(false);
            int sampleSize = Math.min(12, online.size());
            GameProfile[] gameProfiles = new GameProfile[sampleSize];
            for (int i = 0; i < sampleSize; i++) {
                gameProfiles[i] = online.get(i).asPlayer().getGameProfile();
            }
            ServerStatus.Players new_players = new ServerStatus.Players(players.getMaxPlayers(), online.size());
            new_players.setSample(gameProfiles);
            this.status.setPlayers(new_players);
        }
        final ServerStatus.Version version = this.status.getVersion();
        if (version != null && VersionCompatibility.isEnabled()) {
            ServerStatus.Version newVersion;
            if (VersionCompatibility.shouldPretendProtocolVersion()) {
                newVersion = VersionCompatibility.getPretendMetaVersion();
            } else {
                newVersion = new ServerStatus.Version(SharedConstants.getCurrentVersion().getName(), SharedConstants.getCurrentVersion().getProtocolVersion());
            }
            this.status.setVersion(newVersion);
        }
    }
}
