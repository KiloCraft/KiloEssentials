package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CmdUtils;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class PingCommand extends EssentialCommand {
    public PingCommand() {
        super("ping", CommandPermission.PING_SELF, new String[]{"latency"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
                .requires(src -> hasPermission(src, CommandPermission.PING_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> execute(ctx.getSource(), getPlayer(ctx, "player")));

        argumentBuilder.executes(ctx -> execute(ctx.getSource(), ctx.getSource().getPlayer()));
        commandNode.addChild(selectorArgument.build());
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (CmdUtils.areTheSame(source, target)) {
            KiloChat.sendLangMessageTo(source, "command.ping.self",
                    TextFormat.getFormattedPing(target.pingMilliseconds), getStringForPing(target.pingMilliseconds));
            return 1;
        }

        KiloChat.sendLangMessageTo(source, "command.ping.others", target.getName().asString(),
                TextFormat.getFormattedPing(target.pingMilliseconds), getStringForPing(target.pingMilliseconds));

        return target.pingMilliseconds;
    }

    private static String getStringForPing(int i) {
        String prefix = "general.text.";
        String key;
        if (i < 200)
            key = prefix + "good";
        else if (i > 200 && i < 400)
            key = prefix + "medium";
        else if (i > 400 && i < 800)
            key = prefix + "bad";
        else key =  prefix + "oof";

        return ModConstants.getLang().getProperty(key);
    }

}
