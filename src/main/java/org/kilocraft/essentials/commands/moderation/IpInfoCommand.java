package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class IpInfoCommand extends EssentialCommand {
    public IpInfoCommand() {
        super("ipinfo", CommandPermission.IPINFO, 3, new String[]{"ip"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetArgument = argument("target", player())
                .suggests(TabCompletions::allPlayers)
                .executes(this::execute);

        commandNode.addChild(targetArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = getPlayer(ctx, "target");
        getOnlineUser(target).sendLangMessage("command.ipinfo", target.getEntityName(), target.networkHandler.getConnection().getAddress().toString());
        return SINGLE_SUCCESS;
    }

}
