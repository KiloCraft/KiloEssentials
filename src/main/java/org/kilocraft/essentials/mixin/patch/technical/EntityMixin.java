package org.kilocraft.essentials.mixin.patch.technical;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
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

    @Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;getList(Ljava/lang/String;I)Lnet/minecraft/nbt/NbtList;", ordinal = 0))
    public void addCommandFromTag(NbtCompound tag, CallbackInfo ci) {
        if (tag.contains("command")) this.command = tag.getString("command");
        if (tag.contains("leftCommand")) this.leftCommand = tag.getString("leftCommand");
        if (tag.contains("rightCommand")) this.rightCommand = tag.getString("rightCommand");
    }

    @Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtCompound;put(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;)Lnet/minecraft/nbt/NbtElement;"))
    public void addCommandToTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        if (!this.command.equals("")) tag.putString("command", this.command);
        if (!this.leftCommand.equals("")) tag.putString("leftCommand", this.leftCommand);
        if (!this.rightCommand.equals("")) tag.putString("rightCommand", this.rightCommand);
    }

}
