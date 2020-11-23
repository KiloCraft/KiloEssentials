package org.kilocraft.essentials.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.player.UserUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract String getEntityName();

    @Shadow protected abstract MutableText addTellClickEvent(MutableText mutableText);

    @Inject(method = "getDisplayName", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"), cancellable = true)
    private void modify(CallbackInfoReturnable<Text> cir) {
        if (!KiloConfig.main().useNicknamesEverywhere)
            return;

        PlayerEntity player = (ServerPlayerEntity) (Object) this;
        OnlineUser user = KiloServer.getServer().getOnlineUser(player.getUuid());
        if (player.getScoreboardTeam() != null && user != null) {
            Text text = new LiteralText(user.getFormattedDisplayName()).styled((style) ->
                    style.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + this.getEntityName() + " "))
                            .withHoverEvent((HoverEvent) this.addTellClickEvent(UserUtils.getDisplayNameWithMeta(user, true))).withInsertion(this.getEntityName()));

            cir.setReturnValue(player.getScoreboardTeam().decorateName(text));
        }
    }

}
