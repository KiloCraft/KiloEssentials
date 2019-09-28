package org.kilocraft.essentials.craft.mixin;

import com.google.gson.JsonElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelGeneratorType;
import org.kilocraft.essentials.craft.homesystem.PlayerHomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Indigo Amann
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void loadHomes(String string_1, String string_2, long long_1, LevelGeneratorType levelGeneratorType_1, JsonElement jsonElement_1, CallbackInfo ci) {
        File homes = new File(gameDir.getPath() + "/" + string_1 + "/homes.dat");
        File homes_old = new File(gameDir.getPath() + "/" + string_1 + "/homes.dat_old");
        PlayerHomeManager.INSTANCE = new PlayerHomeManager();
        if (!homes.exists()) {
            if (homes_old.exists()) {}
            else return;
        }
        try {
            if (!homes.exists() && homes_old.exists()) throw new FileNotFoundException();
            CompoundTag tag = NbtIo.readCompressed(new FileInputStream(homes));
            PlayerHomeManager.INSTANCE.fromNbt(tag);
        } catch (IOException e) {
            System.err.println("Could not load homes.dat:");
            e.printStackTrace();
            if (homes_old.exists()) {
                System.out.println("Attempting to load backup homes...");
                try {
                    CompoundTag tag = NbtIo.readCompressed(new FileInputStream(homes));
                    PlayerHomeManager.INSTANCE.fromNbt(tag);
                } catch (IOException e2) {
                    throw new RuntimeException("Could not load homes.dat_old - Crashing server to save data. Remove or fix homes.dat or homes.dat_old to continue");

                }
            }
        }
    }
    @Shadow
    private File gameDir;
}
