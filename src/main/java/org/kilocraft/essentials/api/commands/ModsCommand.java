package org.kilocraft.essentials.api.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloPerms;
import org.kilocraft.essentials.api.Mod;

import java.util.Optional;

public class ModsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("mods")
                .requires(source -> KiloPerms.testFor(source, "kiloapi.command.mods"))
                .executes(ModsCommand::getMods);

        RequiredArgumentBuilder<ServerCommandSource, String> modIdArgument = CommandManager.argument("modId", StringArgumentType.string())
                .executes(context -> getMod(context, StringArgumentType.getString(context, "modId")));

        literalArgumentBuilder.then(modIdArgument);
        buildSuggestions(literalArgumentBuilder);
        dispatcher.register(literalArgumentBuilder);
    }

    public static int getMods(CommandContext<ServerCommandSource> context) {
        Text text = new LiteralText("");
        text.append(buildMinecraftMod());

        for (ModContainer modContainer: FabricLoader.getInstance().getAllMods()) {
            buildModName(modContainer, text);
        }

        context.getSource().sendFeedback(text, false);
        return 1;
    }

    private static void buildSuggestions(LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder) {
        FabricLoader.getInstance().getAllMods().forEach(modContainer ->
            addToArguments(literalArgumentBuilder, modContainer.getMetadata().getId())
        );
    }

    private static void addToArguments(LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder, String modId) {
        literalArgumentBuilder.then(CommandManager.literal(modId))
                .executes(context -> getMod(context, modId));
    }

    public static int getMod(CommandContext<ServerCommandSource> context, String modId) {
        Optional<ModContainer> mod = FabricLoader.getInstance().getModContainer(modId);
        LiteralText literalText = new LiteralText("");

        if (!mod.get().getMetadata().getId().equals("fabric-yarn-mappings")) {
            if (!mod.isPresent()) {
                mod = FabricLoader.getInstance().getAllMods().stream().filter(c -> c.getMetadata().getName().equals(modId)).findFirst();

                if (!mod.isPresent()) {
                    context.getSource().sendFeedback(
                            new LiteralText("Can't find the mod with a id of \"" + modId + "\"").formatted(Formatting.RED),
                            false);
                }
            }

            literalText = buildSignleModMeta(mod.get().getMetadata());

        } else {
            literalText.append(buildMinecraftMod());
        }

        ModMetadata modMetadata = mod.get().getMetadata();

        context.getSource().sendFeedback(literalText, false);

        return 1;
    }

    private static LiteralText buildSignleModMeta(ModMetadata modMetadata) {
        LiteralText literalText = new LiteralText("");
        literalText.append("Meta: ").formatted(Formatting.GOLD);
        literalText.append(modMetadata.getName()).formatted(Formatting.GREEN);
        literalText.append(" (" + modMetadata.getVersion() + ")").formatted(Formatting.GRAY);
        literalText.append(" ->\n").formatted(Formatting.AQUA);
        literalText.append("Description: ").formatted(Formatting.GOLD);
        literalText.append(modMetadata.getDescription().substring(0, 150));
        literalText.append("Authors: ").formatted(Formatting.GOLD);
        literalText.append(modMetadata.getAuthors().toString());

        return literalText;
    }

    private static Text buildMinecraftMod() {
        LiteralText literalText = new LiteralText("");
        LiteralText hoverText = new LiteralText("");
        hoverText.append("fabric-yarn-mappings").formatted(Formatting.GOLD);
        hoverText.append(" (" + Mod.getMappingsVersion() + ")").formatted(Formatting.GRAY);
        hoverText.append("\nDescription: \n").formatted(Formatting.WHITE);
        hoverText.append("Minecraft source code deObfuscation by Fabric-yarn project" + "\n").formatted(Formatting.WHITE);
        hoverText.append("\nauthors: Mojang. mappings by fabric").formatted(Formatting.GRAY);
        hoverText.append("\ntype: universal").formatted(Formatting.GRAY);

        Text text1 = Texts.bracketed(new LiteralText("Minecraft " + MinecraftVersion.create().getName())).styled(style ->
                style.setColor(Formatting.GOLD)
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
        );

        return literalText.append(text1);
    }

    private static void buildModName(ModContainer mod, Text text) {
        LiteralText hoverText = new LiteralText("");
        hoverText.append(mod.getMetadata().getId()).formatted(Formatting.GOLD);
        hoverText.append(" (" + mod.getMetadata().getVersion() + ")").formatted(Formatting.GRAY);
        hoverText.append("\nDescription: \n").formatted(Formatting.WHITE);
        hoverText.append(mod.getMetadata().getDescription().substring(0, 65) + "\n").formatted(Formatting.WHITE);
        hoverText.append(mod.getMetadata().getDescription().substring(65, 140)  + "...").formatted(Formatting.WHITE);
        hoverText.append("\nauthors: " + mod.getMetadata().getAuthors()).formatted(Formatting.GRAY);
        hoverText.append("\ntype: " + mod.getMetadata().getType()).formatted(Formatting.GRAY);

        Text text1 = Texts.bracketed(new LiteralText(mod.getMetadata().getName())).styled(style ->
                style.setColor(Formatting.GREEN)
                   .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
        );

        text.append(", ").formatted(Formatting.DARK_GRAY);
        text.append(text1);
    }
}
