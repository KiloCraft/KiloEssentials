package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

public class ReloadCommand extends EssentialCommand {
    public ReloadCommand() {
        super("reload", CommandPermission.RELOAD, new String[]{"rl"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.reload.start");

        server.reload();
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.reload.end");
        return SINGLE_SUCCESS;
    }
}
