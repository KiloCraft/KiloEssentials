package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandUtils;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class PingCommand extends EssentialCommand {
    public PingCommand() {
        super("ping", CommandPermission.PING_SELF, new String[]{"latency"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
                .requires(src -> hasPermission(src, CommandPermission.PING_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> execute(ctx.getSource(), getPlayer(ctx, "player")));

        argumentBuilder.executes(ctx -> execute(ctx.getSource(), ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (CommandUtils.areTheSame(source, target)) {
            KiloChat.sendLangMessageTo(source, "command.ping.self",
                    TextFormat.getFormattedPing(target.pingMilliseconds), target.pingMilliseconds);
            return 1;
        }

        KiloChat.sendLangMessageTo(source, "command.ping.others", target.getName().asString(),
                TextFormat.getFormattedPing(target.pingMilliseconds), target.pingMilliseconds);

        return target.pingMilliseconds;
    }

}
