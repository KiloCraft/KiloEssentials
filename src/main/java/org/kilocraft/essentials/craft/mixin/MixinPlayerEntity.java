package org.kilocraft.essentials.craft.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.kilocraft.essentials.craft.player.KiloPlayer;
import org.kilocraft.essentials.craft.player.KiloPlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

	@Inject(at = @At("HEAD"), method = "getDisplayName")
	private void getDisplayName(CallbackInfoReturnable<Text> cir) {
		PlayerEntity player = (PlayerEntity) (Object) this;
		KiloPlayer kiloPlayer = KiloPlayerManager.getPlayerData(player.getUuid());

		/*if (kiloPlayer.nick == "") {
			CallbackInfoReturnable<R>.getCallInfoClassName(returnType)
			return new LiteralText(player.getName().asString());
		} else {
			return new LiteralText(kiloPlayer.nick);
		}*/
	}

}
