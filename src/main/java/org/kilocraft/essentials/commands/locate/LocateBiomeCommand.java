package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.ThreadManager;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.threaded.ThreadedBiomeLocator;

import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;
import java.util.Optional;

public class LocateBiomeCommand {
    public static final DynamicCommandExceptionType BIOME_NOT_FOUND = new DynamicCommandExceptionType(LocateBiomeCommand::getMessage);

    private static Text getMessage(Object o) {
        Identifier id = (Identifier) o;
        return new LiteralText("Could not find biome of name: " + id.toString()).formatted(Formatting.RED);
    }

    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {
        LiteralArgumentBuilder<ServerCommandSource> literalBiome = literal("biome")
                .requires(s -> KiloCommands.hasPermission(s, CommandPermission.LOCATE_BIOME));

        literalBiome.then(argument("biome", identifier())
            .suggests(TabCompletions::biomes))
            .executes(LocateBiomeCommand::execute);

        builder.then(literalBiome);
    }

    private static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        Identifier id = getIdentifier(context, "biome");
        Optional<Biome> biome = Registry.BIOME.getOrEmpty(id);

        ThreadManager thread = new ThreadManager(new ThreadedBiomeLocator(context.getSource(), biome.orElseThrow(() -> BIOME_NOT_FOUND.create(id))));
        thread.start();

        return 1;
    }

}
