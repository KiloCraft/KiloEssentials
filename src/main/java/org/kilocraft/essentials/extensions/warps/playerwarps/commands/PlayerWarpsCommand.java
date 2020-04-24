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
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
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

                if (this.isOnline(owner)) {
                    try {
                        map.put(this.getOnlineUser(owner).getFormattedDisplayName(), warps);
                        continue;
                    } catch (CommandSyntaxException ignored) {
                    }
                }

                this.essentials.getUserThenAcceptAsync(owner, (optionalUser) -> {
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
                Text text = new LiteralText("");
                text.append(new LiteralText((index) + ".").formatted(Formatting.GOLD));
                text.append(" ");
                text.append(new LiteralText(warp.getName()).formatted(Formatting.WHITE)).styled((style) -> {
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.toText("")
                            .append(new LiteralText("By ").formatted(Formatting.WHITE))
                            .append(new LiteralText(ownerName))
                            .append("\n")
                            .append(new LiteralText("In ").formatted(Formatting.WHITE))
                            .append(new LiteralText(RegistryUtils.dimensionToName(warp.getLocation().getDimensionType())))));
                });
                text.append(new LiteralText(" (").formatted(Formatting.DARK_GRAY));
                text.append(new LiteralText(warp.getType()).formatted(Formatting.LIGHT_PURPLE));
                text.append(new LiteralText(") ").formatted(Formatting.DARK_GRAY));
                text.append(
                        Texter.toText().append(
                                Texts.bracketed(Texter.getButton(" &6i ", "/pwarp info " + warp.getName(), Texter.toText("&dClick for more Info")))
                        ).append(" ").append(
                                Texts.bracketed(Texter.getButton("&aGo", "/pwarp teleport " + warp.getName(), Texter.toText("&dClick to Teleport")))
                        )
                ).append(" ");

                int maxLength = 60 - text.getString().length();
                String desc = warp.getDescription();
                String shortenedDesc = desc.substring(0, Math.min(desc.length(), maxLength));

                Text description = Texter.toText(TextFormat.clearColorCodes(shortenedDesc)).formatted(Formatting.WHITE).styled((style) -> {
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.toText(desc).formatted(Formatting.WHITE)));
                });

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
