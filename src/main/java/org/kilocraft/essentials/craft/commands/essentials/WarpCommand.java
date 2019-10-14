package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.worldwarps.Warp;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("warp")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp", 2))
                .executes(c -> executeList(c.getSource()));

        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = CommandManager.argument("warp", StringArgumentType.string());

        warpArg.executes(c -> executeTeleport(c.getSource(), StringArgumentType.getString(c, "warp")));
        warpArg.suggests((context, builder1) -> {
            return WarpManager.suggestWarps.getSuggestions(context, builder1);
        });

        builder.then(warpArg);
        registerAdmin(builder, dispatcher);
        dispatcher.register(builder);
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> aliasAdd = CommandManager.literal("addwarp");
        LiteralArgumentBuilder<ServerCommandSource> aliasRemove = CommandManager.literal("delwarp");
        RequiredArgumentBuilder<ServerCommandSource, String> removeArg = CommandManager.argument("warp", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, String> addArg = CommandManager.argument("name", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> addArgBlockPos = CommandManager.argument("blockPos", BlockPosArgumentType.blockPos());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> addArgPermission = CommandManager.argument("requiresPermission", BoolArgumentType.bool());

        aliasAdd.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.add", 2));
        aliasRemove.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.remove", 2));

        removeArg.executes(c -> executeRemove(c.getSource(), StringArgumentType.getString(c, "warp")));
        addArgPermission.executes(c -> executeAdd(
                c.getSource(),
                StringArgumentType.getString(c, "name"),
                BoolArgumentType.getBool(c, "requiresPermission"),
                BlockPosArgumentType.getBlockPos(c, "blockPos")
        ));

        removeArg.suggests((context, builder1) -> {
            return WarpManager.suggestWarps.getSuggestions(context, builder1);
        });

        addArgBlockPos.then(addArgPermission);
        addArg.then(addArgBlockPos);

        aliasAdd.then(addArg);
        aliasRemove.then(removeArg);

        dispatcher.register(aliasAdd);
        dispatcher.register(aliasRemove);
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        WarpManager.getWarpsByName().forEach(builder::suggest);
        return builder.buildFuture();
    });

    private static int executeTeleport(ServerCommandSource source, String name) throws CommandSyntaxException {
        Warp warp = WarpManager.getWarp(name);
        //source.getPlayer().teleport();


        return 1;
    }

    private static int executeList(ServerCommandSource source) throws CommandSyntaxException {
        return 1;
    }

    private static int executeAdd(ServerCommandSource source, String warp, boolean permission, BlockPos blockPos) {
        Double pitch = 0.0;
        Double yaw = 0.0;

        WarpManager.addWarp(
                new Warp(
                        warp,
                        blockPos,
                        1,
                        1,
                        permission
                )
        );

        return 1;
    }

    private static int executeRemove(ServerCommandSource source, String warp) {
        WarpManager.removeWarp(warp);
        return 1;
    }

}
