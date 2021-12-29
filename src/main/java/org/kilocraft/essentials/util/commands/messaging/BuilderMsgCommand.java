package org.kilocraft.essentials.util.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;

import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class BuilderMsgCommand extends EssentialCommand {
    private static final ServerChat.Channel THIS_CHANNEL = ServerChat.Channel.BUILDER;

    public BuilderMsgCommand() {
        super("buildermsg", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.CHAT_CHANNEL_BUILDERMSG.getNode()));
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralCommandNode<CommandSourceStack> joinArg = this.literal("on")
                .executes(ctx -> this.on(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(this.argument("player", EntityArgument.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> this.on(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<CommandSourceStack> leaveArg = this.literal("off")
                .executes(ctx -> this.off(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(this.argument("player", EntityArgument.player()).suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> this.off(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))).build();

        LiteralCommandNode<CommandSourceStack> toggleArg = this.literal("toggle")
                .executes(this::toggle).build();

        ArgumentCommandNode<CommandSourceStack, String> sendArg = this.argument("message", greedyString())
                .executes(this::send).build();

        this.commandNode.addChild(joinArg);
        this.commandNode.addChild(leaveArg);
        this.commandNode.addChild(toggleArg);
        this.commandNode.addChild(sendArg);
    }

    private int on(CommandSourceStack source, ServerPlayer player) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        user.getPreferences().set(Preferences.CHAT_CHANNEL, ServerChat.Channel.STAFF);

        user.sendLangMessage("channel.on", "builder");
        return SUCCESS;
    }

    private int off(CommandSourceStack source, ServerPlayer player) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);
        user.getPreferences().reset(Preferences.CHAT_CHANNEL);

        user.sendLangMessage("channel.off", "builder");
        return SUCCESS;
    }

    private int toggle(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
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

    private int send(CommandContext<CommandSourceStack> ctx) {
        String message = StringArgumentType.getString(ctx, "message");
        ServerChat.sendChatMessage(this.getCommandSource(ctx), message, ServerChat.Channel.BUILDER);
        return SUCCESS;
    }

}
