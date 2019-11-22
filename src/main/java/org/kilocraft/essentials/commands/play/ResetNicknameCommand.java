package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.user.User;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static net.minecraft.server.command.CommandManager.literal;

public class ResetNicknameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();

        LiteralCommandNode<ServerCommandSource> commandRoot = literal("resetnickname")
                .requires(s -> hasPermissionOrOp(s, KiloCommands.getCommandPermission("nick.self"), 2))
                .executes(ResetNicknameCommand::resetSelf)
                .build();

        LiteralCommandNode<ServerCommandSource> aliasResetNick = literal("resetnick").redirect(commandRoot).build();

        LiteralCommandNode<ServerCommandSource> aliasClearNick = literal("clearnick").redirect(commandRoot).build();
        LiteralCommandNode<ServerCommandSource> aliasClearNickname = literal("clearnickname").redirect(commandRoot).build();

        root.addChild(commandRoot);
        root.addChild(aliasResetNick);
        root.addChild(aliasClearNick);
        root.addChild(aliasClearNickname);
    }

    private static int resetSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        User user = KiloServer.getServer().getUserManager().getOnline(player);
        user.clearNickname();
        // This is an Optional.ofNullable, so the DataTracker will just reset the name without any other magic since TrackedData is always and automatically synchronized with the client.
        player.setCustomName(null);
        // TODO Langtext issues
        ctx.getSource().sendFeedback(LangText.getFormatter(true, "command.nick.reset"),
                false);
        return 1;
    }
}
