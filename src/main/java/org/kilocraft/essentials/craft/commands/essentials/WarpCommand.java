package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.command.arguments.BlockPosArgumentType;
import net.minecraft.command.arguments.PosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("warp")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp", 2));

        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = CommandManager.argument("warp", StringArgumentType.string());
        LiteralArgumentBuilder<ServerCommandSource> literalList = CommandManager.literal("-list");

        warpArg.executes(c -> executeTeleport(c.getSource(), StringArgumentType.getString(c, "warp")));
        literalList.executes(c -> executeList(c.getSource()));

        //warpArg.suggests(suggestionProvider);
        builder.then(warpArg);
        builder.then(literalList);
        registerAdmin(builder, dispatcher);
        dispatcher.register(builder);
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> builder, CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalAdd = CommandManager.literal("-add");
        LiteralArgumentBuilder<ServerCommandSource> literalRemove = CommandManager.literal("-remove");
        LiteralArgumentBuilder<ServerCommandSource> aliasAdd = CommandManager.literal("addwarp");
        LiteralArgumentBuilder<ServerCommandSource> aliasRemove = CommandManager.literal("delwarp");
        RequiredArgumentBuilder<ServerCommandSource, String> removeArg = CommandManager.argument("warp", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, String> addArg = CommandManager.argument("name", StringArgumentType.string());
        RequiredArgumentBuilder<ServerCommandSource, PosArgument> addArgBlockPos = CommandManager.argument("blockPos", BlockPosArgumentType.blockPos());
        RequiredArgumentBuilder<ServerCommandSource, Boolean> addArgPermission = CommandManager.argument("requiresPermission", BoolArgumentType.bool());

        literalAdd.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.add", 2));
        literalRemove.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.remove", 2));
        aliasAdd.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.add", 2));
        aliasRemove.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.remove", 2));

        removeArg.executes(c -> executeRemove(c.getSource(), StringArgumentType.getString(c, "warp")));
        addArgPermission.executes(c -> executeAdd(
                c.getSource(),
                StringArgumentType.getString(c, "name"),
                BoolArgumentType.getBool(c, "requiresPermission"),
                BlockPosArgumentType.getBlockPos(c, "blockPos")
        ));

        addArgBlockPos.then(addArgPermission);
        addArg.then(addArgBlockPos);
        literalAdd.then(addArg);

        literalRemove.then(removeArg);

        aliasAdd.then(addArg);
        aliasRemove.then(removeArg);

        dispatcher.register(aliasAdd);
        dispatcher.register(aliasRemove);

        builder.then(literalAdd);
        builder.then(literalRemove);
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        WarpManager.INSTANCE.getWarpsByName().forEach(builder::suggest);
        return builder.buildFuture();
    });

    private static int executeTeleport(ServerCommandSource source, String warp) {
        return 1;
    }

    private static int executeList(ServerCommandSource source) {
        return 1;
    }

    private static int executeAdd(ServerCommandSource source, String warp, boolean permission, BlockPos blockPos) {
        return 1;
    }

    private static int executeRemove(ServerCommandSource source, String warp) {
        return 1;
    }

}
