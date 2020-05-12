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
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.ParticleAnimation;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.text.Texter;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        boolean silent = false;
        Identifier identifier = getIdentifier(ctx, "animation");

        if (identifier.getPath().endsWith("--s")) {
            silent = true;
            identifier = new Identifier(identifier.toString().replaceFirst("--s", ""));
        }

        if (!isValidId(identifier)) {
            identifier = getIdFromPath(identifier.getPath());
        }

        if (!isValidId(identifier)) {
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Particle animation").create();
        }

        if (!canUse(this.getOnlineUser(player), identifier)) {
            KiloChat.sendMessageTo(player, KiloCommands.getPermissionError("?"));
            return FAILED;
        }

        addPlayer(player.getUuid(), identifier);
        player.sendMessage(LangText.getFormatter(true, "command.magicalparticles.set", getAnimationName(identifier)), silent);
        return SUCCESS;
    }

    private int list(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Texter.ListStyle text = Texter.ListStyle.of(
                "Particle Animations", Formatting.GOLD, Formatting.DARK_GRAY, Formatting.WHITE, Formatting.GRAY
        );

        text.append("&cdisable",
                Texter.Events.onHover(new LiteralText("Click Here to Disable").formatted(Formatting.GOLD)
                ),
                Texter.Events.onClickRun("/mp disable")
        ).append("&7|&r");

        map.forEach((id, animation) -> text.append(id.getPath(),
                Texter.Events.onHover(new LiteralText("")
                        .append(new LiteralText(animation.getName()).formatted(Formatting.GOLD))
                        .append("\n")
                        .append(new LiteralText(animation.getId().toString()).formatted(Formatting.DARK_GRAY))
                        .append("\n")
                        .append(new LiteralText(tl("general.click_apply")).formatted(Formatting.YELLOW))
                ),
                Texter.Events.onClickRun("/mp set " + id.toString() + "--s")
        ));

        KiloChat.sendMessageTo(player, text.setSize(map.size()).build());
        return SUCCESS;
    }

    private int disable(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        KiloChat.sendLangMessageTo(player, "command.magicalparticles.disabled");
        removePlayer(player.getUuid());
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(context);
        List<Identifier> identifiers = new ArrayList<>();
        for (Map.Entry<Identifier, ParticleAnimation> entry : map.entrySet()) {
            ParticleAnimation animation = entry.getValue();

            if (animation.predicate() == null || (animation.predicate() != null && animation.predicate().test(user))) {
                identifiers.add(entry.getKey());
            }
        }
        return CommandSource.suggestIdentifiers(identifiers, builder);
    }

}