package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Inject(method = "changeDimension", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;changeDimension(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;"))
    private void modify(ServerWorld serverWorld, CallbackInfoReturnable<Entity> cir) {
        if (LocationUtil.shouldBlockAccessTo(serverWorld.getDimension())) {
            cir.cancel();
            KiloChat.sendLangMessageTo((ServerPlayerEntity) (Object) this, "general.dimension_not_allowed", RegistryUtils.dimensionToName(serverWorld.getDimension()));
        }

        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "teleport", at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"),cancellable = true)
    private void modify$Teleport(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

}
