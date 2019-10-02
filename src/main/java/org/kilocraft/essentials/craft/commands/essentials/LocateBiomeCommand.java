package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.registry.Registry;
import org.kilocraft.essentials.craft.ThreadManager;
import org.kilocraft.essentials.craft.provider.ThreadedBiomeLocator;

public class LocateBiomeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("ke_locate")
                .requires(s -> Thimble.hasPermissionChildOrOp(s, "kiloessentials.command.locate.biome", 2))
                .then(CommandManager.literal("biome")
                        .then(CommandManager.argument("biome", StringArgumentType.string())
                            .suggests(suggestionProvider))
                            .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "biome")))
                );


        dispatcher.register(builder);
    }

    private static int execute(ServerCommandSource source, String biome) {

        Registry.BIOME.stream().forEach((biome1) -> {
            if (biome1.getName().equals(biome)) {
                ThreadManager thread = new ThreadManager(new ThreadedBiomeLocator(source, biome1));
                thread.start();
            }
        });

        return 0;
    }

    private static SuggestionProvider<ServerCommandSource> suggestionProvider = ((context, builder) -> {
        Registry.BIOME.forEach((biome) -> {
            builder.suggest(biome.getName().asFormattedString());
        });

        return builder.buildFuture();
    });
}
