package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.IEssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.preference.Preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static org.kilocraft.essentials.KiloCommands.getPermissionError;

public class GamemodeCommand extends EssentialCommand {
    public GamemodeCommand() {
        super("ke_gamemode", src ->
                KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_ADVENTURE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_SURVIVAL) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_SPECTATOR) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_SELF_CREATIVE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_ADVENTURE) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_SURVIVAL) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_SPECTATOR) ||
                        KiloCommands.hasPermission(src, CommandPermission.GAMEMODE_OTHERS_CREATIVE),
                new String[]{"gm"});
        this.withUsage("command.gamemode.usage", "mode", "<user> target", "<optional> -silent");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> gameTypeArgument = this.argument("mode", string())
                .suggests(this::suggestGameModes)
                .executes(ctx -> this.execute(ctx, null, ctx.getSource().getName(),false));

        final RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = this.getUserArgument("target")
                .executes(ctx -> this.execute(ctx, null, this.getUserArgumentInput(ctx, "target"),false))
                .then(
                        this.literal("-silent")
                        .executes(ctx -> this.execute(ctx, null, this.getUserArgumentInput(ctx, "target"), true))
                );


        gameTypeArgument.then(targetArgument);
        this.commandNode.addChild(gameTypeArgument.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable final GameMode cValue, final String selection, final boolean silent) throws CommandSyntaxException {
        ServerCommandSource src = ctx.getSource();
        OnlineUser sourceUser = this.getOnlineUser(ctx);
        String arg = cValue == null ? getString(ctx, "mode") : cValue.getName();
        GameMode selectedMode = this.getMode(arg);

        if (selectedMode == null) {
            throw new SimpleCommandExceptionType(new LiteralText("Please select a valid Game type!")).create();
        }

        if (!this.hasPermission(src, this.getPermission("self", selectedMode))) {
            throw new SimpleCommandExceptionType(getPermissionError(this.getPermission("self", selectedMode).getNode())).create();
        }

        AtomicInteger atomicInteger = new AtomicInteger(IEssentialCommand.AWAIT);
        this.getEssentials().getUserThenAcceptAsync(sourceUser, selection, user -> {
            try {
                user.getPreferences().set(Preferences.GAME_MODE, selectedMode);

                if (user.isOnline()) {
                    ((OnlineUser) user).setGameMode(selectedMode);
                }

                user.saveData();
            } catch (final IOException e) {
                sourceUser.sendError(e.getMessage());
            }

            sourceUser.sendLangMessage("template.#1", "GameMode", selectedMode.getName(), user.getNameTag());
        });

        return atomicInteger.get();
    }

    private GameMode getMode(final String arg) {
        if  (arg.startsWith("sp") || arg.equals("3"))
            return GameMode.SPECTATOR;
        if  (arg.startsWith("s") || arg.equals("0"))
            return GameMode.SURVIVAL;
        if  (arg.startsWith("c") || arg.equals("1"))
            return GameMode.CREATIVE;
        if  (arg.startsWith("a") || arg.equals("2"))
            return GameMode.ADVENTURE;

        return null;
    }

    private CommandPermission getPermission(final String type, final GameMode mode) {
        return CommandPermission.byName("gamemode." + type + "." + mode.getName().toLowerCase());
    }

    private CompletableFuture<Suggestions> suggestGameModes(final CommandContext<ServerCommandSource> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        for (GameMode value : GameMode.values()) {
            if (value.equals(GameMode.NOT_SET) || !this.getOnlineUser(context).hasPermission(this.getPermission("self", value))) {
                continue;
            }

            strings.add(value.getName());
        }

        return CommandSource.suggestMatching(strings, builder);
    }

}
