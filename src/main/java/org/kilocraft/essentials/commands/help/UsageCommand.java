package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;

import static com.mojang.brigadier.arguments.StringArgumentType.*;


public class UsageCommand extends EssentialCommand {
    public UsageCommand() {
        super("usage");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = this.argument("command", greedyString())
                .suggests(ArgumentCompletions::usableCommands)
                .executes(this::execute);

        this.commandNode.addChild(stringArgument.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String command = getString(ctx, "command");
        final String fromLang = ModConstants.getLang().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", ctx.getSource());
        else
            KiloCommands.executeSmartUsageFor(command, ctx.getSource());

        return SUCCESS;
    }

}
