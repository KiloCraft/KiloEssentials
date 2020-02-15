package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.RegistryUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public abstract void sendChatMessage(Text text, MessageType messageType);

    @Shadow public abstract void addChatMessage(Text text, boolean bl);

    @Inject(method = "changeDimension", cancellable = true, at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;changeDimension(Lnet/minecraft/world/dimension/DimensionType;)Lnet/minecraft/entity/Entity;"))
    private void modify(DimensionType dimensionType_1, CallbackInfoReturnable<Entity> cir) {
        boolean allowNether = KiloConfig.main().world().allowTheNether;
        boolean allowTheEnd = KiloConfig.main().world().allowTheEnd;

        if ((dimensionType_1.equals(DimensionType.THE_NETHER) && !allowNether) ||
                dimensionType_1.equals(DimensionType.THE_END) && !allowTheEnd) {
            cir.cancel();
            this.addChatMessage(LangText.getFormatter(true, "general.dimension_not_allowed",
                    RegistryUtils.toIdentifier(dimensionType_1).getPath()), false);
        }
    }

    @Inject(method = "teleport", at = @At(value = "HEAD", target = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDFF)V"),cancellable = true)
    private void modify$Teleport(ServerWorld serverWorld, double d, double e, double f, float g, float h, CallbackInfo ci) {
        OnlineUser user = KiloServer.getServer().getOnlineUser((ServerPlayerEntity) (Object) this);
        if (user != null)
            user.saveLocation();
    }

}
