package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.config_old.KiloConfigOLD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "changeDimension", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;changeDimension(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/entity/Entity;"))
    private void modify(DimensionType dimensionType_1, CallbackInfoReturnable<Entity> cir) {
        boolean allowNether = KiloConfigOLD.getProvider().getMain().getBooleanSafely("server.world.allow_nether", false);
        boolean allowTheEnd = KiloConfigOLD.getProvider().getMain().getBooleanSafely("server.world.allow_the_end", false);

        if (dimensionType_1.equals(DimensionType.THE_NETHER) && !allowNether)
            cir.cancel();
        else if (dimensionType_1.equals(DimensionType.THE_END) && !allowTheEnd)
            cir.cancel();
    }

}
