package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow
    public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(world, blockPos, f, gameProfile);
    }

    @Inject(method = "worldChanged", at = @At("HEAD"), cancellable = true)
    private void modify(ServerWorld serverWorld, CallbackInfo ci) {
        // Restrict dimension access
        if (LocationUtil.shouldBlockAccessTo(serverWorld.getDimension())) {
            KiloEssentials.getUserManager().getOnline((ServerPlayerEntity) (Object) this).sendLangMessage("general.dimension_not_allowed", RegistryUtils.dimensionToName(serverWorld.getDimension()));
            ci.cancel();
        }
        // Save user location for /back
        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "teleport", at = @At("RETURN"))
    private void savePlayerLocation(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        // Save user location for /back
        ServerUser.saveLocationOf((ServerPlayerEntity) (Object) this);
    }

}
