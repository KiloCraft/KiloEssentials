package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.provided.LocateBiomeProvided;

import static net.minecraft.server.command.CommandManager.literal;

public class LocateBiomeCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalBiome = literal("biome")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE_BIOME));

        Registry.BIOME.stream().forEach((biome) -> {
            literalBiome.then(literal(LocateBiomeProvided.getBiomeId(biome))
                .executes(c -> execute(c.getSource(), biome)));
        });

        builder.then(literalBiome);
    }

    private static int execute(ServerCommandSource source, Biome biome) {


        return 1;
    }

}
