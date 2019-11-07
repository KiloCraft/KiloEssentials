package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.github.indicode.fabric.permissions.Thimble;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.chat.TextFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ModsCommand { // TODO Why is this in api package?
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloAPICommands.getCommandPermission("mods");
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = literal("mods")
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloAPICommands.getCommandPermission("tps"), 2))
                .executes(ModsCommand::executeMultiple);

        RequiredArgumentBuilder<ServerCommandSource, String> modIdArgument = argument("id", word()) // modids cannot contain spaces so a word arg is fine
                .executes(c -> executeSingle(c, getString(c, "id")));

        modIdArgument.suggests(suggestModIDs);
        literalArgumentBuilder.then(modIdArgument);
        dispatcher.register(literalArgumentBuilder);
    }

    private static int executeMultiple(CommandContext<ServerCommandSource> context) {
        ArrayList<String> mods = new ArrayList<>();
        int i = FabricLoader.getInstance().getAllMods().size();
        FabricLoader.getInstance().getAllMods().forEach(modContainer -> mods.add(modContainer.getMetadata().getName()));

        LiteralText text = new LiteralText("&6Mods (" + i + " loaded):&f " + mods.toString().replace("[","").replace("]", "")); // TODO Magic values
        TextFormat.sendToUniversalSource(context.getSource(), text, false);
        return 1;
    }

    private static int executeSingle(CommandContext<ServerCommandSource> context, String modId) {
        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(modId);

        if (!modContainer.isPresent()) {
            modContainer = FabricLoader.getInstance().getAllMods().stream().filter(c -> c.getMetadata().getName().equals(modId)).findFirst();
            if (!modContainer.isPresent()) {
                context.getSource().sendError(new LiteralText("Can't find the mod with a name of \"" + modId + "\"").formatted(Formatting.RED)); // TODO Magic values
            }
        }

        if (modContainer.isPresent()) {
            ModMetadata meta = modContainer.get().getMetadata();

            List<String> authors = new ArrayList<>();
            meta.getAuthors().forEach(author ->
                authors.add(author.getName())
            );

            LiteralText text;
            // TODO Magic values -- START
            if (!modContainer.get().getMetadata().getId().equals("minecraft")) {
               text = new LiteralText(String.format(
                        "&6Mod meta:\n&b -info:&a %s &7(%s@%s)\n &b-Description:&f %s\n &b-Authors:&e %s",
                        meta.getName(), meta.getId(), meta.getVersion().toString(),
                        meta.getDescription(),
                        authors.toString().replace("[","").replace("]", "")
                ));

            } else {
                text = new LiteralText(String.format(
                        "&6Mod meta:\n&b -info:&a %s &7(%s@%s)\n &b-Description:&f %s\n &b-Authors:&e %s",
                        meta.getName(), meta.getId(), meta.getVersion().toString(),
                        "Minecraft source code deobfuscation mappings by the fabric-yarn",
                        "fabric"
                ));
            }
            // TODO Magic values -- END

            TextFormat.sendToUniversalSource(context.getSource(), text, false);
        }

        return 1;
    }


    private static SuggestionProvider<ServerCommandSource> suggestModIDs = ((context, builder) -> CommandSource.suggestMatching(FabricLoader.getInstance().getAllMods().stream().map((mod) -> mod.getMetadata().getId()), builder));

}
