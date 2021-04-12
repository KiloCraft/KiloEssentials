package org.kilocraft.essentials.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.events.player.PlayerBannedEventImpl;
import org.kilocraft.essentials.events.player.PlayerMutedEventImpl;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.TimeDifferenceUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Collection;
import java.util.Date;

public class TempBanCommand extends EssentialCommand {
    public TempBanCommand() {
        super("tempban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> victim = argument("profile", GameProfileArgumentType.gameProfile())
                .suggests(ArgumentSuggestions::allPlayers);

        RequiredArgumentBuilder<ServerCommandSource, String> time = argument("time", StringArgumentType.word())
                .suggests(TimeDifferenceUtil::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, false));

        RequiredArgumentBuilder<ServerCommandSource, String> reason = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<ServerCommandSource> silent = literal("-silent").then(
                argument("profile", GameProfileArgumentType.gameProfile())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                        argument("time", StringArgumentType.word())
                                .suggests(TimeDifferenceUtil::listSuggestions)
                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), null, true))
                                .then(
                                        argument("reason", StringArgumentType.greedyString())
                                                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "time"), StringArgumentType.getString(ctx, "reason"), true))
                                )
                )
        );

        time.then(reason);
        victim.then(time);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @NotNull final String time, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        Date date = new Date();
        Date expiry = new Date(TimeDifferenceUtil.parse(time, true));
        Collection<GameProfile> gameProfiles = GameProfileArgumentType.getProfileArgument(ctx, "profile");
        if (gameProfiles.size() > 1) {
            throw KiloCommands.getException(ExceptionMessageNode.TOO_MANY_SELECTIONS).create();
        }
        GameProfile victim = gameProfiles.iterator().next();

        BannedPlayerEntry entry = new BannedPlayerEntry(victim, date, src.getName(), expiry, reason);
        super.getServer().getMinecraftServer().getPlayerManager().getUserBanList().add(entry);

        if (super.isOnline(victim.getId())) {
            super.getOnlineUser(victim.getId()).asPlayer().networkHandler.disconnect(
                    ComponentText.toText(
                            ServerUserManager.replaceVariables(super.config.moderation().messages().tempBan, entry, false)
                    )
            );
        }
        KiloServer.getServer().triggerEvent(new PlayerBannedEventImpl(src, EntityIdentifiable.fromGameProfile(victim), reason, expiry.getTime(), silent, false));
        this.getServer().getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.BAN, time, silent);
        return AWAIT;
    }


}
