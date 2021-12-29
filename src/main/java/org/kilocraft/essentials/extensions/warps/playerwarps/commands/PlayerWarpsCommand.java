package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.ListedText;
import org.kilocraft.essentials.util.text.Texter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;

public class PlayerWarpsCommand extends EssentialCommand {
    static final String CACHE_KEY = "command.playerwarps";
    private static final TextColor PRIMARY_COLOR = TextColor.parseColor("#17BEBB");
    private static final TextColor NUM_COLOR = TextColor.parseColor("#FAD8D6");

    public PlayerWarpsCommand(String label, CommandPermission permission, String[] alias) {
        super(label, permission, alias);
        this.withUsage("command.playerwarps.usage");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, Integer> pageArgument = this.argument("page", IntegerArgumentType.integer(1))
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), false));

        LiteralArgumentBuilder<CommandSourceStack> forceArgument = this.literal("force")
                .executes((ctx) -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page"), true));

        pageArgument.then(forceArgument);
        this.commandNode.addChild(pageArgument.build());
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        return this.send(ctx, 1, false);
    }

    private int send(final CommandContext<CommandSourceStack> ctx, int page, boolean forced) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (!forced && CacheManager.isPresent(CACHE_KEY)) {
            List<Map.Entry<PlayerWarp, String>> list = (List<Map.Entry<PlayerWarp, String>>) CacheManager.get(CACHE_KEY).get();
            return this.send(ctx.getSource(), page, list);
        }

        CompletableFuture.runAsync(() -> {
            Map<PlayerWarp, String> map = Maps.newHashMap();
            AtomicInteger atomicInteger = new AtomicInteger();
            for (PlayerWarp warp : PlayerWarpsManager.getWarps()) {
                this.getUserManager().getUserThenAcceptAsync(warp.getOwner(), (optional) -> {
                    String name = optional.isPresent() ? optional.get().getFormattedDisplayName() : "<red><bold>?";
                    map.put(warp, name);
                    atomicInteger.incrementAndGet();
                });
            }
            long startTime = System.nanoTime();
            while (atomicInteger.get() != PlayerWarpsManager.getWarps().size() || startTime + 5000000 > System.nanoTime())
                ;
            List<Map.Entry<PlayerWarp, String>> entries = Lists.newArrayList(map.entrySet());
            entries.sort(Map.Entry.comparingByKey());
            CacheManager.cache(new Cached<>(CACHE_KEY, 1, TimeUnit.MINUTES, entries));
            this.send(ctx.getSource(), page, entries);

        });

        return AWAIT;
    }

    private int send(final CommandSourceStack src, final int page, final List<Map.Entry<PlayerWarp, String>> list) {
        TextInput input = new TextInput();
        int index = 0;

        for (Map.Entry<PlayerWarp, String> entry : list) {
            final String name = entry.getValue();
            final PlayerWarp warp = entry.getKey();

            index++;
            MutableComponent text = Texter.newText();
            text.append(new TextComponent(index + ".").withStyle((style) -> style.withColor(NUM_COLOR)));
            text.append(" ");
            Location location = warp.getLocation();
            text.append(new TextComponent(warp.getName()).withStyle((style) ->
                    style.withHoverEvent(Texter.Events.onHover(
                            Texter.newText()
                                    .append(new TextComponent("By ").withStyle(ChatFormatting.WHITE))
                                    .append(Texter.newText(name.isEmpty() ? "&c?" : name))
                                    .append("\n")
                                    .append(new TextComponent("In ").withStyle(ChatFormatting.GRAY))
                                    .append(new TextComponent(RegistryUtils.dimensionToName(warp.getLocation().getDimensionType())))
                                    .append("\n")
                                    .append(new TextComponent("At ").withStyle(ChatFormatting.GRAY))
                                    .append(new TextComponent(Math.round(location.getX()) + " " + Math.round(location.getY()) + " " + Math.round(location.getZ())).withStyle(ChatFormatting.WHITE))
                    ))
            ).withStyle(ChatFormatting.WHITE));
            text.append(new TextComponent(" (").withStyle(ChatFormatting.DARK_GRAY));
            text.append(new TextComponent(warp.getType()).withStyle((style) -> style.withColor(PRIMARY_COLOR)));
            text.append(new TextComponent(") ").withStyle(ChatFormatting.DARK_GRAY));

            text.append(
                    Texter.newText().append(
                            ComponentUtils.wrapInSquareBrackets(
                                    Texter.getButton(" &6i ", "/pwarp info " + warp.getName(),
                                            Texter.newText("&dClick for more Info"))
                            )
                    ).append(" ").append(
                            ComponentUtils.wrapInSquareBrackets(
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

            final MutableComponent description = Texter.newText(ComponentText.clearFormatting(shortenedDesc)).withStyle((style) ->
                    style.withHoverEvent(Texter.Events.onHover(Texter.newText(desc).withStyle(ChatFormatting.WHITE)))
            );

            if (desc.length() > maxLength) {
                description.append("...");
            }

            text.append(description.withStyle(ChatFormatting.GRAY));
            input.append(text);
        }

        ListedText.Page listed = ListedText.getPageFromText(ListedText.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());
        listed.send(src, "Player Warps (" + index + ")", "/" + this.getLabel() + " %page%");
        return SUCCESS;
    }
}
