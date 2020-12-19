package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.chat.StringText;

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
                player.sendMessage(StringText.of(true, "command.viewdistance", distance), false);
            }
            return distance;
        }
        return SUCCESS;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        MinecraftServer server = ctx.getSource().getMinecraftServer();
        TextComponent text = Component.text("View Distance: ").color(NamedTextColor.GRAY).append(Component.text(server.getPlayerManager().getViewDistance()).color(NamedTextColor.GOLD));
        player.sendMessage(ComponentText.toText(text), false);
        return SUCCESS;
    }
}
