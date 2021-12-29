package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.commands.CommandUtils;

import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.Entity;

import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class SudoCommand extends EssentialCommand {

    public SudoCommand() {
        super("sudo", source -> source.hasPermission(2));
        this.withUsage("command.sudo.usage", "target", "command");
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        ArgumentCommandNode<CommandSourceStack, EntitySelector> selectorArg = this.argument("target", player())
                .suggests(ArgumentSuggestions::allPlayers)
                .redirect(dispatcher.getRoot(), redirectModifier())
                .build();


        LiteralCommandNode<CommandSourceStack> asArg = this.literal("as")
                .then(this.argument("targets", EntityArgument.entities())
                        .then(this.argument("commandWithFormatting", StringArgumentType.greedyString())
                                .executes(SudoCommand::executeAs)))
                .build();

        this.commandNode.addChild(asArg);
        this.commandNode.addChild(selectorArg);
    }

    private static SingleRedirectModifier<CommandSourceStack> redirectModifier() {
        return context -> getPlayer(context, "target").createCommandSourceStack();
    }

    private static int executeAs(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String command = StringArgumentType.getString(ctx, "commandWithFormatting");
        Collection<? extends Entity> collection = EntityArgument.getEntities(ctx, "targets");
        for (Entity entity : collection) {
            CommandUtils.runCommandWithFormatting(entity.createCommandSourceStack(), command);
        }

        return collection.size();
    }

}
