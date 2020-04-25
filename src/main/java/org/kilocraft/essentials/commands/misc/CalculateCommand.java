package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.concurrent.CompletableFuture;

public class CalculateCommand extends EssentialCommand {
    public CalculateCommand() {
        super("calculate", CommandPermission.CALCULATE, new String[]{"calc"});
        this.withUsage("command.calculate.usage", "input");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> inputArgument = argument("input", StringArgumentType.greedyString())
                .suggests(this::operations)
                .executes(this::execute);

        this.commandNode.addChild(inputArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        CommandSourceUser src = this.getServerUser(ctx);
        String input = StringArgumentType.getString(ctx, "input");
        StringUtils.Calculator calculator = new StringUtils.Calculator(input);
        int result;

        if (!input.contains("+") && !input.contains("-") && !input.contains("*") && !input.contains("/") && !input.contains("^") && !input.contains("%")) {
            src.sendLangMessage("command.calculate.nooperator");
            return FAILED;
        }

        try {
            calculator.calculate();
            result = (int) calculator.result();
        } catch (Exception e) {
            src.sendError(Texter.exceptionToText(e, true));
            return FAILED;
        }

        src.sendLangMessage("command.calculate.info");
        src.sendLangMessage("command.calculate.result", calculator.getInput(), calculator.resultAsShortString());
        return result;
    }

    private CompletableFuture<Suggestions> operations(final CommandContext<ServerCommandSource> ctx, final SuggestionsBuilder builder) {
        return ArgumentCompletions.suggestAtCursor(StringUtils.Calculator.operations(), ctx);
    }
}
