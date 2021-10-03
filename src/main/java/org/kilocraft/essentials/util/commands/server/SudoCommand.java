package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.SingleRedirectModifier;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.commands.CommandUtils;

import java.util.Collection;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;

public class SudoCommand extends EssentialCommand {

    public SudoCommand() {
        super("sudo", source -> source.hasPermissionLevel(2));
        this.withUsage("command.sudo.usage", "target", "command");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ArgumentCommandNode<ServerCommandSource, EntitySelector> selectorArg = this.argument("target", player())
                .suggests(ArgumentSuggestions::allPlayers)
                .redirect(dispatcher.getRoot(), redirectModifier())
                .build();


        LiteralCommandNode<ServerCommandSource> asArg = this.literal("as")
                .then(this.argument("targets", EntityArgumentType.entities())
                        .then(this.argument("commandWithFormatting", StringArgumentType.greedyString())
                                .executes(SudoCommand::executeAs)))
                .build();

        this.commandNode.addChild(asArg);
        this.commandNode.addChild(selectorArg);
    }

    private static SingleRedirectModifier<ServerCommandSource> redirectModifier() {
        return context -> getPlayer(context, "target").getCommandSource();
    }

    private static int executeAs(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String command = StringArgumentType.getString(ctx, "commandWithFormatting");
        Collection<? extends Entity> collection = EntityArgumentType.getEntities(ctx, "targets");
        for (Entity entity : collection) {
            CommandUtils.runCommandWithFormatting(entity.getCommandSource(), command);
        }

        return collection.size();
    }

}
