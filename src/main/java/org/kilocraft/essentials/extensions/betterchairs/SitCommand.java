package org.kilocraft.essentials.extensions.betterchairs;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class SitCommand extends EssentialCommand {
    public SitCommand() {
        super("sit", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SIT));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> boolArgument =  argument("set", word())
                .suggests(TabCompletions::boolStyle)
                .executes(this::set);


        argumentBuilder.executes(this::execute);
        commandNode.addChild(boolArgument.build());
    }

    private int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());
        String input = getString(ctx, "set");
        boolean bool = input.equals("on");

        user.setCanSit(bool);
        user.sendLangMessage("template.#2", "canSit", bool);
        return SINGLE_SUCCESS;
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());

        user.setCanSit(!user.canSit());
        user.sendLangMessage("template.#2", "canSit", user.canSit());
        return SINGLE_SUCCESS;
    }

}
