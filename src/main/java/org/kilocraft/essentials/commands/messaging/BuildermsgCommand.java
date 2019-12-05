package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.channels.BuilderChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.user.ServerUser;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class BuildermsgCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = dispatcher.register(literal("buildermsg")
                .requires(src -> hasPermission(src, "buildermsg", 2)));

        LiteralCommandNode<ServerCommandSource> joinArg = literal("join")
                .executes(ctx -> executeJoin(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> executeJoin(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> leaveArg = literal("leave")
                .executes(ctx -> executeLeave(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> executeLeave(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        ArgumentCommandNode<ServerCommandSource, String> sendArg = argument("message", greedyString())
                .executes(BuildermsgCommand::executeSned).build();

        rootCommand.addChild(joinArg);
        rootCommand.addChild(leaveArg);
        rootCommand.addChild(sendArg);
    }

    private static int executeJoin(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        KiloServer.getServer().getChatManager().getChannel(BuilderChat.getChannelId()).join((ServerUser) user);
        user.setUpstreamChannelId(BuilderChat.getChannelId());

        return SUCCESS();
    }

    private static int executeLeave(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        KiloServer.getServer().getChatManager().getChannel(BuilderChat.getChannelId()).leave((ServerUser) user);
        user.setUpstreamChannelId(GlobalChat.getChannelId());
        return SUCCESS();
    }

    private static int executeSned(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String message = StringArgumentType.getString(ctx, "message");
        KiloServer.getServer().getChatManager().getChannel(BuilderChat.getChannelId()).onChatMessage(ctx.getSource().getPlayer(), message);
        return SUCCESS();
    }

}
