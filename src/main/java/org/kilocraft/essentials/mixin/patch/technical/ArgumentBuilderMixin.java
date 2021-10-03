package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Predicate;

@Mixin(ArgumentBuilder.class)
public abstract class ArgumentBuilderMixin<S, T extends ArgumentBuilder<S, T>> {

    @Shadow
    private Predicate<S> requirement;

    private static final String COMMAND_MANAGER = "net.minecraft.class_2170";
    private static final String NET_MINECRAFT = "net.minecraft.";

    @Redirect(method = "requires", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lcom/mojang/brigadier/builder/ArgumentBuilder;requirement:Ljava/util/function/Predicate;"))
    public void addVanillaPermissions(ArgumentBuilder<S, T> argumentBuilder, Predicate<S> original) {
        final String callerClassName = Thread.currentThread().getStackTrace()[3].getClassName();
        if (argumentBuilder instanceof LiteralArgumentBuilder<?> literalArgumentBuilder) {
            if (callerClassName.startsWith(NET_MINECRAFT) && !callerClassName.equals(COMMAND_MANAGER)) {
                final String literal = literalArgumentBuilder.getLiteral();
                try {
                    this.requirement = src -> KiloCommands.hasPermission((ServerCommandSource) src, "minecraft." + literal);
                } catch (Exception e) {
                    this.requirement = original;
                }
            }
        }
    }

}
