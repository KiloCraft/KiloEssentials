package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class HomesCommand extends EssentialCommand {
    public HomesCommand() {
        super("homes", CommandPermission.HOMES_SELF);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {

    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) {

        return SINGLE_SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) {

        return SINGLE_SUCCESS;
    }
}
