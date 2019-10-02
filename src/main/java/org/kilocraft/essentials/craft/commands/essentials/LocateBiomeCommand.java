package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.craft.ThreadManager;
import org.kilocraft.essentials.craft.provider.ThreadedBiomeLocator;

public class LocateBiomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ke_locate")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.locate", 2));

        LiteralArgumentBuilder<ServerCommandSource> literalBiome = CommandManager.literal("biome")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.locate.biome", 2));

        Registry.BIOME.forEach((biome) -> {
            literalBiome.then(CommandManager.literal(biome.getName().asFormattedString())
                .executes(c -> execute(c.getSource(), biome)));
        });

        builder.then(literalBiome);
        dispatcher.register(builder);
    }

    private static int execute(ServerCommandSource source, Biome biome) {
        ThreadManager thread = new ThreadManager(new ThreadedBiomeLocator(source, biome));
        thread.start();

        return 0;
    }

}
