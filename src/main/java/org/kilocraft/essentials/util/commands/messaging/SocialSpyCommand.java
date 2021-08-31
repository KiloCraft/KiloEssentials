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

public class SocialSpyCommand extends EssentialCommand {
    private static final Preference<Boolean> SOCIAL_SPY = Preferences.SOCIAL_SPY;

    public SocialSpyCommand() {
        super("socialspy", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_CHAT));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Boolean set = !src.getPreference(SOCIAL_SPY);
        src.getPreferences().set(SOCIAL_SPY, set);

        if (set) {
            src.sendLangMessage("command.socialspy.active");
        } else {
            src.sendLangMessage("command.socialspy.inactive");
        }

        return set ? SUCCESS : AWAIT;
    }
}
