package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;

import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class PingCommand extends EssentialCommand {
    public PingCommand() {
        super("ping", CommandPermission.PING_SELF, new String[]{"latency"});
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> selectorArgument = this.argument("player", player())
                .requires(src -> this.hasPermission(src, CommandPermission.PING_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.execute(ctx, getPlayer(ctx, "player")));

        this.argumentBuilder.executes(ctx -> this.execute(ctx, ctx.getSource().getPlayerOrException()));
        this.commandNode.addChild(selectorArgument.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        CommandSourceUser user = this.getCommandSource(ctx);
        if (CommandUtils.areTheSame(ctx.getSource(), target)) {
            user.sendLangMessage("command.ping.self", ComponentText.formatPing(target.latency));
            return 1;
        }

        user.sendLangMessage("command.ping.others", target.getName().getContents(),
                ComponentText.formatPing(target.latency));

        return target.latency;
    }

}
