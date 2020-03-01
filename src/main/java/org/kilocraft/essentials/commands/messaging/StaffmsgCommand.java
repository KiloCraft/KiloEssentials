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
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.ChatChannel;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.chat.channels.StaffChat;

import java.util.UUID;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class StaffmsgCommand extends EssentialCommand {
    public StaffmsgCommand() {
        super("staffmsg", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.CHAT_CHANNEL_STAFFMSG));
    }
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> listArg = literal("list")
                .executes(StaffmsgCommand::executeList).build();

        LiteralCommandNode<ServerCommandSource> joinArg = literal("on")
                .executes(ctx -> executeJoin(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(TabCompletions::allPlayers)
                        .executes(ctx -> executeJoin(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> leaveArg = literal("off")
                .executes(ctx -> executeLeave(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(TabCompletions::allPlayers)
                        .executes(ctx -> executeLeave(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        ArgumentCommandNode<ServerCommandSource, String> sendArg = argument("message", greedyString())
                .executes(StaffmsgCommand::executeSend).build();

        commandNode.addChild(listArg);
        commandNode.addChild(joinArg);
        commandNode.addChild(leaveArg);
        commandNode.addChild(sendArg);
    }

    private static int executeJoin(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.setUpstreamChannelId(StaffChat.getChannelId());
        KiloChat.sendLangMessageTo(source, "command.setchannel.set_upstream",
                user.getUpstreamChannelId(), user.getRankedDisplayName().asFormattedString());

        return SINGLE_SUCCESS;
    }

    private static int executeLeave(ServerCommandSource source, ServerPlayerEntity player) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.setUpstreamChannelId(GlobalChat.getChannelId());
        KiloChat.sendLangMessageTo(source, "command.setchannel.set_upstream",
                user.getUpstreamChannelId(), user.getRankedDisplayName().asFormattedString());

        return SINGLE_SUCCESS;
    }

    private static int executeSend(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String message = StringArgumentType.getString(ctx, "message");
        KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).onChatMessage(ctx.getSource().getPlayer(), message);
        return SINGLE_SUCCESS;
    }

    private static int executeList(CommandContext<ServerCommandSource> ctx) {
        ChatChannel channel = KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId());
        Text text = new LiteralText(channel.getId() + " channel's subscribers:").formatted(Formatting.YELLOW);
        for (UUID subscriber : KiloServer.getServer().getChatManager().getChannel(StaffChat.getChannelId()).getSubscribers()) {
            OnlineUser user = KiloServer.getServer().getOnlineUser(subscriber);

            text.append(new LiteralText("\n- ").formatted(Formatting.GRAY))
                .append(TextFormat.translate(user.getRankedDisplayName().asFormattedString()));
        }

        ctx.getSource().sendFeedback(text, false);
        return SINGLE_SUCCESS;
    }

}
