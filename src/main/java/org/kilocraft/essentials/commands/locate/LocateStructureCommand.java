package org.kilocraft.essentials.commands.locate;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.world.Structures;
import org.kilocraft.essentials.ThreadManager;
import org.kilocraft.essentials.provided.LocateStructureProvided;
import org.kilocraft.essentials.threaded.ThreadedStructureLocator;

import static net.minecraft.server.command.CommandManager.literal;

public class LocateStructureCommand {
    public static void registerAsChild(LiteralArgumentBuilder<ServerCommandSource> builder) {

        LiteralArgumentBuilder<ServerCommandSource> literalStructure = literal("structure")
                .requires(s -> Thimble.hasPermissionOrOp(s, "kiloessentials.command.locate.structure", 2));

        Structures.list.forEach((structure) -> {
            literalStructure.then(literal(structure.toLowerCase())
                    .executes(c -> execute(c.getSource(), structure))
            );
        });

        builder.then(literalStructure);
    }

    private static int execute(ServerCommandSource source, String structure) {
        source.sendFeedback(LangText.getFormatter(true, "command.locate.scanning", LocateStructureProvided.getStructureName(structure)), false);
        ThreadManager thread = new ThreadManager(new ThreadedStructureLocator(source, structure));
        thread.start();

        return 0;
    }

}
