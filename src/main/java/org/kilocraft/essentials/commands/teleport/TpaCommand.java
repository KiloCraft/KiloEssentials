package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/**
 * @author Indigo Amann
 */
public class TpaCommand {
    private static final Map<ServerPlayerEntity, Pair<Pair<ServerPlayerEntity, Boolean>, Long>> tpMap = new HashMap<>();
    public static void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final LiteralArgumentBuilder<ServerCommandSource> tpa = literal("tpa");
        tpa.requires(source -> source.hasPermissionLevel(2));
        tpa.requires(source -> {
            try {
                return source.getPlayer() != null;
            } catch (final CommandSyntaxException e) {
                return false;
            }
        });
        final LiteralArgumentBuilder<ServerCommandSource> tpahere = literal("tpahere");
        tpahere.requires(source -> source.hasPermissionLevel(2));
        tpahere.requires(source -> {
            try {
                return source.getPlayer() != null;
            } catch (final CommandSyntaxException e) {
                return false;
            }
        });
        final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> playerA = argument("player", player());
        final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> playerB = argument("player", player());

        playerA.suggests(TabCompletions::allPlayers);
        playerA.suggests(TabCompletions::allPlayers);

        playerA.executes(context -> TpaCommand.executeRequest(context, false));
        playerB.executes(context -> TpaCommand.executeRequest(context, true));
        tpa.then(playerA);
        tpahere.then(playerB);
        dispatcher.register(tpa);
        dispatcher.register(tpahere);
        {
            final LiteralArgumentBuilder<ServerCommandSource> accept = literal("tpaccept");
            final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> player = argument("player", player());
            player.executes(context -> TpaCommand.executeResponse(context, true));
            accept.then(player);
            dispatcher.register(accept);
        }
        final LiteralArgumentBuilder<ServerCommandSource> deny = literal("tpdeny");
        final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> player = argument("player", player());
        player.executes(context -> TpaCommand.executeResponse(context, false));
        deny.then(player);
        dispatcher.register(deny);
        final LiteralArgumentBuilder<ServerCommandSource> cancel = literal("tpcancel");
        cancel.requires(source -> source.hasPermissionLevel(1));
        cancel.executes(TpaCommand::cancelRequest);
        dispatcher.register(cancel);
    }
    private static int executeRequest(final CommandContext<ServerCommandSource> context, final boolean here) throws CommandSyntaxException {
        final ServerPlayerEntity victim = getPlayer(context, "player");
        final ServerPlayerEntity sender = context.getSource().getPlayer();
        TpaCommand.tpMap.put(sender, new Pair<>(new Pair<>(victim, here), new Date().getTime()));
        victim.sendMessage(new LiteralText("").append(sender.getDisplayName()).append(new LiteralText(" has requested " + (here ? "that you teleport to them" : "to teleport to you") + ". ").formatted(Formatting.GOLD)).append(
                new LiteralText("[ACCEPT] ").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + sender.getGameProfile().getName()))
                .setColor(Formatting.GREEN))).append(
                new LiteralText("[DENY] ").setStyle(new Style().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + sender.getGameProfile().getName()))
                        .setColor(Formatting.RED))));
        sender.sendMessage(new LiteralText("Your request was sent. ").formatted(Formatting.GOLD).append(new LiteralText("[CANCEL]").setStyle(new Style()
        .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpcancel"))
        .setColor(Formatting.RED))));
        return 0;
    }
    private static int executeResponse(final CommandContext<ServerCommandSource> context, final boolean accepted) throws CommandSyntaxException {
        final ServerPlayerEntity sender = getPlayer(context, "player");
        final ServerPlayerEntity victim = context.getSource().getPlayer();
        if (TpaCommand.hasTPRequest(sender, victim)) {
            if (accepted) {
                sender.sendMessage(new LiteralText("").append(new LiteralText("Your teleportation request to ").formatted(Formatting.GOLD)).append(victim.getDisplayName()).append(new LiteralText(" was ").formatted(Formatting.GOLD).append(accepted ? new LiteralText("ACCEPTED").formatted(Formatting.GREEN) : new LiteralText("DENIED").formatted(Formatting.RED))));
                final boolean toSender = TpaCommand.useTPRequest(sender);
                final ServerPlayerEntity tpTo = toSender ? sender : victim;
                KiloServer.getServer().getOnlineUser(sender).saveLocation();
                (toSender ? victim : sender).teleport(tpTo.getServerWorld(), tpTo.getPos().x, tpTo.getPos().y, tpTo.getPos().z, tpTo.yaw, tpTo.pitch);
            } else {
                sender.sendMessage(new LiteralText("Your teleportation requrest was denied.").formatted(Formatting.RED));
                victim.sendMessage(new LiteralText("The request was denied.").formatted(Formatting.GREEN));
                TpaCommand.tpMap.remove(sender);
            }
        } else {
            victim.sendMessage(new LiteralText("").append(sender.getDisplayName()).append(new LiteralText(" is not requesting a telepoert.").formatted(Formatting.RED)));
        }
        return 0;
    }
    private static int cancelRequest(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final ServerPlayerEntity sender = context.getSource().getPlayer();
        if (TpaCommand.hasAnyTPRequest(sender)) {
            TpaCommand.tpMap.get(sender).getLeft().getLeft().sendMessage(new LiteralText("").append(sender.getDisplayName()).append(new LiteralText(" cancelled their teleportation request.").formatted(Formatting.GOLD)));
            TpaCommand.tpMap.remove(sender);
            sender.sendMessage(new LiteralText("Your teleportation request was cancelled.").formatted(Formatting.GOLD));
        } else {
            sender.sendMessage(new LiteralText("You don't have an active teleportation request.").formatted(Formatting.RED));
        }
        return 0;
    }
    private static boolean hasAnyTPRequest(final ServerPlayerEntity source) {
        if (TpaCommand.tpMap.containsKey(source)) {
            if (new Date().getTime() - TpaCommand.tpMap.get(source).getRight() > 60000) {
                TpaCommand.tpMap.remove(source);
            }
            else return true;
        }
        return false;
    }
    private static boolean hasTPRequest(final ServerPlayerEntity source, final ServerPlayerEntity victim) {
        if (TpaCommand.tpMap.containsKey(source)) {
            if (TpaCommand.tpMap.get(source).getLeft().getLeft().equals(victim)) {
                if (new Date().getTime() - TpaCommand.tpMap.get(source).getRight() > 60000) {
                    TpaCommand.tpMap.remove(source);
                }
                else return true;
            }
        }
        return false;
    }
    private static boolean useTPRequest(final ServerPlayerEntity source) {
        final boolean toSender = TpaCommand.tpMap.get(source).getLeft().getRight();
        TpaCommand.tpMap.remove(source);
        return toSender;
    }
}
