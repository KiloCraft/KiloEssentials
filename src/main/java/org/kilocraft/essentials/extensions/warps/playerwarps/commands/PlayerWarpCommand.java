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
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.schedule.SinglePlayerScheduler;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.settings.ServerSettings;
import org.kilocraft.essentials.util.text.ListedText;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

public class PlayerWarpCommand extends EssentialCommand {
    private static final String HEADER = ModConstants.translation("command.playerwarp.header");

    public PlayerWarpCommand(String label, Predicate<CommandSourceStack> predicate, String[] alias) {
        super(label, predicate, alias);
        this.withUsage("command.playerwarp.usage", "add", "name", "type", "description");
    }

    private static boolean canSet(User user) {
        for (int i = 0; i < KiloConfig.main().homesLimit; i++) {
            String thisPerm = "kiloessentials.command.player_warp.limit." + i;
            int allowed = Integer.parseInt(thisPerm.split("\\.")[4]);

            if (PlayerWarpsManager.getWarps(user.getUuid()).size() + 1 <= allowed &&
                    KiloEssentials.hasPermissionNode(((OnlineUser) user).getCommandSource(), thisPerm, 3)) {
                return true;
            }
        }

        return KiloCommands.hasPermission(((OnlineUser) user).getCommandSource(), CommandPermission.HOME_SET_LIMIT_BYPASS);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        {
            LiteralArgumentBuilder<CommandSourceStack> addArgument = this.literal("add");
            RequiredArgumentBuilder<CommandSourceStack, String> nameArgument = this.argument("name", StringArgumentType.word())
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_type"));
            RequiredArgumentBuilder<CommandSourceStack, String> typeArgument = this.argument("type", StringArgumentType.word())
                    .suggests(this::typeSuggestions)
                    .executes((ctx) -> this.sendUsage(ctx, "command.playerwarp.usage.provide_desc"));
            RequiredArgumentBuilder<CommandSourceStack, String> descArgument = this.argument("description", StringArgumentType.greedyString())
                    .executes(this::add);

            typeArgument.then(descArgument);
            nameArgument.then(typeArgument);
            addArgument.then(nameArgument);
            this.commandNode.addChild(addArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> removeArgument = this.literal("remove");
            RequiredArgumentBuilder<CommandSourceStack, String> nameArgument = this.argument("name", StringArgumentType.word())
                    .suggests(this::personalWarpSuggestions)
                    .executes(this::remove);

            removeArgument.then(nameArgument);
            this.commandNode.addChild(removeArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> listArgument = this.literal("list")
                    .executes((ctx) -> this.list(ctx, 1, null));
            RequiredArgumentBuilder<CommandSourceStack, String> userArgument = this.getUserArgument("user")
                    .executes((ctx) -> this.list(ctx, 1, this.getUserArgumentInput(ctx, "user")));
            RequiredArgumentBuilder<CommandSourceStack, Integer> pageArgument = this.argument("page", IntegerArgumentType.integer(1))
                    .executes((ctx) -> this.list(ctx, IntegerArgumentType.getInteger(ctx, "page"), this.getUserArgumentInput(ctx, "user")));

            userArgument.then(pageArgument);
            listArgument.then(userArgument);
            this.commandNode.addChild(listArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> setOwnerArgument = this.literal("setowner")
                    .requires(src -> KiloCommands.hasPermission(src, CommandPermission.PLAYER_WARP_ADMIN));
            RequiredArgumentBuilder<CommandSourceStack, String> warpArgument = this.argument("warp", StringArgumentType.word())
                    .suggests(this::allWarpSuggestions);
            RequiredArgumentBuilder<CommandSourceStack, String> ownerArgument = this.getUserArgument("owner")
                    .executes(this::setOwner);

            warpArgument.then(ownerArgument);
            setOwnerArgument.then(warpArgument);
            this.commandNode.addChild(setOwnerArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> setNameArgument = this.literal("setname")
                    .requires(src -> KiloCommands.hasPermission(src, CommandPermission.PLAYER_WARP_ADMIN));
            RequiredArgumentBuilder<CommandSourceStack, String> warpArgument = this.argument("warp", StringArgumentType.word())
                    .suggests(this::allWarpSuggestions);
            RequiredArgumentBuilder<CommandSourceStack, String> nameArgument = this.argument("name", StringArgumentType.word())
                    .executes(this::setName);

            warpArgument.then(nameArgument);
            setNameArgument.then(warpArgument);
            this.commandNode.addChild(setNameArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> setDescriptionArgument = this.literal("setdescription")
                    .requires(src -> KiloCommands.hasPermission(src, CommandPermission.PLAYER_WARP_ADMIN));
            RequiredArgumentBuilder<CommandSourceStack, String> warpArgument = this.argument("warp", StringArgumentType.word())
                    .suggests(this::allWarpSuggestions);
            RequiredArgumentBuilder<CommandSourceStack, String> descriptionArgument = this.argument("description", StringArgumentType.greedyString())
                    .executes(this::setDescription);

            warpArgument.then(descriptionArgument);
            setDescriptionArgument.then(warpArgument);
            this.commandNode.addChild(setDescriptionArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> listArgument = this.literal("info");
            RequiredArgumentBuilder<CommandSourceStack, String> warpArgument = this.argument("warp", StringArgumentType.word())
                    .suggests(this::allWarpSuggestions)
                    .executes(this::info);

            listArgument.then(warpArgument);
            this.commandNode.addChild(listArgument.build());
        }

        {
            LiteralArgumentBuilder<CommandSourceStack> teleportArgument = this.literal("teleport");
            RequiredArgumentBuilder<CommandSourceStack, String> warpArgument = this.argument("warp", StringArgumentType.word())
                    .suggests(this::allWarpSuggestions)
                    .executes(this::teleport);

            teleportArgument.then(warpArgument);
            this.commandNode.addChild(teleportArgument.build());
        }

    }

    private int add(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        String name = StringArgumentType.getString(ctx, "name");
        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (!canSet(user) && !user.hasPermission(CommandPermission.PLAYER_WARP_LIMIT_BYPASS)) {
            user.sendLangMessage("command.playerwarp.limit");
            return FAILED;
        }

        if (warp != null && !warp.getOwner().equals(user.getUuid())) {
            user.sendLangMessage("command.playerwarp.name_taken");
            return FAILED;
        }

        if (!name.matches("[\\w]{4,20}")) {
            user.sendLangMessage("command.playerwarp.name_invalid");
            return FAILED;
        }

        String type = StringArgumentType.getString(ctx, "type");
        String desc = StringArgumentType.getString(ctx, "description");

        if (ComponentText.clearFormatting(desc).length() > 100) {
            user.sendLangError("command.playerwarp.desc_too_long");
            return FAILED;
        }

        if (!PlayerWarp.Type.isValid(type)) {
            user.sendLangError("command.playerwarp.invalid_type", type);
            return FAILED;
        }

        if (warp != null && !user.hasPermission(CommandPermission.PLAYER_WARP_ADMIN)) {
            user.sendPermissionError(CommandPermission.PLAYER_WARP_ADMIN.getNode());
            return FAILED;
        } else {
            PlayerWarpsManager.addWarp(new PlayerWarp(name, user.getLocation(), user.getUuid(), type, desc));
        }

        user.sendLangMessage("command.playerwarp.set", name);
        return SUCCESS;
    }

    private int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        String input = StringArgumentType.getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");

        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (warp == null) {
            user.sendLangMessage("command.playerwarp.invalid_warp");
            return FAILED;
        }

        if (!warp.getOwner().equals(user.getUuid()) && !user.hasPermission(CommandPermission.PLAYER_WARP_ADMIN)) {
            user.sendPermissionError(CommandPermission.PLAYER_WARP_ADMIN.getNode());
            return FAILED;
        }

        if (!input.startsWith("-confirmed-")) {
            user.sendMessage(this.getRemoveConfirmationText(name));
            return FAILED;
        }

        PlayerWarpsManager.removeWarp(name);

        user.sendLangMessage("command.playerwarp.remove", name);
        return SUCCESS;
    }

    private int list(CommandContext<CommandSourceStack> ctx, int page, @Nullable String inputName) {
        final CommandSourceUser src = this.getCommandSource(ctx);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (inputName == null) {
            this.sendList(src.getCommandSource(), src, page);
            return SUCCESS;
        }

        if (this.isOnline(inputName)) {
            if (!inputName.equals(src.getUsername()) && !src.hasPermission(CommandPermission.PLAYER_WARP_ADMIN)) {
                src.sendLangError("command.exception.permission");
                return FAILED;
            }

            this.sendList(src.getCommandSource(), this.getOnlineUser(inputName), page);
            return SUCCESS;
        }

        this.getUserManager().getUserThenAcceptAsync(src, inputName, (user) -> {
            this.sendList(src.getCommandSource(), user, page);
        });

        return AWAIT;
    }

    private int setOwner(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);

        String name = StringArgumentType.getString(ctx, "warp");

        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (warp == null) {
            src.sendLangMessage("command.playerwarp.invalid_warp");
            return FAILED;
        }

        this.getUserManager().getUserThenAcceptAsync(src, this.getUserArgumentInput(ctx, "owner"), (user) -> {
            warp.setOwner(user.getUuid());
            src.sendLangMessage("command.playerwarp.change_property", "owner", warp.getName(), user.getFormattedDisplayName());
        });

        return AWAIT;
    }

    private int setName(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);

        String name = StringArgumentType.getString(ctx, "warp");

        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

        String warpName = StringArgumentType.getString(ctx, "name");
        if (!warpName.matches("[\\w]{4,20}")) {
            src.sendLangMessage("command.playerwarp.name_invalid");
            return FAILED;
        }

        src.sendLangMessage("command.playerwarp.change_property", "name", warp.getName(), warpName);
        warp.setName(warpName);

        return SUCCESS;
    }

    private int setDescription(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);

        String name = StringArgumentType.getString(ctx, "warp");

        PlayerWarp warp = PlayerWarpsManager.getWarp(name);

        if (PlayerWarpsManager.getWarps().isEmpty()) {
            src.sendLangError("command.playerwarp.no_warp");
            return FAILED;
        }

        if (warp == null) {
            src.sendLangMessage("command.playerwarp.invalid_warp");
            return FAILED;
        }

        String warpDescription = StringArgumentType.getString(ctx, "description");

        if (ComponentText.clearFormatting(warpDescription).length() > 100) {
            src.sendLangError("command.playerwarp.desc_too_long");
            return FAILED;
        }

        src.sendLangMessage("command.playerwarp.change_property", "description", warp.getName(), "\"" + warpDescription + "\"");
        warp.setDescription(warpDescription);

        return SUCCESS;
    }

    private int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String inputName = StringArgumentType.getString(ctx, "warp");

        PlayerWarp warp = PlayerWarpsManager.getWarp(inputName);

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

        this.getUserManager().getUserThenAcceptAsync(src, warp.getOwner(), (user) -> this.sendInfo(src, warp, user));
        return AWAIT;
    }

    private int teleport(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String inputName = StringArgumentType.getString(ctx, "warp");
        String warpName = inputName.replaceFirst("-confirmed-", "");

        PlayerWarp warp = PlayerWarpsManager.getWarp(warpName);

        if (warp == null) {
            src.sendLangError("command.playerwarp.invalid_warp");
            return FAILED;
        }

        if (LocationUtil.isDestinationToClose(src, warp.getLocation())) {
            return IEssentialCommand.FAILED;
        }

        // Add a custom ticket to gradually preload chunks
        warp.getLocation().getWorld().getChunkSource().addRegionTicket(TicketType.create("pwarp", Integer::compareTo, (KiloConfig.main().server().cooldown + 1) * 20), new ChunkPos(warp.getLocation().toPos()), 1, src.asPlayer().getId());
        new SinglePlayerScheduler(src, 1, KiloConfig.main().server().cooldown, () -> {
            src.sendLangMessage("command.playerwarp.teleport", warp.getName());
            src.teleport(warp.getLocation(), true);
        });
        return SUCCESS;
    }

    private void sendInfo(OnlineUser src, PlayerWarp warp, User owner) {
        Texter.InfoBlockStyle text = Texter.InfoBlockStyle.of("Player Warp: " + warp.getName());
        text.append("Owner", owner.getNameTag()).append(" ");
        text.append("Type", warp.getType()).append(" ");
        text.append("World", RegistryUtils.dimensionToName(owner.getLocation().getDimensionType()));

        src.sendMessage(text.build());
    }

    private void sendList(CommandSourceStack src, User user, int page) {
        final String LINE_FORMAT = ModConstants.translation("command.playerwarp.format");
        List<PlayerWarp> warps = PlayerWarpsManager.getWarps(user.getUuid());
        Collections.sort(warps);

        TextInput input = new TextInput(HEADER);

        for (int i = 0; i < warps.size(); i++) {
            PlayerWarp warp = warps.get(i);
            input.append(String.format(LINE_FORMAT, i + 1, warp.getName(), warp.getType(), RegistryUtils.dimensionToName(warp.getLocation().getDimensionType())));
        }

        ListedText.Page paged = ListedText.getPageFromStrings(ListedText.Options.builder().setPageIndex(page - 1).build(), input.getLines());
        paged.send(src, "Player Warps: " + user.getNameTag(), "/playerwarps " + src.getTextName() + " %page%");
    }

    private CompletableFuture<Suggestions> personalWarpSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (CommandUtils.isPlayer(context.getSource())) {
            List<String> strings = new ArrayList<>();
            UUID uuid = context.getSource().getPlayerOrException().getUUID();
            for (PlayerWarp warp : KiloCommands.hasPermission(context.getSource(), CommandPermission.PLAYER_WARP_ADMIN) ? PlayerWarpsManager.getWarps() : PlayerWarpsManager.getWarps(uuid)) {
                strings.add(warp.getName());
            }

            return SharedSuggestionProvider.suggest(strings, builder);
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> allWarpSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        if (CommandUtils.isPlayer(context.getSource())) {
            List<String> strings = new ArrayList<>();
            for (PlayerWarp warp : PlayerWarpsManager.getWarps()) {
                strings.add(warp.getName());
            }

            return SharedSuggestionProvider.suggest(strings, builder);
        }

        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> typeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(new ArrayList<>(PlayerWarp.Type.getTypes()), builder);
    }

    private Component getRemoveConfirmationText(String warpName) {
        return Texter.confirmationMessage(
                "command.playerwarp.remove.confirmation_message",
                Texter.getButton("&7[&eClick here to Confirm&7]", "/pwarp remove -confirmed-" + warpName, Texter.newText("Click").withStyle(ChatFormatting.GREEN))
        );
    }

}
