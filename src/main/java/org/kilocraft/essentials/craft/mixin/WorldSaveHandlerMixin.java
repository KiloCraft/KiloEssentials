package org.kilocraft.essentials.craft.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.WorldSaveHandler;
import net.minecraft.world.level.LevelProperties;
import org.kilocraft.essentials.craft.homesystem.PlayerHomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Indigo Amann
 */
@Mixin(WorldSaveHandler.class)
public class WorldSaveHandlerMixin {
    @Inject(method = "saveWorld(Lnet/minecraft/world/level/LevelProperties;Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void onSaveWorld(LevelProperties levelProperties_1, CompoundTag compoundTag_1, CallbackInfo ci) {
        if (PlayerHomeManager.INSTANCE != null) {
            File claimDataFile = new File(worldDir, "homes.dat");
            if (claimDataFile.exists()) {
                File old = new File(worldDir, "homes.dat_old");
                if (old.exists()) old.delete();
                claimDataFile.renameTo(old);
                claimDataFile.delete();
            }
            try {
                claimDataFile.createNewFile();
                CompoundTag tag = PlayerHomeManager.INSTANCE.toNbt();
                NbtIo.writeCompressed(tag, new FileOutputStream(claimDataFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Shadow
    private File worldDir;
}
