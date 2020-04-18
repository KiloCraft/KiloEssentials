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
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Pager;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerWarpCommand extends EssentialCommand {
    private static final String HEADER = ModConstants.getLang().getProperty("command.playerwarp.header");
    public PlayerWarpCommand(String label, CommandPermission permission, String[] alias) {
        super(label, permission, alias);
        this.withUsage("command.playerwarp.usage", "add", "name", "type", "description");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        {
            final LiteralArgumentBuilder<ServerCommandSource> addArgument = literal("add");
            final RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word())
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_type"));
            final RequiredArgumentBuilder<ServerCommandSource, String> typeArgument = argument("type", StringArgumentType.word())
                    .suggests(this::typeSuggestions)
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_desc"));
            final RequiredArgumentBuilder<ServerCommandSource, String> descArgument = argument("description", StringArgumentType.greedyString())
                    .executes(this::add);

            typeArgument.then(descArgument);
            nameArgument.then(typeArgument);
            addArgument.then(nameArgument);
            commandNode.addChild(addArgument.build());
        }

        {
            final LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
            final RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::remove);

            removeArgument.then(nameArgument);
            commandNode.addChild(removeArgument.build());
        }

        {
            final LiteralArgumentBuilder<ServerCommandSource> listArgument = literal("list")
                    .executes((ctx) -> this.list(ctx, 1, null));
            final RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("user")
                    .executes((ctx) -> this.list(ctx, 1, this.getUserArgumentInput(ctx, "user")));
            final RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                    .executes((ctx) -> this.list(ctx, IntegerArgumentType.getInteger(ctx, "page"), this.getUserArgumentInput(ctx, "user")));

            userArgument.then(pageArgument);
            listArgument.then(userArgument);
            commandNode.addChild(listArgument.build());
        }

        {
            final LiteralArgumentBuilder<ServerCommandSource> listArgument = literal("info");
            final RequiredArgumentBuilder<ServerCommandSource, String> warpArgument = argument("warp", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::info);

            listArgument.then(warpArgument);
            commandNode.addChild(listArgument.build());
        }

    }

    private int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser user = getOnlineUser(ctx);
        final String input = StringArgumentType.getString(ctx, "name");
        final String name = input.replaceFirst("-confirmed-", "");

        if (PlayerWarpsManager.getWarp(name) != null) {
            user.sendLangMessage("command.playerwarp.already_set");
            return FAILED;
        }

        final String type = StringArgumentType.getString(ctx, "type");
        final String desc = StringArgumentType.getString(ctx, "description");

        if (TextFormat.removeAlternateColorCodes('&', desc).length() > 100) {
            user.sendLangError("command.playerwarp.desc_too_long");
            return FAILED;
        }

        if (!PlayerWarp.Type.isValid(type)) {
            user.sendLangError("command.playerwarp.invalid_type", type);
            return FAILED;
        }

        if (PlayerWarpsManager.getWarpsByName().contains(name) && !input.startsWith("-confirmed-")) {
            user.sendMessage(getConfirmationText(name, ""));
            return AWAIT;
        } else {
            PlayerWarpsManager.addWarp(new PlayerWarp(name, user.getLocation(), user.getUuid(), type, desc));
        }

        user.sendLangMessage("command.playerwarp.set", name);
        return SUCCESS;
    }

    private int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        String name = StringArgumentType.getString(ctx, "name");

        if (!PlayerWarpsManager.getWarpsByName().contains(name)) {
            user.sendLangMessage("command.playerwarp.invalid_warp");
            return FAILED;
        }

        PlayerWarpsManager.removeWarp(name);

        user.sendLangMessage("command.playerwarp.remove", name);
        return SUCCESS;
    }

    private int list(CommandContext<ServerCommandSource> ctx, int page, @Nullable String inputName) throws CommandSyntaxException {
        final OnlineUser src = this.getOnlineUser(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (inputName == null) {
            this.sendList(src.getCommandSource(), src, page);
            return SUCCESS;
        }

        if (this.isOnline(inputName)) {
            if (!inputName.equals(src.getUsername()) && !src.hasPermission(CommandPermission.PLAYER_WARP_OTHERS)) {
                src.sendLangError("command.exception.permission");
                return FAILED;
            }

            this.sendList(src.getCommandSource(), this.getOnlineUser(inputName), page);
            return SUCCESS;
        }

        this.essentials.getUserThenAcceptAsync(src, inputName, (user) -> {
            this.sendList(src.getCommandSource(), user, page);
        });

        return AWAIT;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser src = this.getOnlineUser(ctx);
        final String inputName = StringArgumentType.getString(ctx, "warp");

        final PlayerWarp warp = PlayerWarpsManager.getWarp(inputName);

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

        if (this.isOnline(warp.getOwner())) {
            sendInfo(src, warp, this.getOnlineUser(warp.getOwner()));
            return SUCCESS;
        }

        this.essentials.getUserThenAcceptAsync(src, warp.getOwner(), (user) -> {
            sendInfo(src, warp, user);
        });

        return AWAIT;
    }

    private void sendInfo(OnlineUser src, PlayerWarp warp, User owner) {
        Texter.InfoBlockStyle text = Texter.InfoBlockStyle.of("Player Warp: " + warp.getName());
        text.append("Owner", owner.getNameTag()).append(" ");
        text.append("Type", warp.getType()).append(" ");
        text.append("World", RegistryUtils.dimensionToName(owner.getLocation().getDimensionType()));

        src.sendMessage(text.get());
    }

    private void sendList(ServerCommandSource src, User user, int page) {
        final String LINE_FORMAT = ModConstants.translation("command.playerwarp.format");
        List<PlayerWarp> warps = PlayerWarpsManager.getWarps(user.getUuid());
        Collections.sort(warps);

        TextInput input = new TextInput(HEADER);

        for (int i = 0; i < warps.size(); i++) {
            PlayerWarp warp = warps.get(i);
            //1. (type) Name - The Dimension
            input.append(String.format(LINE_FORMAT, i + 1, warp.getName(), warp.getType(), RegistryUtils.dimensionToName(warp.getLocation().getDimensionType())));
        }

        Pager.Page paged = Pager.getPageFromStrings(Pager.Options.builder().setPageIndex(page - 1).build(), input.getLines());
        paged.send(src, "Player Warps: " + user.getNameTag(), "/playerwarps " + src.getName() + " %page%");
    }

    private CompletableFuture<Suggestions> warpSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (CommandUtils.isPlayer(context.getSource())) {
            List<String> strings = new ArrayList<>();
            UUID uuid = context.getSource().getPlayer().getUuid();
            for (PlayerWarp warp : PlayerWarpsManager.getWarps(uuid)) {
                strings.add(warp.getName());
            }

            return CommandSource.suggestMatching(strings, builder);
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> typeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(new ArrayList<>(PlayerWarp.Type.getTypes()), builder);
    }

    private Text getConfirmationText(String warpName, String user) {
        return Texter.confirmationMessage(
                "command.playerwarp.set.confirmation_message",
                Texter.getButton("Confirm", "/pwarp set " + warpName, Texter.toText("Click").formatted(Formatting.GREEN))
        );
    }

}
