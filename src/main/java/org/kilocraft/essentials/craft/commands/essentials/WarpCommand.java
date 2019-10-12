package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.worldwarps.WarpManager;

public class WarpCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("warp")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp", 2));

        RequiredArgumentBuilder<ServerCommandSource, String> warpArg = CommandManager.argument("warp", StringArgumentType.string());
        LiteralArgumentBuilder<ServerCommandSource> literalList = CommandManager.literal("- list");

        warpArg.suggests(suggestionProvider);
        builder.then(warpArg);
        builder.then(literalList);
        registerAdmin(builder);
        dispatcher.register(builder);
    }

    private static void registerAdmin(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalAdd = CommandManager.literal("- add");
        LiteralArgumentBuilder<ServerCommandSource> literalRemove = CommandManager.literal("- remove");

        literalAdd.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.add", 2));
        literalRemove.requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.warp.manage.remove", 2));

        builder.then(literalAdd);
        builder.then(literalRemove);
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        WarpManager.INSTANCE.getWarpsByName().forEach(builder::suggest);

        return builder.buildFuture();
    });

}
