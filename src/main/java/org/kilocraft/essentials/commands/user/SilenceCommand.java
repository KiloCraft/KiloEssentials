package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.setting.Settings;

public class SilenceCommand extends EssentialCommand {
    public SilenceCommand() {
        super("silence");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        boolean set = !user.getSetting(Settings.SOUNDS);
        user.getSettings().set(Settings.SOUNDS, set);

        if (set) {
            user.sendLangMessage("command.silence.on");
        } else {
            user.sendLangMessage("command.silence.off");
        }

        return SINGLE_SUCCESS;
    }

}
