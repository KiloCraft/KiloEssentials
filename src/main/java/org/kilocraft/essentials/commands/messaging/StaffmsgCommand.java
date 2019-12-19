package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.chat.channels.StaffChat;
import org.kilocraft.essentials.user.ServerUser;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;
import static org.kilocraft.essentials.KiloCommands.hasPermission;

public class StaffmsgCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> rootCommand = dispatcher.register(literal("staffmsg")
            .requires(src -> hasPermission(src, "staffmsg", 2)));

        LiteralCommandNode<ServerCommandSource> listArg = literal("list")
                .executes(StaffmsgCommand::executeList).build();

        LiteralCommandNode<ServerCommandSource> joinArg = literal("join")
                .executes(ctx -> executeJoin(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(TabCompletions::allPlayers)
                        .executes(ctx -> executeJoin(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> leaveArg = literal("leave")
                .executes(ctx -> executeLeave(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(TabCompletions::allPlayers)
                        .executes(ctx -> executeLeave(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> receiveArg = literal("receive")
                .executes(StaffmsgCommand::executeToggleReceive)
                .then(argument("set", BoolArgumentType.bool())
                        .executes(ctx -> executeSetReceive(ctx, BoolArgumentType.getBool(ctx, "set")))).build();

        ArgumentCommandNode<ServerCommandSource, String> sendArg = argument("message", greedyString())
                .executes(StaffmsgCommand::executeSend).build();

        rootCommand.addChild(receiveArg);
        rootCommand.addChild(listArg);
        rootCommand.addChild(joinArg);
        rootCommand.addChild(leaveArg);
        rootCommand.addChild(sendArg);
    }

    private static int executeJoin(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).join((ServerUser) user);
        user.setUpstreamChannelId(StaffChat.getChannelId());

        return SUCCESS();
    }

    private static int executeLeave(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).leave((ServerUser) user);
        user.setUpstreamChannelId(GlobalChat.getChannelId());
        return SUCCESS();
    }

    private static int executeSend(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String message = StringArgumentType.getString(ctx, "message");
        KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).onChatMessage(ctx.getSource().getPlayer(), message);
        return SUCCESS();
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) {
        ChatChannel channel = KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId());
        Text text = new LiteralText(channel.getId() + " channel's subscribers:").formatted(Formatting.YELLOW);
        for (UUID subscriber : KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).getSubscribers()) {
            OnlineUser user = KiloServer.getServer().getOnlineUser(subscriber);

            text.append(new LiteralText("\n- ").formatted(Formatting.GRAY))
                .append(user.getRankedDisplayname());
        }

        ctx.getSource().sendFeedback(text, false);
        return SUCCESS();
    }

    private static int executeToggleReceive(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());
        executeSetReceive(ctx, !KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).isSubscribed(user));

        return SUCCESS();
    }

    private static int executeSetReceive(CommandContext<ServerCommandSource> ctx, boolean bool) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());

        if (!bool) {
            KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).leave((ServerUser) user);
            KiloChat.sendLangMessageTo(ctx.getSource(), "command.setchannel.unsubscribed", StaffChat.getChannelId());
            return SUCCESS();
        }

        KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).join((ServerUser) user);
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.setchannel.subscribed", StaffChat.getChannelId());

        return SUCCESS();
    }

}
