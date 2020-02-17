package org.kilocraft.essentials.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract String getEntityName();

    @Inject(method = "getDisplayName", at = @At(value = "HEAD", target = "Lnet/minecraft/entity/player/PlayerEntity;getDisplayName()Lnet/minecraft/text/Text;"), cancellable = true)
    private void modify(CallbackInfoReturnable<Text> cir) {
        if (!KiloConfig.main().useNicknamesEverywhere)
            return;

        PlayerEntity player = (ServerPlayerEntity) (Object) this;
        OnlineUser user = KiloServer.getServer().getOnlineUser(player.getUuid());
        if (player.getScoreboardTeam() != null && user != null) {
            Text text = new LiteralText(user.getFormattedDisplayName()).styled((style) ->
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tell " + this.getEntityName() + " "))
                            .setHoverEvent(getHoverEvent(player)).setInsertion(this.getEntityName()));

            cir.setReturnValue(player.getScoreboardTeam().modifyText(text));
        }
    }

    private HoverEvent getHoverEvent(PlayerEntity player) {
        CompoundTag compoundTag = new CompoundTag();
        Identifier identifier = EntityType.getId(player.getType());
        compoundTag.putString("id", player.getUuidAsString());
        if (identifier != null) {
            compoundTag.putString("type", identifier.toString());
        }

        compoundTag.putString("name", Text.Serializer.toJson(player.getName()));
        return new HoverEvent(HoverEvent.Action.SHOW_ENTITY, new LiteralText(compoundTag.toString()));
    }
}
