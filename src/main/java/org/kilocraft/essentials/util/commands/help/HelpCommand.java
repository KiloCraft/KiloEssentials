package org.kilocraft.essentials.util.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.user.CommandSourceServerUser;

public class HelpCommand extends EssentialCommand {
    public HelpCommand() {
        super("help", new String[]{"?"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> ctx) {
        new CommandSourceServerUser(ctx.getSource()).sendMessage(ComponentText.toComponent(KiloConfig.messages().commands().helpMessage));
        return SUCCESS;
    }

}
