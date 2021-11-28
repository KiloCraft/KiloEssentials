package org.kilocraft.essentials.util.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import static net.minecraft.commands.arguments.DimensionArgument.dimension;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.getVec3;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.vec3;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class TeleportCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> tpToCommand = dispatcher.register(literal("teleportto")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTTO))
                .then(argument("user", StringArgumentType.string())
                        .suggests(ArgumentSuggestions::users)
                        .executes(TeleportCommands::teleportTo))
        );

        LiteralCommandNode<CommandSourceStack> tpPosCommand = dispatcher.register(literal("teleportpos")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTPOS))
                .then(argument("pos", vec3()).executes(TeleportCommands::teleportPos))
        );

        LiteralCommandNode<CommandSourceStack> tpHereCommand = dispatcher.register(literal("teleporthere")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTHERE))
                .then(argument("target", player()).executes(TeleportCommands::teleportHere))
        );

        LiteralCommandNode<CommandSourceStack> tpInCommand = dispatcher.register(literal("teleportin")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTIN))
                .then(argument("dimension", dimension()).suggests(ArgumentSuggestions::dimensions).then(argument("pos", vec3())
                                .executes(ctx -> teleportIn(ctx, ctx.getSource().getPlayerOrException()))
                                .then(argument("target", player())
                                        .executes(ctx -> teleportIn(ctx, getPlayer(ctx, "target"))))
                        )
                )
        );

        dispatcher.register(literal("tpto").requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTTO)).redirect(tpToCommand));
        dispatcher.register(literal("tppos").requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTPOS)).redirect(tpPosCommand));
        dispatcher.register(literal("tphere").requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTHERE)).redirect(tpHereCommand));
        dispatcher.register(literal("tpin").requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTIN)).redirect(tpInCommand));
    }

    private static int teleportTo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = KiloEssentials.getUserManager().getOnline(ctx.getSource());
        String input = StringArgumentType.getString(ctx, "user");

        KiloEssentials.getUserManager().getUserThenAcceptAsync(src, input, (user) -> {
            if (user.getLocation() == null) {
                src.sendLangError("command.back.no_loc");
                return;
            }

            src.teleport(user.getLocation(), true);
            src.sendLangMessage("template.#1", "position", getFormattedMessage(src.asPlayer()), src.getName());
        });

        return 0;
    }

    private static int teleportPos(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Vec3 vec = getVec3(ctx, "pos");

        KiloEssentials.getUserManager().getOnline(player).saveLocation();
        ctx.getSource().getPlayerOrException().teleportTo(
                player.getLevel(),
                vec.x(), vec.y(), vec.z(),
                player.getYRot(), player.getXRot()
        );

        ((CommandSourceUser) CommandSourceServerUser.of(ctx)).sendLangMessage("template.#1", "position",
                getFormattedMessage(player), player.getName().getContents());

        return 1;
    }

    private static int teleportHere(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = getPlayer(ctx, "target");
        ServerPlayer sender = ctx.getSource().getPlayerOrException();

        KiloEssentials.getUserManager().getOnline(target).saveLocation();
        target.teleportTo(
                sender.getLevel(),
                sender.position().x(), sender.position().y(), sender.position().z(),
                sender.getYRot(), sender.getXRot()
        );

        ((CommandSourceUser) CommandSourceServerUser.of(ctx)).sendLangMessage("template.#1", "position",
                getFormattedMessage(target), target.getName().getContents());

        return 1;
    }

    private static int teleportIn(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerLevel targetWorld = KiloEssentials.getMinecraftServer().getLevel(RegistryUtils.toWorldKey(DimensionArgument.getDimension(ctx, "dimension").dimensionType()));
        Vec3 vec = getVec3(ctx, "pos");

        KiloEssentials.getUserManager().getOnline(target).saveLocation();
        target.teleportTo(
                targetWorld,
                vec.x(), vec.y(), vec.z(),
                target.getYRot(), target.getXRot()
        );

        ((CommandSourceUser) CommandSourceServerUser.of(ctx)).sendLangMessage("template.#1", "position",
                getFormattedMessage(target), target.getName().getContents());

        return 1;
    }

    private static String getFormattedMessage(ServerPlayer target) {
        return Vec3dLocation.of(target).toString();
    }


}
