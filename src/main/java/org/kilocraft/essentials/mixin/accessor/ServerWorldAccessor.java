package org.kilocraft.essentials.mixin.accessor;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {

    @Accessor("globalEntities")
    List<Entity> getGlobalEntities();

}
