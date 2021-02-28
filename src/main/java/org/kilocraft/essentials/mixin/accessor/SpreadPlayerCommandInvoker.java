package org.kilocraft.essentials.mixin.accessor;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.SpreadPlayersCommand;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;

@Mixin(SpreadPlayersCommand.class)
public interface SpreadPlayerCommandInvoker {

    @Invoker("execute")
    public static int execute(ServerCommandSource serverCommandSource, Vec2f vec2f, float f, float g, int i, boolean bl, Collection<? extends Entity> collection) throws CommandSyntaxException {
        throw new AssertionError();
    }

}
