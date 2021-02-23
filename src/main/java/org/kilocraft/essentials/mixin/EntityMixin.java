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
    String leftCommand = "";
    String rightCommand = "";

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/ListTag;", ordinal = 0))
    public void addCommandFromTag(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("command")) this.command = tag.getString("command");
        if (tag.contains("leftCommand")) this.leftCommand = tag.getString("leftCommand");
        if (tag.contains("rightCommand")) this.rightCommand = tag.getString("rightCommand");
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;"))
    public void addCommandToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if (!this.command.equals("")) tag.putString("command", this.command);
        if (!this.leftCommand.equals("")) tag.putString("leftCommand", this.leftCommand);
        if (!this.rightCommand.equals("")) tag.putString("rightCommand", this.rightCommand);
    }

}
