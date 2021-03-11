package org.kilocraft.essentials.mixin.patch;

import net.minecraft.screen.Property;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Property.class)
public class PropertyMixinPatch {

    @Inject(method = "hasChanged", at = @At(value = "HEAD"), cancellable = true)
    public void justPretendItChanged(CallbackInfoReturnable<Boolean> cir) {
        if (ServerSettings.getBoolean("patch.enchanting.hasChanged")) cir.setReturnValue(true);
    }

}
