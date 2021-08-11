package org.kilocraft.essentials.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.util.InteractionHandler;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Inject(method = "worldChanged", cancellable = true, at = @At(value = "HEAD"))
    private void modify(ServerWorld serverWorld, CallbackInfo ci) {
        if (LocationUtil.shouldBlockAccessTo(serverWorld.getDimension())) {
            ci.cancel();
            KiloEssentials.getUserManager().getOnline((ServerPlayerEntity) (Object)this).sendLangMessage("general.dimension_not_allowed", RegistryUtils.dimensionToName(serverWorld.getDimension()));
        }

        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "teleport", at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"),cancellable = true)
    private void modify$Teleport(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "teleport", at = @At(value = "RETURN", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"))
    private void patch$Teleport$updateExperience(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        this.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(this.experienceProgress, this.totalExperience, this.experienceLevel));
    }

    @Inject(method = "attack", at = @At(value = "HEAD"))
    public void onAttack(Entity entity, CallbackInfo ci) {
        InteractionHandler.handleInteraction((ServerPlayerEntity) (Object) this, entity, true);
    }

}
