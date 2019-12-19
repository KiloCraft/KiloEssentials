package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;

import java.util.Collection;
import java.util.Collections;

import static net.minecraft.command.arguments.EntityArgumentType.entities;
import static net.minecraft.command.arguments.EntityArgumentType.getEntities;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class KillCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("ke_kill")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.KILL_SINGLE))
                .executes(c -> execute(c.getSource(), Collections.singleton(c.getSource().getPlayer())));

        argumentBuilder.then(
                argument("targets", entities())
                    .suggests(TabCompletions::allPlayers)
                    .executes(c -> execute(c.getSource(), getEntities(c, "targets")))
        );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, Collection<? extends Entity> entities) {
        if (entities.size() > 1 && !KiloCommands.hasPermission(source, CommandPermission.KILL_MULTIPLE)) {
            source.sendError(KiloCommands.getPermissionError(CommandPermission.KILL_MULTIPLE.getNode()));
        } else {
            entities.forEach(Entity::kill);
        }

        if (entities.size() > 1)
            source.sendFeedback(LangText.getFormatter(true, "command.kill.success", entities.size()), false);

        return entities.size();
    }
}
