package org.kilocraft.essentials.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.user.PunishmentManager;

import java.util.Collection;

public class KickCommand extends EssentialCommand {
    public KickCommand() {
        super(
                "ke_kick",
                CommandPermission.KICK
        );

        this.withUsage("command.kick.usage", "profile/username", "reason");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("targets", EntityArgumentType.players())
                .executes((ctx) -> this.execute(ctx, null));
        final RequiredArgumentBuilder<ServerCommandSource, String> reasonArgument = argument("reason", StringArgumentType.greedyString())
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "reason")));

        selectorArgument.then(reasonArgument);
        commandNode.addChild(selectorArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx, String reason) throws CommandSyntaxException {
        final PunishmentManager manager = KiloServer.getServer().getUserManager().getPunishmentManager();
        final Collection<ServerPlayerEntity> collection = EntityArgumentType.getPlayers(ctx, "targets");

        for (ServerPlayerEntity player : collection) {
            manager.kick(player, reason);
        }

        if (collection.size() == 1) {
            this.getServerUser(ctx).sendLangMessage("command.kick.singleton", collection.iterator().next().getEntityName(), reason);
        } else {
            this.getServerUser(ctx).sendLangMessage("command.kick.multiple", collection.size(), reason);
        }

        return collection.size();
    }
}
