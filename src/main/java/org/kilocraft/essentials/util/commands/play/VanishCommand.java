package org.kilocraft.essentials.util.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.ConfigVariableFactory;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.List;

public class VanishCommand extends EssentialCommand {

    private final Preference<Boolean> preference = Preferences.VANISH;

    public VanishCommand() {
        super("vanish", CommandPermission.VANISH);
        this.withUsage("command.vanish.usage");
    }

    @Override
    public final void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::toggle);
    }

    private int toggle(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineServerUser user = (OnlineServerUser) KiloEssentials.getUserManager().getOnline(ctx.getSource());

        user.getPreferences().set(this.preference, !user.getPreference(this.preference));


        List<OnlineUser> online = KiloEssentials.getUserManager().getOnlineUsersAsList();
        if (user.getPreference(this.preference)) {
            for (OnlineUser onlineUser : online) {
                if (!onlineUser.hasPermission(CommandPermission.VANISH)) {
                    onlineUser.asPlayer().networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.REMOVE_PLAYER, user.asPlayer()));
                }
            }
            KiloChat.broadCast(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.left"), user));
            user.sendLangMessage("command.vanish.on");
        } else {
            for (OnlineUser onlineUser : online) {
                if (!onlineUser.hasPermission(CommandPermission.VANISH)) {
                    onlineUser.asPlayer().networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, user.asPlayer()));
                }
            }
            KiloChat.broadCast(ConfigVariableFactory.replaceUserVariables(ModConstants.translation("player.joined"), user));
            user.sendLangMessage("command.vanish.off");
        }

        return SUCCESS;
    }
}
