package org.kilocraft.essentials.mixin;

import net.minecraft.MinecraftVersion;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.Mod;
import org.kilocraft.essentials.wrapper.ServerBrand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerBrand {

    private static String brandName;
    @Override
    public String getServerModName() {
        brandName = String.format(
                "Fabric/§6KiloEssentials§r (%s, %s)",
                MinecraftVersion.create().getName(),
                Mod.properties.getProperty("version"));
        return brandName;
    }

}
