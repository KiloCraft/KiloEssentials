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
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.user.setting.Settings;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class StaffmsgCommand extends EssentialCommand {
    public StaffmsgCommand() {
        super("staffmsg", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.CHAT_CHANNEL_STAFFMSG), new String[]{"sm"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> joinArg = literal("on")
                .executes(ctx -> on(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentCompletions::allPlayers)
                        .executes(ctx -> on(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> leaveArg = literal("off")
                .executes(ctx -> off(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentCompletions::allPlayers)
                        .executes(ctx -> off(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        ArgumentCommandNode<ServerCommandSource, String> sendArg = argument("message", greedyString())
                .executes(this::send).build();

        commandNode.addChild(joinArg);
        commandNode.addChild(leaveArg);
        commandNode.addChild(sendArg);
    }

    private int on(ServerCommandSource source, ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.getSettings().set(Settings.CHAT_CHANNEL, ServerChat.Channel.STAFF);

        user.sendLangMessage("channel.on", "staff");
        return SUCCESS;
    }

    private int off(ServerCommandSource source, ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.getSettings().reset(Settings.CHAT_CHANNEL);

        user.sendLangMessage("channel.off", "staff");
        return SUCCESS;
    }

    private int send(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String message = StringArgumentType.getString(ctx, "message");
        ServerChat.sendSafely(this.getOnlineUser(ctx), new TextMessage(message), ServerChat.Channel.STAFF);
        return SUCCESS;
    }

}
