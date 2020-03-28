package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.Pager;
import org.kilocraft.essentials.util.Texter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerWarpsCommand extends EssentialCommand {
    private static final String CACHE_ID = "command.playerwarps";

    public PlayerWarpsCommand(String label, CommandPermission permission, String[] alias) {
        super(label, permission, alias);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), false));
        final LiteralArgumentBuilder<ServerCommandSource> forceArgument = literal("force")
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true));

        pageArgument.then(forceArgument);
        commandNode.addChild(pageArgument.build());
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        try {
            send(ctx, 1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int send(final CommandContext<ServerCommandSource> ctx, int page, boolean force) throws CommandSyntaxException {
        final OnlineUser src = this.getOnlineUser(ctx);

        if (!force && CacheManager.shouldUse(CACHE_ID)) {
            AtomicReference<List<Map.Entry<PlayerWarp, String>>> sortedList = new AtomicReference<>();

            CacheManager.getAndRun(CACHE_ID, (cached) -> sortedList.set((List<Map.Entry<PlayerWarp, String>>) cached.get()));

            if (sortedList.get() != null) {
                return send(src, page, sortedList.get());
            }
        }

        CompletableFuture.runAsync(() -> {
            final HashMap<PlayerWarp, String> map = new HashMap<>();

            for (PlayerWarp warp : PlayerWarpsManager.getWarps()) {
                this.essentials.getUserThenAcceptAsync(warp.getOwner(), (optionalUser) -> {
                    if (optionalUser.isPresent()) {
                        map.put(warp, optionalUser.get().getFormattedDisplayName());
                    } else {
                        map.put(warp, null);
                    }
                });
            }

            final List<Map.Entry<PlayerWarp, String>> sorted = new ArrayList<>(map.entrySet());

            System.out.println(sorted.size());

            sorted.sort(Map.Entry.comparingByValue());

            System.out.println(sorted.size());

            CacheManager.cache(new Cached<>(CACHE_ID, 60, TimeUnit.MINUTES, sorted));

            send(src, page, sorted);
        });

        return AWAIT_RESPONSE;
    }

    private int send(OnlineUser src, int page, final List<Map.Entry<PlayerWarp, String>> list) {
        final String LINE_FORMAT = tl("command.playerwarps.format");
        final TextInput input = new TextInput(Texter.toText(tl("command.playerwarps.total", list.size())));

        for (int i = 0; i < list.size(); i++) {
            Map.Entry<PlayerWarp, String> entry = list.get(i);
            String ownerName = entry.getValue() == null ? "&c&oNot Present" : entry.getValue();

            input.append(Texter.toText(String.format(LINE_FORMAT, i, entry.getKey().getName(), entry.getKey().getType(), ownerName)).formatted(Formatting.YELLOW));
        }

        Pager.Page paged = Pager.getPageFromText(Pager.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());

        paged.send(src.getCommandSource(), "Player Warps", "/playerwarps %page%");
        return SINGLE_SUCCESS;
    }
}
