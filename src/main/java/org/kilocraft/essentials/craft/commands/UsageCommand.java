package org.kilocraft.essentials.craft.commands;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class UsageCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("usage")
                .then(
                        CommandManager.argument("command", StringArgumentType.greedyString())
                            .suggests((context, builder) -> suggestionProvider.getSuggestions(context, builder))
                            .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "command")))
                );

        dispatcher.register(argumentBuilder);
    }

    private static int execute(ServerCommandSource source, String command) throws CommandSyntaxException {
        String fromLang = Mod.getLang().getProperty("command." + command + ".usage");

        if (fromLang != null)
            KiloCommands.executeUsageFor("command." + command + ".usage", source);
        else
            KiloCommands.executeSmartUsageFor(command, source);

        return 1;
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        int amount = 0;
        ArrayList<String> stringArrayList = new ArrayList<>();
        Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> commandNodeMap = Maps.newHashMap();
        RootCommandNode<CommandSource> rootCommandNode = new RootCommandNode();
        commandNodeMap.put(KiloCommands.getDispatcher().getRoot(), rootCommandNode);

        Iterator iterator = rootCommandNode.getChildren().iterator();

        while (iterator.hasNext()) {
            CommandNode<ServerCommandSource> node = (CommandNode<ServerCommandSource>) iterator.next();

            if (node.canUse(context.getSource())) {
                amount++;
                stringArrayList.add(node.getName());
            }
        }

        String[] strings = {};
        for (int i = 0; i < amount; i++) {
            strings[i] = stringArrayList.get(i);
        }

        return CommandSource.suggestMatching(strings, builder);
    });
}
