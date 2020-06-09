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

public class SocialSpyCommand extends EssentialCommand {
    private static final Setting<Boolean> SOCIAL_SPY = Settings.SOCIAL_SPY;

    public SocialSpyCommand() {
        super("socialspy", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.SPY_CHAT));
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }
    
    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        Boolean set = !src.getSetting(SOCIAL_SPY);
        src.getSettings().set(SOCIAL_SPY, set);

        if (set) {
            src.sendLangMessage("command.socialspy.active");
        } else {
            src.sendLangMessage("command.socialspy.inactive");
        }

        return set ? SUCCESS : AWAIT;
    }
}
