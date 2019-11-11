package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.SuggestArgument;
import org.kilocraft.essentials.KiloCommands;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.command.arguments.EntityArgumentType.entities;
import static net.minecraft.command.arguments.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class KillCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("kill");
        KiloCommands.getCommandPermission("kill.single");
        KiloCommands.getCommandPermission("kill.multiple");
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("ke_kill")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("kill.single"), 2))
                .executes(c -> execute(c.getSource(), Collections.singleton(c.getSource().getPlayer())));

        argumentBuilder.then(
                argument("targets", entities())
                    .suggests(SuggestArgument::allPlayers)
                    .executes(c -> execute(c.getSource(), getEntities(c, "targets")))
        );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> entities) {
        if (entities.size() > 1 && !Thimble.hasPermissionOrOp(source, KiloCommands.getCommandPermission("kill.multiple"), 2)) {
            source.sendError(KiloCommands.getPermissionError(KiloCommands.getCommandPermission("kill.multiple")));
        } else {
            entities.forEach((entity) -> {
                entity.kill();
            });
        }

        if (entities.size() > 1)
            source.sendFeedback(LangText.getFormatter(true, "command.kill.success", entities.size()), false);

        return entities.size();
    }
}
