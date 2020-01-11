package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class PingCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = literal("ping")
                .requires(src -> hasPermission(src, CommandPermission.PING_SELF))
                .executes(ctx -> execute(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", player())
                        .suggests(TabCompletions::allPlayers)
                        .requires(src -> hasPermission(src, CommandPermission.PING_OTHERS))
                        .executes(ctx -> execute(ctx.getSource(), getPlayer(ctx, "player")))).build();

        dispatcher.getRoot().addChild(rootCommand);
        dispatcher.register(literal("latency").redirect(rootCommand));
    }

    private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
        if (CommandHelper.areTheSame(source, target)) {
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
