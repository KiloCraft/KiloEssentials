package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.IdentifierArgumentType;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.provided.LocateStructureProvided;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LocateStructureCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalStructure = literal("structure")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE_STRUCTURE));

        RequiredArgumentBuilder<ServerCommandSource, Identifier> structureArg = argument("identifier", identifier())
                .suggests(LocateStructureCommand::structureNames)
                .executes(c -> execute(c.getSource(), IdentifierArgumentType.getIdentifier(c, "identifier")));

        literalStructure.then(structureArg);
        builder.then(literalStructure);
    }

    private static CompletableFuture<Suggestions> structureNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (StructureFeature<?> structureFeature : Registry.STRUCTURE_FEATURE) strings.add(structureFeature.getName().toLowerCase());
        return CommandSource.suggestMatching(strings, builder);
    }

    private static int execute(ServerCommandSource source, Identifier identifier) throws CommandSyntaxException {
        StructureFeature<?> structure = Registry.STRUCTURE_FEATURE.get(identifier);

        if (structure == null)
            throw KiloCommands.getException(ExceptionMessageNode.INCORRECT_IDENTIFIER, "structure").create();

        StructureLocatorThread locatorThread = new StructureLocatorThread(source, structure.getName());
        Thread thread = new Thread(locatorThread, "Structure locator thread");
        thread.start();

        return 1;
    }

    static class StructureLocatorThread implements Runnable {
        private Logger logger = LogManager.getLogger();
        private ServerCommandSource source;
        private String structure;

        public StructureLocatorThread(ServerCommandSource source, String structure) {
            this.source = source;
            this.structure = structure;
        }

        @Override
        public void run() {
            logger.info("Locating structure \"" + structure + "\", executed by " + source.getName());
            LocateStructureProvided.execute(this.source, this.structure);
        }
    }
}