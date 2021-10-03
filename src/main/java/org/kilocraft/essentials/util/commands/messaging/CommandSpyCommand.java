package org.kilocraft.essentials.util.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;

public class CommandSpyCommand extends EssentialCommand {
    private static final Preference<Boolean> COMMAND_SPY = Preferences.COMMAND_SPY;

    public CommandSpyCommand() {
        super("commandspy", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_COMMAND));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Boolean set = !src.getPreference(COMMAND_SPY);
        src.getPreferences().set(COMMAND_SPY, set);

        if (set) {
            src.sendLangMessage("command.commandspy.active");
        } else {
            src.sendLangMessage("command.commandspy.inactive");
        }

        return set ? SUCCESS : AWAIT;
    }
}
