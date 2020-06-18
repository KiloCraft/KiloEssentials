package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.DimensionArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
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

        LiteralArgumentBuilder<ServerCommandSource> nameLiteral = literal("name");
        RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.string())
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true, StringArgumentType.getString(ctx, "name"), null, null));

        LiteralArgumentBuilder<ServerCommandSource> dimensionLiteral = literal("dimension");
        RequiredArgumentBuilder<ServerCommandSource, Identifier> dimensionArgument = argument("dimension", DimensionArgumentType.dimension())
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true, null, DimensionArgumentType.getDimensionArgument(ctx, "dimension"), null));

        LiteralArgumentBuilder<ServerCommandSource> categoryLiteral = literal("category");
        RequiredArgumentBuilder<ServerCommandSource, String> categoryArgument = argument("category", StringArgumentType.string())
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true, null, null, StringArgumentType.getString(ctx, "category")))
                .suggests(this::typeSuggestions);

        LiteralArgumentBuilder<ServerCommandSource> forceArgument = literal("force")
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true));

        nameLiteral.then(nameArgument);
        pageArgument.then(nameLiteral);

        dimensionLiteral.then(dimensionArgument);
        pageArgument.then(dimensionLiteral);

        categoryLiteral.then(categoryArgument);
        pageArgument.then(categoryLiteral);

        pageArgument.then(forceArgument);
        commandNode.addChild(pageArgument.build());
        argumentBuilder.executes(this::execute);
    }

    private CompletableFuture<Suggestions> typeSuggestions(CommandContext<ServerCommandSource> serverCommandSourceCommandContext, SuggestionsBuilder suggestionsBuilder) {
        return CommandSource.suggestMatching(new ArrayList<>(PlayerWarp.Type.getTypes()), suggestionsBuilder);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return send(ctx, 1, false);
    }

    private int send(CommandContext<ServerCommandSource> ctx, int page, boolean force) throws CommandSyntaxException {
        return send(ctx, page, false, null, null, null);
    }

    private int send(CommandContext<ServerCommandSource> ctx, int page, boolean force, String name, ServerWorld dimension, String category) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (!force && CacheManager.shouldUse(CACHE_ID)) {
            AtomicReference<List<Map.Entry<String, List<PlayerWarp>>>> sortedList = new AtomicReference<>();

            CacheManager.getAndRun(CACHE_ID, (cached) -> sortedList.set((List<Map.Entry<String, List<PlayerWarp>>>) cached.get()));

            if (sortedList.get() != null) {
                return send(src, page, sortedList.get());
            }
        }

        CompletableFuture.runAsync(() -> {
            Map<String, List<PlayerWarp>> map = new HashMap<>();

            for (UUID owner : PlayerWarpsManager.getOwners()) {
                List<PlayerWarp> warps = PlayerWarpsManager.getWarps(owner);

                // Sort away incorrect ones
                // Don't waste time trying to sort if there's nothing to sort
                if (name != null || dimension != null || category != null) {
                    for (int i = warps.size() - 1; i >= 0; i--) {
                        if (name != null && !warps.get(i).getName().contains(name)) {
                            warps.remove(i);
                        } else if (dimension != null && warps.get(i).getLocation().getWorld() != dimension) {
                            warps.remove(i);
                        } else if (category != null && !warps.get(i).getType().equals(category)) {
                            warps.remove(i);
                        }
                    }
                }

                if (this.isOnline(owner)) {
                    try {
                        map.put(this.getOnlineUser(owner).getFormattedDisplayName(), warps);
                        continue;
                    } catch (CommandSyntaxException ignored) {
                    }
                }

                this.getEssentials().getUserThenAcceptAsync(owner, (optionalUser) -> {
                    if (optionalUser.isPresent()) {
                        map.put(optionalUser.get().getFormattedDisplayName(), warps);
                    } else {
                        map.put(StringUtils.EMPTY_STRING, warps);
                    }
                }).join();
            }

            List<Map.Entry<String, List<PlayerWarp>>> sorted = new ArrayList<>(map.entrySet());
            sorted.sort(Map.Entry.comparingByKey());

            CacheManager.cache(new Cached<>(CACHE_ID, 60, TimeUnit.MINUTES, sorted));
            send(src, page, sorted);
        });

        return AWAIT;
    }

    private int send(OnlineUser src, int page, List<Map.Entry<String, List<PlayerWarp>>> list) {
        TextInput input = new TextInput();

        int index = 0;
        for (Map.Entry<String, List<PlayerWarp>> entry : list) {
            String ownerName = entry.getKey().isEmpty() ? "&c&o?" : entry.getKey();

            List<PlayerWarp> warps = entry.getValue();
            for (PlayerWarp warp : warps) {
                index++;
                MutableText text = new LiteralText("");
                text.append(new LiteralText((index) + ".").formatted(Formatting.GOLD));
                text.append(" ");
                text.append(new LiteralText(warp.getName()).formatted(Formatting.WHITE)).styled((style) ->
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.newText("")
                        .append(new LiteralText("By ").formatted(Formatting.WHITE))
                        .append(new LiteralText(ownerName))
                        .append("\n")
                        .append(new LiteralText("In ").formatted(Formatting.WHITE))
                        .append(new LiteralText(RegistryUtils.dimensionToName(warp.getLocation().getDimensionType()))))));
                text.append(new LiteralText(" (").formatted(Formatting.DARK_GRAY));
                text.append(new LiteralText(warp.getType()).formatted(Formatting.LIGHT_PURPLE));
                text.append(new LiteralText(") ").formatted(Formatting.DARK_GRAY));
                text.append(
                        Texter.newText().append(
                                Texts.bracketed(Texter.getButton(" &6i ", "/pwarp info " + warp.getName(), Texter.newText("&dClick for more Info")))
                        ).append(" ").append(
                                Texts.bracketed(Texter.getButton("&aGo", "/pwarp teleport " + warp.getName(), Texter.newText("&dClick to Teleport")))
                        )
                ).append(" ");

                int maxLength = 60 - text.getString().length();
                String desc = warp.getDescription();
                String shortenedDesc = desc.substring(0, Math.min(desc.length(), maxLength));

                MutableText description = Texter.newText(TextFormat.clearColorCodes(shortenedDesc)).formatted(Formatting.WHITE).styled((style) ->
                        style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.newText(desc).formatted(Formatting.WHITE)))
                );

                if (desc.length() > maxLength) {
                    description.append("...");
                }

                text.append(description.formatted(Formatting.GRAY));
                input.append(text);
            }
        }

        Pager.Page paged = Pager.getPageFromText(Pager.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());

        paged.send(src.getCommandSource(), "Player Warps (" + index + ")", "/playerwarps %page%");
        return SUCCESS;
    }
}
