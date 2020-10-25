package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;

import static com.mojang.brigadier.arguments.StringArgumentType.*;


public class UsageCommand extends EssentialCommand {
    public UsageCommand() {
        super("usage");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = getCommandArgument();
        this.commandNode.addChild(stringArgument.build());
    }

    static RequiredArgumentBuilder<ServerCommandSource, String> getCommandArgument() {
        return CommandManager.argument("command", greedyString())
                .suggests(ArgumentSuggestions::usableCommands)
                .executes(UsageCommand::execute);
    }

    static int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final String command = getString(ctx, "command");
        final String fromLang = ModConstants.getStrings().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", ctx.getSource());
        else
            KiloCommands.executeSmartUsageFor(command, ctx.getSource());

        return SUCCESS;
    }

}
