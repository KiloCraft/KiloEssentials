package org.kilocraft.essentials.util.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LocateBiomeCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalBiome = literal("biome")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE_BIOME));

        RequiredArgumentBuilder<ServerCommandSource, Identifier> biomeArg = argument("identifier", identifier())
                .suggests(LocateBiomeCommand::biomeNames)
                .executes(LocateBiomeCommand::execute);

        literalBiome.then(biomeArg);
        builder.then(literalBiome);
    }

    private static CompletableFuture<Suggestions> biomeNames(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (Biome biome : KiloEssentials.getMinecraftServer().getRegistryManager().get(Registry.BIOME_KEY)) {
            strings.add(LocateBiomeProvided.getBiomeId(biome));
        }
        return CommandSource.suggestMatching(strings, builder);
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Biome biome = KiloEssentials.getMinecraftServer().getRegistryManager().get(Registry.BIOME_KEY).get(IdentifierArgumentType.getIdentifier(ctx, "identifier"));

        if (biome == null)
            throw KiloCommands.getException("exception.incorrect_identifier", "biome").create();

        LocateBiomeProvided locator = new LocateBiomeProvided(biome);
        locator.run(ctx);

        return 1;
    }

}


