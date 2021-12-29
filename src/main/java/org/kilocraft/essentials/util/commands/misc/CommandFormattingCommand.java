package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.text.Texter;

public class CommandFormattingCommand extends EssentialCommand {

    public CommandFormattingCommand() {
        super("commandformatting", CommandPermission.SUDO_OTHERS);
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<CommandSourceStack> ctx) {
        this.getCommandSource(ctx).sendMessage(Texter.InfoBlockStyle.of(ModConstants.translation("command.commandformatting.title"))
                .newLine().append(ModConstants.translation("command.commandformatting.info")).newLine()
                .build());
        return SUCCESS;
    }
}
