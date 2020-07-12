package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.ListedText;
import org.kilocraft.essentials.util.text.Texter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerWarpsCommand extends EssentialCommand {
    static final String CACHE_KEY = "command.playerwarps";
    private static final TextColor PRIMARY_COLOR = TextColor.parse("#17BEBB");
    private static final TextColor NUM_COLOR = TextColor.parse("#FAD8D6");

    public PlayerWarpsCommand(String label, CommandPermission permission, String[] alias) {
        super(label, permission, alias);
        this.withUsage("command.playerwarps.usage");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), false));

        LiteralArgumentBuilder<ServerCommandSource> forceArgument = literal("force")
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true));

        pageArgument.then(forceArgument);
        this.commandNode.addChild(pageArgument.build());
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return send(ctx, 1, false);
    }

    private int send(final CommandContext<ServerCommandSource> ctx, int page, boolean forced) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (!forced && CacheManager.isPresent(CACHE_KEY)) {
            Map<PlayerWarp, String> map = (Map<PlayerWarp, String>) CacheManager.get(CACHE_KEY).get();
            return send(ctx.getSource(), page, map);
        }

        CompletableFuture.runAsync(() -> {
            Map<PlayerWarp, String> map = Maps.newHashMap();

            for (UUID owner : PlayerWarpsManager.getOwners()) {
                AtomicReference<String> name = new AtomicReference<>("");

                this.getEssentials().getUserThenAcceptAsync(owner, (optional) -> {
                    System.out.println("Got user!");
                    if (optional.isPresent() && optional.get().getFormattedDisplayName() != null) {
                        name.set(optional.get().getFormattedDisplayName());
                    }
                }).join();

                for (PlayerWarp warp : PlayerWarpsManager.getWarps(owner)) {
                    map.put(warp, name.get());
                }
            }

            ArrayList<Map.Entry<PlayerWarp, String>> entries = Lists.newArrayList(map.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            CacheManager.cache(new Cached<>(CACHE_KEY, 10, TimeUnit.MINUTES, map));
            send(ctx.getSource(), page, map);
        });

        return AWAIT;
    }

    private int send(final ServerCommandSource src, final int page, final Map<PlayerWarp, String> map) {
        TextInput input = new TextInput();
        int index = 0;

        for (Map.Entry<PlayerWarp, String> entry : map.entrySet()) {
            PlayerWarp warp = entry.getKey();
            String name = entry.getValue();

            index++;
            MutableText text = Texter.newText();
            text.append(new LiteralText(index + ".").styled((style) -> style.withColor(NUM_COLOR)));
            text.append(" ");
            text.append(warp.getName()).formatted(Formatting.WHITE).styled((style) ->
                    style.setHoverEvent(Texter.Events.onHover(
                            Texter.newText()
                                    .append(new LiteralText("By ").formatted(Formatting.WHITE))
                                    .append(Texter.newText(name.isEmpty() ? "&c?" : name))
                                    .append("\n")
                                    .append(new LiteralText("In ").formatted(Formatting.DARK_GRAY))
                                    .append(new LiteralText(RegistryUtils.dimensionToName(warp.getLocation().getDimensionType())))
                    ))
            );
            text.append(new LiteralText(" (").formatted(Formatting.DARK_GRAY));
            text.append(new LiteralText(warp.getType()).styled((style) -> style.withColor(PRIMARY_COLOR)));
            text.append(new LiteralText(") ").formatted(Formatting.DARK_GRAY));

            text.append(
                    Texter.newText().append(
                            Texts.bracketed(
                                    Texter.getButton(" &6i ", "/pwarp info " + warp.getName(),
                                            Texter.newText("&dClick for more Info"))
                            )
                    ).append(" ").append(
                            Texts.bracketed(
                                    Texter.getButton("&aGo", "/pwarp teleport " + warp.getName(),
                                            Texter.newText("&dClick to Teleport"))
                            )
                    )
            ).append(" ");

            int maxLength = 65 - text.getString().length();
            if (maxLength < 0) {
                maxLength = 66 - text.getString().length();
            }
            final String desc = warp.getDescription();
            final String shortenedDesc = desc.substring(0, Math.min(desc.length(), maxLength));

            final MutableText description = Texter.newText(TextFormat.clearColorCodes(shortenedDesc)).styled((style) ->
                    style.setHoverEvent(Texter.Events.onHover(Texter.newText(desc).formatted(Formatting.WHITE)))
            );

            if (desc.length() > maxLength) {
                description.append("...");
            }

            text.append(description.formatted(Formatting.GRAY));
            input.append(text);
        }

        ListedText.Page listed = ListedText.getPageFromText(ListedText.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());
        listed.send(src, "Player Warps (" + index + ")", "/" + this.getLabel() + " %page%");
        return SUCCESS;
    }
}
