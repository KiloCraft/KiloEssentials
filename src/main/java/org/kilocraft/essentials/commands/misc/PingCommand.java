package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("ping")
                        .requires(src -> hasPermission(src, "ping.self", 2))
                        .executes(ctx -> execute(ctx.getSource(), ctx.getSource().getPlayer()))
                        .then(argument("player", player())
                                .suggests(ArgumentSuggestions::allPlayers)
                                .requires(src -> hasPermission(src, "ping.others", 2))
                                .executes(ctx -> execute(ctx.getSource(), getPlayer(ctx, "player")))
                        )
        );

    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (CommandHelper.areTheSame(source, target))
            KiloChat.sendLangMessageTo(source, "command.ping.self", target.pingMilliseconds);
        else
            KiloChat.sendLangMessageTo(source, "command.ping.others", target.getName().asString(), target.pingMilliseconds);

        return SUCCESS();
    }
}
