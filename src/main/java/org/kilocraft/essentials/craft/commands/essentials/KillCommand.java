package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.Collection;
import java.util.Collections;

public class KillCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("ke_kill")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.kill", 2))
                .executes(c -> execute(c.getSource(), Collections.singleton(c.getSource().getPlayer())));

        argumentBuilder.then(
                CommandManager.argument("targets", EntityArgumentType.entities())
                    .suggests((context, builder) -> {
                        return CommandSuggestions.allPlayers.getSuggestions(context, builder);
                    })
                    .executes(c -> execute(c.getSource(), EntityArgumentType.getEntities(c, "targets")))
        );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> entities) {
        if (entities.size() > 1 && !Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.kill.multiple", 2)) {
            source.sendError(KiloCommands.getPermissionError("kiloessentials.command.kill.multiple"));
        } else {
            entities.forEach((entity) -> {
                entity.kill();
            });
        }

        source.sendFeedback(LangText.getFormatter(true, "command.kill.success", 2), false);
        return entities.size();
    }
}
