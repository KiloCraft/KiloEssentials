package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientboundPlayerInfoPacket.PlayerUpdate.class)
public abstract class PlayerUpdateMixin {
    @Shadow
    @Final
    private GameProfile profile;

    @Inject(
            method = "getDisplayName",
            at = @At("HEAD"),
            cancellable = true
    )
    private void modifyPlayerListEntry(CallbackInfoReturnable<Component> cir) {
        if (KiloConfig.main().playerList().useNicknames) {
            Component text = KiloEssentials.getUserManager().getOnline(this.profile.getId()).getRankedDisplayName();
            cir.setReturnValue(text);
        }
    }

}
