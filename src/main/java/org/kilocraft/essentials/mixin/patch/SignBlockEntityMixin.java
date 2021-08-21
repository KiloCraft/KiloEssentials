package org.kilocraft.essentials.mixin.patch;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin {

    @Shadow public abstract ServerCommandSource getCommandSource(ServerPlayerEntity serverPlayerEntity);

    @Shadow @Final private Text[] texts;

    // Fixes the activate method so your hand won't swing if the sign doesn't have any commands
    @Inject(method = "onActivate", at = @At(value = "HEAD"), cancellable = true)
    private void signActivationReturnValue(ServerPlayerEntity serverPlayerEntity, CallbackInfoReturnable<Boolean> cir) {
        for (Text value : texts) {
            Style style = value != null ? value.getStyle() : null;
            if (style != null && style.getClickEvent() != null && style.getClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) {
                serverPlayerEntity.swingHand(Hand.MAIN_HAND, true);
                KiloCommands.execute(getCommandSource(serverPlayerEntity), style.getClickEvent().getValue());
                cir.setReturnValue(true);
            }
        }

        cir.setReturnValue(false);
    }

}
