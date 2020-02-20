package org.kilocraft.essentials.extensions.magicalparticles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.TextUtils;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager.*;

public class MagicalParticlesCommand extends EssentialCommand {
    public MagicalParticlesCommand(){
        super("magicalparticles",
                src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.MAGIC_PARTICLES_SELF), new String[]{"mp", "particleanimation", "pa"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> setArg = literal("set");
        LiteralArgumentBuilder<ServerCommandSource> listArg = literal("list")
                .executes(this::list);
        LiteralArgumentBuilder<ServerCommandSource> disableArg = literal("disable")
                .executes(this::disable);

        RequiredArgumentBuilder<ServerCommandSource, Identifier> idArgument = argument("animation", identifier())
                .suggests(this::particleIdSuggestions)
                .executes(this::set);

        setArg.then(idArgument);
        commandNode.addChild(listArg.build());
        commandNode.addChild(setArg.build());
        commandNode.addChild(disableArg.build());
        argumentBuilder.executes(this::list);
    }

    private int set(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Identifier identifier = validateAndGetId(ctx);

        addPlayer(player.getUuid(), identifier);
        KiloChat.sendLangMessageTo(player, "command.magicalparticles.set", getAnimationName(identifier));
        return SINGLE_SUCCESS;
    }

    private int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        TextUtils.ListStyle text = TextUtils.ListStyle.of(
                "Particle Animations", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        text.append("&cdisable",
                TextUtils.Events.onHover(new LiteralText("Click Here to Disable").formatted(Formatting.GOLD)
                ),
                TextUtils.Events.onClick("/mp disable")
        );

        map.forEach((id, animation) -> text.append(id.getPath(),
                TextUtils.Events.onHover(new LiteralText("")
                        .append(new LiteralText(animation.getName()).formatted(Formatting.GOLD))
                        .append("\n")
                        .append(new LiteralText(animation.getId().toString()).formatted(Formatting.DARK_GRAY))
                        .append("\n")
                        .append(new LiteralText(tl("general.click_apply")).formatted(Formatting.YELLOW))
                ),
                TextUtils.Events.onClick("/mp set " + id.toString())
        ));

        KiloChat.sendMessageTo(player, text.build());
        return SINGLE_SUCCESS;
    }

    private int disable(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        KiloChat.sendLangMessageTo(player, "command.magicalparticles.disabled");
        removePlayer(player.getUuid());
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        map.forEach((id, animation) -> strings.add(id.getPath()));
        return CommandSource.suggestMatching(strings, builder);
    }

    private Identifier validateAndGetId(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Identifier identifier = getIdentifier(ctx, "animation");
        if (!isValidId(identifier))
            identifier = getIdFromPath(identifier.getPath());

        if (!isValidId(identifier))
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Particle animation").create();

        return identifier;
    }

}
