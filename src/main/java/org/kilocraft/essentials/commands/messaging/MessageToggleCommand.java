package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.ServerUser;

public class MessageToggleCommand extends EssentialCommand {
    public MessageToggleCommand() {
        super("messagetoggle", new String[]{"msgtoggle", "togglemessages", "togglemsg"});
        this.withUsage("command.messagetoggle.usage");
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::toggle);
    }

    private int toggle(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerUser user = (ServerUser) this.getOnlineUser(ctx);

        user.setAcceptsMessages(!user.acceptsMessages());

        if (user.acceptsMessages()) {
            ((OnlineUser) user).sendLangMessage("command.messagetoggle.on");
        } else {
            ((OnlineUser) user).sendLangMessage("command.messagetoggle.off");
        }

        return SINGLE_SUCCESS;
    }

}
