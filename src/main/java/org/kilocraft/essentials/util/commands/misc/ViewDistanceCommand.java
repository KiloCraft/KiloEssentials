package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.settings.ServerSettings;

public class ViewDistanceCommand extends EssentialCommand {

    public ViewDistanceCommand() {
        super("viewdistance", CommandPermission.VIEWDISTANCE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> distance = this.argument("distance", IntegerArgumentType.integer(2, 32));
        distance.executes(this::execute);
        this.argumentBuilder.executes(this::info);
        this.commandNode.addChild(distance.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        int distance = IntegerArgumentType.getInteger(ctx, "distance");
        MinecraftServer server = ctx.getSource().getServer();
        ServerPlayerEntity player = ctx.getSource().getPlayer();

        if (server.isDedicated()) {
            if (distance != ServerSettings.getViewDistance()) {
                KiloEssentials.getMinecraftServer().getPlayerManager().setViewDistance(distance);
                player.sendMessage(StringText.of("command.viewdistance.set", distance), false);
            }
            return distance;
        }
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        player.sendMessage(StringText.of("command.viewdistance.info", ServerSettings.getViewDistance()), false);
        return SUCCESS;
    }
}
