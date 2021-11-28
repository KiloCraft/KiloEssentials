package org.kilocraft.essentials.util.commands.misc;

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
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.text.Texter;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ModsCommand extends EssentialCommand {
    public ModsCommand() {
        super("mods", new String[]{"fabric"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> modArgument = this.argument("modid", word())
                .suggests(this::suggestMods)
                .executes(this::sendInfo);

        this.commandNode.addChild(modArgument.build());
        this.argumentBuilder.executes(this::sendList);
    }

    private int sendList(CommandContext<CommandSourceStack> ctx) {
        Texter.ListStyle text = Texter.ListStyle.of("Mods", ChatFormatting.GOLD, ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY);

        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata meta = mod.getMetadata();

            text.append(
                    Texter.Events.onHover(ModConstants.translation("general.click_info")),
                    Texter.Events.onClickRun("/mods " + meta.getId()),
                    meta.getName()
            );
        }
        this.getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }

    private int sendInfo(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        String inputId = getString(ctx, "modid");

        if (!FabricLoader.getInstance().getModContainer(inputId).isPresent()) {
            throw this.MOD_NOT_PRESENT.create();
        }

        ModMetadata meta = FabricLoader.getInstance().getModContainer(inputId).get().getMetadata();

        Texter.InfoBlockStyle text = Texter.InfoBlockStyle.of(meta.getName());
        text.append("Version", meta.getVersion().getFriendlyString());
        text.append("Authors", this.authorsToArrayText(meta));
        text.append("Description", meta.getDescription());

        this.getCommandSource(ctx).sendMessage(text.build());
        return SUCCESS;
    }

    private MutableComponent authorsToArrayText(ModMetadata meta) {
        Texter.ArrayStyle text = new Texter.ArrayStyle();
        for (Person author : meta.getAuthors()) {
            MutableComponent mutable = Texter.newText(author.getName());
            mutable.withStyle((style) -> {
                style.withHoverEvent(Texter.Events.onHover(ModConstants.translation("general.click_info")));
                style.withClickEvent(Texter.Events.onClickRun("mods", meta.getId(), author.getName()));
                return style;
            });
            text.append(mutable);
        }

        return text.build();
    }


    private CompletableFuture<Suggestions> suggestMods(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FabricLoader.getInstance().getAllMods().stream()
                .map(mod -> mod.getMetadata().getId()), builder);
    }

    private final SimpleCommandExceptionType MOD_NOT_PRESENT = new SimpleCommandExceptionType(new TextComponent("Can't find a mod with that name/id!"));
}
