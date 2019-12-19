package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.ThreadManager;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.threaded.ThreadedBiomeLocator;

import static net.minecraft.server.command.CommandManager.literal;

public class LocateBiomeCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalBiome = literal("biome")
                .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.locate.biome", 2));

        Registry.BIOME.stream().forEach((biome) -> {
            literalBiome.then(literal(LocateBiomeProvided.getBiomeId(biome))
                .executes(c -> execute(c.getSource(), biome)));
        });

        builder.then(literalBiome);
    }

    private static int execute(ServerCommandSource source, Biome biome) {
        ThreadManager thread = new ThreadManager(new ThreadedBiomeLocator(source, biome));
        thread.start();

        return 1;
    }

}
