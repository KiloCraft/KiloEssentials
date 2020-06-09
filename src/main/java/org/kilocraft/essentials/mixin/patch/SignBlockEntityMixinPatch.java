package org.kilocraft.essentials.mixin.patch;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixinPatch {

    @Shadow @Final public Text[] text;

    @Shadow public abstract ServerCommandSource getCommandSource(ServerPlayerEntity serverPlayerEntity);

    //Fixes the activate method so your hand won't swing if the Sign doesn't have any commands
    @Inject(method = "onActivate", at = @At(value = "HEAD", target = "Lnet/minecraft/block/entity/SignBlockEntity;onActivate(Lnet/minecraft/entity/player/PlayerEntity;)Z"), cancellable = true)
    private void patch$SignActivationReturnValue(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        int cmds = 0;
        for (Text value : text) {
            Style style = value != null ? value.getStyle() : null;
            if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) {
                if (KiloServer.getServer().execute(getCommandSource((ServerPlayerEntity) playerEntity), style.getClickEvent().getValue()) > 0) {
                    cmds++;
                }
            }
        }

        cir.setReturnValue(cmds > 0);
    }

}
