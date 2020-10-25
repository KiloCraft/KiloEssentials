package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.chat.MutableTextMessage;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class StaffMessageCommand extends EssentialCommand {
    private static final ServerChat.Channel THIS_CHANNEL = ServerChat.Channel.STAFF;
    public StaffMessageCommand() {
        super("staffmsg", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.CHAT_CHANNEL_STAFFMSG), new String[]{"sm"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> joinArg = literal("on")
                .executes(ctx -> on(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> on(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> leaveArg = literal("off")
                .executes(ctx -> off(ctx.getSource(), ctx.getSource().getPlayer()))
                .then(argument("player", EntityArgumentType.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> off(ctx.getSource(), EntityArgumentType.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<ServerCommandSource> toggleArg = literal("toggle")
                .executes(this::toggle).build();

        ArgumentCommandNode<ServerCommandSource, String> sendArg = argument("message", greedyString())
                .executes(this::send).build();

        commandNode.addChild(joinArg);
        commandNode.addChild(leaveArg);
        commandNode.addChild(toggleArg);
        commandNode.addChild(sendArg);
    }

    private int on(ServerCommandSource source, ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.getPreferences().set(Preferences.CHAT_CHANNEL, ServerChat.Channel.STAFF);

        user.sendLangMessage("channel.on", "staff");
        return SUCCESS;
    }

    private int off(ServerCommandSource source, ServerPlayerEntity player) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);
        user.getPreferences().reset(Preferences.CHAT_CHANNEL);

        user.sendLangMessage("channel.off", "staff");
        return SUCCESS;
    }

    private int toggle(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        List<ServerChat.Channel> disabled = user.getPreference(Preferences.DISABLED_CHATS);
        if (disabled.contains(THIS_CHANNEL)) {
            disabled.remove(THIS_CHANNEL);
            user.sendLangMessage("channel.toggle.enabled", THIS_CHANNEL.getId());
        } else {
            disabled.add(THIS_CHANNEL);
            user.sendLangMessage("channel.toggle.disabled", THIS_CHANNEL.getId());
        }

        user.getPreferences().set(Preferences.DISABLED_CHATS, disabled);
        return SUCCESS;
    }

    private int send(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String message = StringArgumentType.getString(ctx, "message");
        ServerChat.sendSafely(this.getOnlineUser(ctx), new MutableTextMessage(message), ServerChat.Channel.STAFF);
        return SUCCESS;
    }

}
