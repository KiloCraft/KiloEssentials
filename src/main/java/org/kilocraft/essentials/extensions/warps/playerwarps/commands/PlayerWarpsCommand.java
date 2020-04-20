package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.text.Pager;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerWarpsCommand extends EssentialCommand {
    private static final String CACHE_ID = "command.playerwarps";

    public PlayerWarpsCommand(String label, CommandPermission permission, String[] alias) {
        super(label, permission, alias);
        this.withUsage("command.playerwarps.usage", "search context... (dimension|name|type)");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), false));
        LiteralArgumentBuilder<ServerCommandSource> forceArgument = literal("force")
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true));

        pageArgument.then(forceArgument);
        commandNode.addChild(pageArgument.build());
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return send(ctx, 1, false);
    }

    private int send(CommandContext<ServerCommandSource> ctx, int page, boolean force) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (!force && CacheManager.shouldUse(CACHE_ID)) {
            AtomicReference<List<Map.Entry<PlayerWarp, String>>> sortedList = new AtomicReference<>();

            CacheManager.getAndRun(CACHE_ID, (cached) -> sortedList.set((List<Map.Entry<PlayerWarp, String>>) cached.get()));

            if (sortedList.get() != null) {
                return send(src, page, sortedList.get());
            }
        }

        CompletableFuture.runAsync(() -> {
            Map<PlayerWarp, String> map = new HashMap<>();

            for (PlayerWarp warp : PlayerWarpsManager.getWarps()) {
                if (this.isOnline(warp.getOwner())) {
                    try {
                        map.put(warp, this.getOnlineUser(warp.getOwner()).getFormattedDisplayName());
                        continue;
                    } catch (CommandSyntaxException e) {
                        src.sendError(e.getMessage());
                    }
                }

                this.essentials.getUserThenAcceptAsync(warp.getOwner(), (optionalUser) -> {
                    if (optionalUser.isPresent()) {
                        map.put(warp, optionalUser.get().getFormattedDisplayName());
                    } else {
                        map.put(warp, StringUtils.EMPTY_STRING);
                    }
                }).join();
            }

            List<Map.Entry<PlayerWarp, String>> sorted = new ArrayList<>(map.entrySet());
            sorted.sort(Map.Entry.comparingByValue());

            CacheManager.cache(new Cached<>(CACHE_ID, 60, TimeUnit.MINUTES, sorted));
            send(src, page, sorted);
        });

        return AWAIT;
    }

    private int send(OnlineUser src, int page, List<Map.Entry<PlayerWarp, String>> list) {
        TextInput input = new TextInput(Texter.toText(tl("command.playerwarps.total", list.size())));

        for (int i = 0; i < list.size(); i++) {
            Map.Entry<PlayerWarp, String> entry = list.get(i);
            String ownerName = entry.getValue().isEmpty() ? "&c&oNot Present" : entry.getValue();

            Text text = new LiteralText("");
            text.append(new LiteralText((i + 1) + ".").formatted(Formatting.GOLD));
            text.append(" ");
            text.append(new LiteralText(entry.getKey().getName()).formatted(Formatting.WHITE)).styled((style) -> {
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.toText("")
                        .append(new LiteralText("By ").formatted(Formatting.WHITE))
                        .append(new LiteralText(ownerName))
                        .append("\n")
                        .append(new LiteralText("In ").formatted(Formatting.WHITE))
                        .append(new LiteralText(RegistryUtils.dimensionToName(entry.getKey().getLocation().getDimensionType())))));
            });
            text.append(new LiteralText(" (").formatted(Formatting.DARK_GRAY));
            text.append(new LiteralText(entry.getKey().getType()).formatted(Formatting.LIGHT_PURPLE));
            text.append(new LiteralText(") ").formatted(Formatting.DARK_GRAY));

            int maxLength = 45 - text.getString().length();
            String desc = entry.getKey().getDescription();
            String shortenedDesc = desc.substring(0, Math.min(desc.length(), maxLength));

            Text description = Texter.toText(shortenedDesc).styled((style) -> {
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(desc).formatted(Formatting.WHITE)));
            });

            if (desc.length() > maxLength) {
                description.append("...");
            }

            text.append(description.formatted(Formatting.GRAY));
            input.append(text);
        }

        Pager.Page paged = Pager.getPageFromText(Pager.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());

        paged.send(src.getCommandSource(), "Player Warps", "/playerwarps %page%");
        return SUCCESS;
    }
}
