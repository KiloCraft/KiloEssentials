package org.kilocraft.essentials.api.mixin;

import net.minecraft.MinecraftVersion;
import net.minecraft.server.MinecraftServer;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.server.ServerModName;
import org.kilocraft.essentials.api.wrapper.ServerBrand;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ServerBrand {

    @Override
    public String getServerModName() {
        String s;
        if (ServerModName.getName().isEmpty()) {
            s = String.format(
                    "Fabric/KiloEssentials (%s, %s)",
                    MinecraftVersion.create().getName(),
                    Mod.properties.getProperty("version"));
        } else s = ServerModName.getName();

        return s;
    }

}
