package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.user.setting.Settings;

public class CommandSpyCommand extends EssentialCommand {
    private static final Setting<Boolean> COMMAND_SPY = Settings.COMMAND_SPY;

    public CommandSpyCommand() {
        super("commandspy", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_COMMAND));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Boolean set = !src.getSetting(COMMAND_SPY);
        src.getSettings().set(COMMAND_SPY, set);

        if (set) {
            src.sendLangMessage("command.commandspy.active");
        } else {
            src.sendLangMessage("command.commandspy.inactive");
        }

        return set ? SUCCESS : AWAIT;
    }
}
