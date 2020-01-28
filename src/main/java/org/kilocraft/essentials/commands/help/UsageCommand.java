package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;


public class UsageCommand extends EssentialCommand {
    public UsageCommand() {
        super("usage");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = argument("command", greedyString())
                .suggests(TabCompletions::usableCommands)
                .executes(this::execute);

        commandNode.addChild(stringArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String command = getString(ctx, "command");
        String fromLang = ModConstants.getLang().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", ctx.getSource());
        else
            KiloCommands.executeSmartUsageFor(command, ctx.getSource());

        return KiloCommands.SUCCESS();
    }

}
