package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ModsCommand extends EssentialCommand {
    public ModsCommand() {
        super("mods", new String[]{"mod", "fabric"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> modArgument = argument("modid", word())
                .suggests(this::suggestMods)
                .executes(this::sendInfo);

        commandNode.addChild(modArgument.build());
        argumentBuilder.executes(this::sendList);
    }

    private int sendList(CommandContext<ServerCommandSource> ctx) {
        int allMods = FabricLoader.getInstance().getAllMods().size();
        Text text = new LiteralText("Mods").formatted(Formatting.GOLD)
                .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(String.valueOf(allMods)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();
            Text thisMod = new LiteralText("");

            i++;
            Formatting thisFormat = nextColor ? Formatting.WHITE : Formatting.GRAY;

            thisMod.append(new LiteralText(meta.getName()).styled((style) -> {
                style.setColor(thisFormat);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("[i] ").formatted(Formatting.YELLOW)
                                .append(new LiteralText("Click for more info!").formatted(Formatting.GREEN))));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mods " + meta.getId()));
            }));

            if (allMods != i)
                thisMod.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor = !nextColor;
            text.append(thisMod);
        }

        KiloChat.sendMessageTo(ctx.getSource(), text);
        return SINGLE_SUCCESS;
    }

    private int sendInfo(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String inputId = getString(ctx, "modid");

        if (!FabricLoader.getInstance().getModContainer(inputId).isPresent())
            throw MOD_NOT_PRESENT.create();

        ModMetadata meta = FabricLoader.getInstance().getModContainer(inputId).get().getMetadata();
        Text text = new LiteralText("").append(new LiteralText(meta.getName()).formatted(Formatting.YELLOW)).append("\n")
                .append(new LiteralText(meta.getId()).append("@").append(String.valueOf(meta.getVersion())).formatted(Formatting.GRAY))
                .append("\n").formatted(Formatting.RESET)
                .append(new LiteralText("Author(s): ").append(getModAuthorList(meta)).formatted(Formatting.RESET));

        if (!meta.getDescription().isEmpty())
            text.append("\n").append(new LiteralText("Description: ").append(new LiteralText(meta.getDescription())));

        KiloChat.sendMessageTo(ctx.getSource(), text);
        return SINGLE_SUCCESS;
    }

    private Text getModAuthorList(ModMetadata meta) {
        Text text = new LiteralText("");
        AtomicInteger i = new AtomicInteger();
        AtomicBoolean nextColor = new AtomicBoolean(false);
        int authors = meta.getAuthors().size();

        meta.getAuthors().forEach((person) -> {
            Text thisPerson = new LiteralText("");
            i.getAndIncrement();
            Formatting thisFormat = nextColor.get() ? Formatting.WHITE : Formatting.GRAY;

            thisPerson.append(new LiteralText(person.getName()).formatted(thisFormat));
            if (authors != i.get())
                thisPerson.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor.set(!nextColor.get());
            text.append(thisPerson);
        });

        return text;
    }

    private CompletableFuture<Suggestions> suggestMods(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.getMetadata().getId()), builder);
    }

    private SimpleCommandExceptionType MOD_NOT_PRESENT = new SimpleCommandExceptionType(new LiteralText("Can't find a mod with that name/id!"));
}
