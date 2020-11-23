package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.text.Texter;

public class BuilderCommand extends EssentialCommand {
    public BuilderCommand() {
        super("builder", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.BUILDER));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        Texter.ListStyle text = Texter.ListStyle.of(
                "Builders", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(EssentialPermission.BUILDER)) {
                text.append(Texter.Legacy.toFormattedString(user.getRankedDisplayName()));
            }
        }

        this.getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }
}
