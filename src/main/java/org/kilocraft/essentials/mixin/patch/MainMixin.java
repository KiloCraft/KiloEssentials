package org.kilocraft.essentials.mixin.patch;

import net.minecraft.server.Main;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Main.class)
public class MainMixin {

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/LevelSummary;isPreWorldHeightChangeVersion()Z"))
    private static boolean iDontKnowWhatIamDoing(LevelSummary levelSummary) {
        return false;
    }

}
