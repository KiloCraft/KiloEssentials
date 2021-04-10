package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class ViewDistanceCommand extends EssentialCommand {

    public ViewDistanceCommand() {
        super("viewdistance", CommandPermission.VIEWDISTANCE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> distance = argument("distance", IntegerArgumentType.integer(2, 32));
        distance.executes(this::execute);
        argumentBuilder.executes(this::info);
        commandNode.addChild(distance.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int distance = IntegerArgumentType.getInteger(ctx, "distance");
        MinecraftServer server = ctx.getSource().getMinecraftServer();
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (server.isDedicated()) {
            if (distance != server.getPlayerManager().getViewDistance()) {
                server.getPlayerManager().setViewDistance(distance);
                ServerSettings.setInt("view_distance", distance);
                player.sendMessage(StringText.of(true, "command.viewdistance.set", distance), false);
            }
            return distance;
        }
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of(true, "command.viewdistance.info", ServerSettings.getInt("view_distance")), false);
        return SUCCESS;
    }
}
