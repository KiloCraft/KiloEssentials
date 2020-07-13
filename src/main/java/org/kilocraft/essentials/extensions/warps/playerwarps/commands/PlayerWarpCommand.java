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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.ListedText;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class PlayerWarpCommand extends EssentialCommand {
    private static final String HEADER = ModConstants.getLang().getProperty("command.playerwarp.header");
    public PlayerWarpCommand(String label, Predicate<ServerCommandSource> predicate, String[] alias) {
        super(label, predicate, alias);
        this.withUsage("command.playerwarp.usage", "add", "name", "type", "description");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        {
            LiteralArgumentBuilder<ServerCommandSource> addArgument = literal("add");
            RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word())
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_type"));
            RequiredArgumentBuilder<ServerCommandSource, String> typeArgument = argument("type", StringArgumentType.word())
                    .suggests(this::typeSuggestions)
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_desc"));
            RequiredArgumentBuilder<ServerCommandSource, String> descArgument = argument("description", StringArgumentType.greedyString())
                    .executes(this::add);

            typeArgument.then(descArgument);
            nameArgument.then(typeArgument);
            addArgument.then(nameArgument);
            commandNode.addChild(addArgument.build());
        }

        {
            LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
            RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::remove);

            removeArgument.then(nameArgument);
            commandNode.addChild(removeArgument.build());
        }

        {
            LiteralArgumentBuilder<ServerCommandSource> listArgument = literal("list")
                    .executes((ctx) -> this.list(ctx, 1, null));
            RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("user")
                    .executes((ctx) -> this.list(ctx, 1, this.getUserArgumentInput(ctx, "user")));
            RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                    .executes((ctx) -> this.list(ctx, IntegerArgumentType.getInteger(ctx, "page"), this.getUserArgumentInput(ctx, "user")));

            userArgument.then(pageArgument);
            listArgument.then(userArgument);
            commandNode.addChild(listArgument.build());
        }

        {
            LiteralArgumentBuilder<ServerCommandSource> listArgument = literal("info");
            RequiredArgumentBuilder<ServerCommandSource, String> warpArgument = argument("warp", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::info);

            listArgument.then(warpArgument);
            commandNode.addChild(listArgument.build());
        }

        {
            LiteralArgumentBuilder<ServerCommandSource> teleportArgument = literal("teleport");
            RequiredArgumentBuilder<ServerCommandSource, String> warpArgument = argument("warp", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::teleport);

            teleportArgument.then(warpArgument);
            commandNode.addChild(teleportArgument.build());
        }

    }

    private int add(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        String input = StringArgumentType.getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");
        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (!canSet(user) && !user.hasPermission(CommandPermission.PLAYER_WARP_LIMIT_BYPASS)) {
            user.sendMessage(messages.commands().playerWarp().limitReached);
            return FAILED;
        }

        if (warp != null && !warp.getOwner().equals(user.getUuid())) {
            user.sendLangMessage(messages.commands().playerWarp().nameAlreadyTaken);
            return FAILED;
        }

        if (name.length() > 20) {
            user.sendLangMessage("command.playerwarp.name_too_long");
            return FAILED;
        }

        String type = StringArgumentType.getString(ctx, "type");
        String desc = StringArgumentType.getString(ctx, "description");

        if (TextFormat.removeAlternateColorCodes('&', desc).length() > 100) {
            user.sendLangError("command.playerwarp.desc_too_long");
            return FAILED;
        }

        if (!PlayerWarp.Type.isValid(type)) {
            user.sendLangError("command.playerwarp.invalid_type", type);
            return FAILED;
        }

        if (warp != null && !user.hasPermission(CommandPermission.PLAYER_WARP_OTHERS)) {
            user.sendError(KiloCommands.getPermissionError(CommandPermission.PLAYER_WARP_OTHERS.getNode()));
            return FAILED;
        } else if (warp != null && !input.startsWith("-confirmed-")) {
            user.sendMessage(getConfirmationText(name, ""));
            return AWAIT;
        } else {
            PlayerWarpsManager.addWarp(new PlayerWarp(name, user.getLocation(), user.getUuid(), type, desc));
        }

        user.sendMessage(messages.commands().playerWarp().warpSet.replace("{NAME}", name));
        return SUCCESS;
    }

    private int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        String input = StringArgumentType.getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (warp == null) {
            user.sendLangMessage("command.playerwarp.invalid_warp");
            return FAILED;
        }

        if (!warp.getOwner().equals(user.getUuid()) && !user.hasPermission(CommandPermission.PLAYER_WARP_OTHERS)) {
            user.sendError(KiloCommands.getPermissionError(CommandPermission.PLAYER_WARP_OTHERS.getNode()));
            return FAILED;
        }

        if (!input.startsWith("-confirmed-")) {
            user.sendMessage(getRemoveConfirmationText(name));
            return FAILED;
        }

        PlayerWarpsManager.removeWarp(name);

        user.sendMessage(messages.commands().playerWarp().warpRemoved.replace("{NAME}", name));
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

        this.getEssentials().getUserThenAcceptAsync(src, inputName, (user) -> {
            this.sendList(src.getCommandSource(), user, page);
        });

        return AWAIT;
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String inputName = StringArgumentType.getString(ctx, "warp");

        PlayerWarp warp = PlayerWarpsManager.getWarp(inputName);

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

        this.getEssentials().getUserThenAcceptAsync(src, warp.getOwner(), (user) -> sendInfo(src, warp, user));
        return AWAIT;
    }

    private int teleport(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String inputName = StringArgumentType.getString(ctx, "warp");
        String warpName = inputName.replaceFirst("-confirmed-", "");

        PlayerWarp warp = PlayerWarpsManager.getWarp(warpName);

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

//        try {
//            LocationUtil.validateIsSafe(warp.getLocation());
//        } catch (InsecureDestinationException e) {
//            if (!inputName.startsWith("-confirmed-")) {
//                src.sendMessage(getTeleportConfirmationText(warpName));
//                return -1;
//            }
//        }

        src.teleport(warp.getLocation(), true);
        src.sendMessage(new TextMessage(
                KiloConfig.messages().commands().warp().teleportTo
                        .replace("{WARP_NAME}", warp.getName()),
                true
        ));

        return SUCCESS;
    }

    private void sendInfo(OnlineUser src, PlayerWarp warp, User owner) {
        Texter.InfoBlockStyle text = Texter.InfoBlockStyle.of("Player Warp: " + warp.getName());
        text.append("Owner", owner.getNameTag()).append(" ");
        text.append("Type", warp.getType()).append(" ");
        text.append("World", RegistryUtils.dimensionToName(owner.getLocation().getDimensionType()));

        src.sendMessage(text.build());
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

        ListedText.Page paged = ListedText.getPageFromStrings(ListedText.Options.builder().setPageIndex(page - 1).build(), input.getLines());
        paged.send(src, "Player Warps: " + user.getNameTag(), "/playerwarps " + src.getName() + " %page%");
    }

    private static boolean canSet(User user) {
        for (int i = 0; i < KiloConfig.main().homesLimit; i++) {
            String thisPerm = "kiloessentials.command.player_warp.limit." + i;
            int allowed = Integer.parseInt(thisPerm.split("\\.")[4]);

            if (PlayerWarpsManager.getWarps(user.getUuid()).size() + 1 <= allowed &&
                    KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), thisPerm, 3)) {
                return true;
            }
        }

        return KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS);
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
                Texter.getButton("&7[&eClick here to Confirm&7]", "/pwarp set " + warpName, Texter.newText("Click").formatted(Formatting.GREEN))
        );
    }

    private Text getRemoveConfirmationText(String warpName) {
        return Texter.confirmationMessage(
                "command.playerwarp.remove.confirmation_message",
                Texter.getButton("&7[&eClick here to Confirm&7]", "/pwarp remove -confirmed-" + warpName, Texter.newText("Click").formatted(Formatting.GREEN))
        );
    }

    private Text getTeleportConfirmationText(String warpName) {
        return new LiteralText("")
                .append(LangText.get(true, "general.loc.unsafe.confirmation")
                        .formatted(Formatting.YELLOW))
                .append(new LiteralText(" [").formatted(Formatting.GRAY)
                        .append(new LiteralText("Click here to Confirm").formatted(Formatting.GREEN))
                        .append(new LiteralText("]").formatted(Formatting.GRAY))
                        .styled((style) -> {
                            return style.withFormatting(Formatting.GRAY).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Confirm").formatted(Formatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pwarp teleport -confirmed-" + warpName));
                        }));
    }

}
