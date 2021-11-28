package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.text.Texter;

public class BuilderCommand extends EssentialCommand {
    public BuilderCommand() {
        super("builder", src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.BUILDER));
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) {
        Texter.ListStyle text = Texter.ListStyle.of(
                "Builders", ChatFormatting.GOLD, ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY
        );

        for (OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
            if (user.hasPermission(EssentialPermission.BUILDER)) {
                text.append(Texter.Legacy.toFormattedString(user.getRankedDisplayName()));
            }
        }

        this.getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }
}
