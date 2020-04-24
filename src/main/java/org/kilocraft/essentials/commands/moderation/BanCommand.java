package org.kilocraft.essentials.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.user.PunishmentManager;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.Collection;

public class BanCommand extends EssentialCommand {
    public BanCommand() {
        super(
                "ke_ban",
                src ->
                        src.getMinecraftServer().getPlayerManager().getUserBanList().isEnabled() &&
                                (
                                        KiloCommands.hasPermission(src, CommandPermission.BAN_IP) ||
                                        KiloCommands.hasPermission(src, CommandPermission.BAN_PROFILE)
                                )
        );

        this.withUsage("command.ban.usage", "profile/username", "reason");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, GameProfileArgumentType.GameProfileArgument> profileArgument = argument("profile", GameProfileArgumentType.gameProfile())
                .executes((ctx) -> this.ban(ctx, null));
        final RequiredArgumentBuilder<ServerCommandSource, String> reasonArgument = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.ban(ctx, StringArgumentType.getString(ctx, "reason")));

        profileArgument.then(reasonArgument);
        commandNode.addChild(profileArgument.build());
    }

    private int ban(CommandContext<ServerCommandSource> ctx, @Nullable String reason) throws CommandSyntaxException {
        final CommandSourceUser src = this.getServerUser(ctx);
        final Collection<GameProfile> collection = GameProfileArgumentType.getProfileArgument(ctx, "profile");
        if (collection.size() > 1) {
            throw KiloCommands.getException(ExceptionMessageNode.TOO_MANY_SELECTIONS).create();
        }

        final GameProfile target = collection.iterator().next();
        final PunishmentManager manager = KiloServer.getServer().getUserManager().getPunishmentManager();

        if (!manager.shouldBan(target, reason)) {
            src.sendError(tl("command.ban.failed", target.getName()));
            return FAILED;
        }

        manager.ban(target, src.getUsername(), reason);

        src.sendLangMessage("command.ban.banned", target.getName(), reason);
        return SUCCESS;
    }
}
