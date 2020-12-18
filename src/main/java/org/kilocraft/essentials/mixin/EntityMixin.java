package org.kilocraft.essentials.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    String command = "";

    @Inject(method = "fromTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;"))
    public void addCommandFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("command")) this.command = tag.getString("command");
    }

    @Inject(method = "toTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"))
    public void addCommandToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.command.equals("")) tag.putString("command", this.command);
    }

}
