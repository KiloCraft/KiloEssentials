package org.kilocraft.essentials.mixin.patch.technical;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;

@Mixin(ArgumentBuilder.class)
public abstract class ArgumentBuilderMixin<S, T extends ArgumentBuilder<S, T>> {

    @Shadow
    private Predicate<S> requirement;

    @Shadow
    protected abstract T getThis();

    private static final String COMMAND_MANAGER = "net.minecraft.class_2170";
    private static final String NET_MINECRAFT = "net.minecraft.";


    /**
     * @author Drex
     * @reason Add permissions for vanilla commands
     */
    @Overwrite
    public T requires(Predicate<S> requirement) {
        if ((Object) this instanceof LiteralArgumentBuilder literalArgumentBuilder) {
            final String callerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            if (callerClassName.startsWith(NET_MINECRAFT) && !callerClassName.equals(COMMAND_MANAGER)) {
                final String literal = literalArgumentBuilder.getLiteral();
                requirement = src -> KiloCommands.hasPermission((ServerCommandSource) src, "minecraft." + literal);
            }
        }
        this.requirement = requirement;
        return this.getThis();
    }
}
