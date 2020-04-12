package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.setting.Settings;

public class DonotdisturbCommand extends EssentialCommand {
    private Setting<Boolean> SETTING = Settings.DON_NOT_DISTURB;
    public DonotdisturbCommand() {
        super("donotdisturb", new String[]{"dontdisturb", "toggledisturb"});
        this.withUsage("command.donotdisturb.usage");
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::toggle);
    }

    private int toggle(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final ServerUser user = (ServerUser) this.getOnlineUser(ctx);

        user.getSettings().set(SETTING, !user.getSetting(SETTING));

        if (user.getSetting(SETTING)) {
            ((OnlineUser) user).sendLangMessage("command.donotdisturb.on");
        } else {
            ((OnlineUser) user).sendLangMessage("command.donotdisturb.off");
        }

        return SINGLE_SUCCESS;
    }

}
