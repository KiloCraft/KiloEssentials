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
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LocateBiomeCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalBiome = literal("biome")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE_BIOME));

        RequiredArgumentBuilder<ServerCommandSource, Identifier> biomeArg = argument("identifier", identifier())
                .suggests(LocateBiomeCommand::biomeNames)
                .executes(c -> execute(c.getSource(), IdentifierArgumentType.getIdentifier(c, "identifier")));

        literalBiome.then(biomeArg);
        builder.then(literalBiome);
    }

    private static CompletableFuture<Suggestions> biomeNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (Biome biome : Registry.BIOME) strings.add(LocateBiomeProvided.getBiomeId(biome));
        return CommandSource.suggestMatching(strings, builder);
    }

    private static int execute(ServerCommandSource source, Identifier identifier) throws CommandSyntaxException {
        Biome biome = Registry.BIOME.get(identifier);

        if (biome == null)
            throw KiloCommands.getException(ExceptionMessageNode.INCORRECT_IDENTIFIER, "biome").create();

        KiloChat.sendLangMessageTo(source, "command.locate.scanning", LocateBiomeProvided.getBiomeName(biome));
        BiomeLocatorThread locatorThread = new BiomeLocatorThread(source, biome);
        Thread thread = new Thread(locatorThread, "Biome locator thread");
        thread.start();

        return 1;
    }

}

class BiomeLocatorThread implements Runnable {
    private Logger logger = LogManager.getLogger();
    private ServerCommandSource source;
    private Biome biome;

    public BiomeLocatorThread(ServerCommandSource source, Biome biome) {
        this.source = source;
        this.biome = biome;
    }

    @Override
    public void run() {
        logger.info("Locating biome \"" + LocateBiomeProvided.getBiomeId(biome) + "\", executed by " + source.getName());
        LocateBiomeProvided.execute(this.source, this.biome);
    }
}
