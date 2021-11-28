package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.api.util.EntityIdentifiable;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.PunishEvents;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.Collection;
import java.util.Date;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.players.UserBanListEntry;

public class BanCommand extends EssentialCommand {
    public BanCommand() {
        super("ke_ban", CommandPermission.BAN);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, GameProfileArgument.Result> victim = this.argument("profile", GameProfileArgument.gameProfile())
                .suggests(ArgumentSuggestions::allPlayers)
                .executes((ctx) -> this.execute(ctx, null, false));

        RequiredArgumentBuilder<CommandSourceStack, String> reason = this.argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), false));

        LiteralArgumentBuilder<CommandSourceStack> silent = this.literal("-silent").then(
                this.argument("profile", GameProfileArgument.gameProfile())
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes((ctx) -> this.execute(ctx, null, true))
                        .then(
                                this.argument("reason", StringArgumentType.greedyString())
                                        .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), true))
                        )
        );

        victim.then(reason);
        this.argumentBuilder.then(silent);
        this.argumentBuilder.then(victim);
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, @Nullable final String reason, boolean silent) throws CommandSyntaxException {
        CommandSourceUser src = this.getCommandSource(ctx);
        Date date = new Date();
        Collection<GameProfile> gameProfiles = GameProfileArgument.getGameProfiles(ctx, "profile");
        if (gameProfiles.size() > 1) {
            throw KiloCommands.getException("exception.too_many_selections").create();
        }
        GameProfile victim = gameProfiles.iterator().next();

        UserBanListEntry entry = new UserBanListEntry(victim, date, src.getName(), null, reason);
        KiloEssentials.getMinecraftServer().getPlayerList().getBans().add(entry);

        if (super.isOnline(victim.getId())) {
            super.getOnlineUser(victim.getId()).asPlayer().connection.disconnect(
                    ComponentText.toText(
                            ServerUserManager.replaceBanVariables(KiloConfig.main().moderation().messages().permBan, entry, true)
                    )
            );
        }
        PunishEvents.BAN.invoker().onBan(src, EntityIdentifiable.fromGameProfile(victim), entry.getReason(), false, -1L, silent);
        this.getUserManager().onPunishmentPerformed(src, new Punishment(src, EntityIdentifiable.fromGameProfile(victim), reason), Punishment.Type.BAN, null, silent);

        return SUCCESS;
    }


}
