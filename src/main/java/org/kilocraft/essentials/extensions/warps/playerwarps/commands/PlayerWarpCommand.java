package org.kilocraft.essentials.extensions.warps.playerwarps.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarp;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.util.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerWarpCommand extends EssentialCommand {
    public PlayerWarpCommand(String label, CommandPermission PERMISSION, String[] alias) {
        super(label, PERMISSION, alias);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        {
            final LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set");
            final RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word());
            final RequiredArgumentBuilder<ServerCommandSource, String> typeArgument = argument("type", StringArgumentType.word())
                    .suggests(this::typeSuggestions)
                    .executes(this::set);

            nameArgument.then(typeArgument);
            setArgument.then(nameArgument);
            commandNode.addChild(setArgument.build());
        }

        {
            final LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove");
            final RequiredArgumentBuilder<ServerCommandSource, String> nameArgument = argument("name", StringArgumentType.word())
                    .suggests(this::warpSuggestions)
                    .executes(this::remove);

            removeArgument.then(nameArgument);
            commandNode.addChild(removeArgument.build());
        }


    }

    private int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        String input = StringArgumentType.getString(ctx, "name");
        String name = input.replaceFirst("-confirmed-", "");
        String type = StringArgumentType.getString(ctx, "type");

        for (PlayerWarp warp : PlayerWarpsManager.getWarps()) {
            if (warp.getName().equalsIgnoreCase(name) && warp.getOwner() != user.getUuid()) {
                user.sendLangMessage("command.playerwarps.already_set");
                return SINGLE_FAILED;
            }
        }

        if (!PlayerWarp.Type.isValid(type)) {
            user.sendLangMessage("command.playerwarps.invalid_type", type);
            return SINGLE_FAILED;
        }

        if (PlayerWarpsManager.getWarpsByName().contains(name) && !input.startsWith("-confirmed-")) {
            user.sendMessage(getConfirmationText(name, ""));
            return AWAIT_RESPONSE;
        } else {
            PlayerWarpsManager.addWarp(
                    new PlayerWarp(
                            name,
                            user.getLocation(),
                            user.getUuid(),
                            type
                    )
            );

        }


        user.sendLangMessage("command.playerwarps.set", name);
        return SINGLE_SUCCESS;
    }

    private int remove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx);
        String name = StringArgumentType.getString(ctx, "name");

        if (!PlayerWarpsManager.getWarpsByName().contains(name)) {
            user.sendLangMessage("command.playerwarps.invalid_warp");
            return SINGLE_FAILED;
        }

        PlayerWarpsManager.removeWarp(name);

        user.sendLangMessage("command.playerwarps.remove", name);
        return SINGLE_SUCCESS;
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
                "command.playerwarps.set.confirmation_message",
                Texter.getButton("Confirm", "/pwarp set " + warpName, Texter.toText("Click").formatted(Formatting.GREEN))
        );
    }

}
