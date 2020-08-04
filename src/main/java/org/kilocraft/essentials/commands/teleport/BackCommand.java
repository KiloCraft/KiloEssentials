package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.commands.CommandUtils;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class BackCommand extends EssentialCommand {
    public BackCommand() {
        super("back", CommandPermission.BACK_SELF, new String[]{"goback"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("target", player())
                .requires(src -> hasPermission(src, CommandPermission.BACK_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(this::executeOthers);

        commandNode.addChild(selectorArgument.build());
        argumentBuilder.executes(this::executeSelf);
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return sendBack(ctx, ctx.getSource().getPlayer());
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return sendBack(ctx, getPlayer(ctx, "target"));
    }

    private int sendBack(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) {
        OnlineUser user = getOnlineUser(target);

        if (user.getLastSavedLocation() == null) {
            sendMessage(ctx, "command.back.no_loc");
            return FAILED;
        }

        Location loc = user.getLastSavedLocation();
        user.saveLocation();
        user.teleport(loc, true);

        if (CommandUtils.areTheSame(ctx.getSource(), target))
            user.sendLangMessage("command.back.self", loc.asFormattedString());
        else
            sendMessage(ctx, "command.back.others", user.getUsername(), loc.asFormattedString());

        return SUCCESS;
    }
}
