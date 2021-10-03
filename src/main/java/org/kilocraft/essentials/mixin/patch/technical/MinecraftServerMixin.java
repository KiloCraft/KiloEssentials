package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.Brandable;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.provided.BrandedServer;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements Brandable {

    @Shadow private int ticks;

    @Override
    public String getServerModName() {
        return BrandedServer.getFinalBrandName();
    }

    @Redirect(
            method = "tick",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETFIELD,
                    target = "Lnet/minecraft/server/MinecraftServer;ticks:I",
                    ordinal = 1
            )
    )
    public int modifySaveInterval(MinecraftServer server) {
        if ((this.ticks + 600) % ServerSettings.patch_save_interval == 0) {
            KiloChat.broadCast(ModConstants.translation("general.warning.save", 30));
        } else if ((this.ticks + 200) % ServerSettings.patch_save_interval == 0) {
            KiloChat.broadCast(ModConstants.translation("general.warning.save", 10));
        }
        return this.ticks % ServerSettings.patch_save_interval == 0 ? 6000 : -1;
    }

}
