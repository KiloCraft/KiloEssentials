package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.entity.Entity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {

    @Redirect(method = "tickEntity", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    public <T> void shouldTickEntity(Consumer<T> consumer, T t) {
        // Configurable entity ticking
        if (!ServerSettings.entityTickCache[0]) return;
        if (t instanceof Entity) {
            if (!ServerSettings.entityTickCache[Registry.ENTITY_TYPE.getRawId(((Entity) t).getType()) + 1]) return;
        }
        consumer.accept(t);
    }

}
