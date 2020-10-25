package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import static net.minecraft.command.argument.DimensionArgumentType.dimension;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.Vec3ArgumentType.getVec3;
import static net.minecraft.command.argument.Vec3ArgumentType.vec3;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;

public class TeleportCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> tpToCommand = dispatcher.register(literal("teleportto")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTTO))
                .then(argument("user", StringArgumentType.string())
                        .suggests(ArgumentSuggestions::users)
                        .executes(TeleportCommands::teleportTo))
        );

        LiteralCommandNode<ServerCommandSource> tpPosCommand = dispatcher.register(literal("teleportpos")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTPOS))
                .then(argument("pos", vec3()).executes(TeleportCommands::teleportPos))
        );

        LiteralCommandNode<ServerCommandSource> tpHereCommand = dispatcher.register(literal("teleporthere")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTHERE))
                .then(argument("target", player()).executes(TeleportCommands::teleportHere))
        );

        LiteralCommandNode<ServerCommandSource> tpInCommand = dispatcher.register(literal("teleportin")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.TELEPORTIN))
                .then(argument("dimension", dimension()).suggests(ArgumentSuggestions::dimensions).then(argument("pos", vec3())
                                .executes(ctx -> teleportIn(ctx, ctx.getSource().getPlayer()))
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

    private static int teleportTo(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());
        String input = StringArgumentType.getString(ctx, "user");

        KiloEssentials.getInstance().getUserThenAcceptAsync(src, input, (user) -> {
            if (user.getLocation() == null) {
                src.sendLangError("command.back.no_loc");
                return;
            }

            src.teleport(user.getLocation(), true);
            src.sendLangMessage("template.#1", "position", getFormattedMessage(src.asPlayer()), src.getName());
        });

        return 0;
    }

    private static int teleportPos(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Vec3d vec = getVec3(ctx, "pos");

        KiloServer.getServer().getOnlineUser(player).saveLocation();
        ctx.getSource().getPlayer().teleport(
                player.getServerWorld(),
                vec.getX(), vec.getY(), vec.getZ(),
                player.yaw, player.pitch
        );

        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "position",
                getFormattedMessage(player), player.getName().asString());

        return SUCCESS();
    }

    private static int teleportHere(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity target = getPlayer(ctx, "target");
        ServerPlayerEntity sender = ctx.getSource().getPlayer();

        KiloServer.getServer().getOnlineUser(target).saveLocation();
        target.teleport(
                sender.getServerWorld(),
                sender.getPos().getX(), sender.getPos().getY(), sender.getPos().getZ(),
                sender.yaw, sender.pitch
        );

        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "position",
                getFormattedMessage(target), target.getName().asString());

        return SUCCESS();
    }

    private static int teleportIn(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        ServerWorld targetWorld = KiloServer.getServer().getMinecraftServer().getWorld(RegistryUtils.toWorldKey(DimensionArgumentType.getDimensionArgument(ctx, "dimension").getDimension()));
        Vec3d vec = getVec3(ctx, "pos");

        KiloServer.getServer().getOnlineUser(target).saveLocation();
        target.teleport(
                targetWorld,
                vec.getX(), vec.getY(), vec.getZ(),
                target.yaw, target.pitch
        );

        KiloChat.sendLangMessageTo(ctx.getSource(), "template.#1", "position",
                getFormattedMessage(target), target.getName().asString());

        return SUCCESS();
    }

    private static String getFormattedMessage(ServerPlayerEntity target) {
        return Vec3dLocation.of(target).toString();
    }


}
