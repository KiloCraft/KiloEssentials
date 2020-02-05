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
import net.minecraft.util.Identifier;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;
import static org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager.*;

public class ParticleAnimationCommand extends EssentialCommand {
    public ParticleAnimationCommand(){
        super("particleanimation",
                src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.MAGIC_PARTICLES_SELF), new String[]{"paan", "pa"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Identifier> idArgument = argument("identifier", identifier())
                .suggests(this::particleIdSuggestions)
                .executes(this::setSelf);
        LiteralArgumentBuilder<ServerCommandSource> disableArgument = literal("disable")
                .executes(this::disableSelf);

        commandNode.addChild(disableArgument.build());
        commandNode.addChild(idArgument.build());
    }

    private int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Identifier identifier = getIdentifier(ctx, "identifier");

        if (!isValidId(identifier))
            identifier = getIdFromPath(identifier.getPath());

        if (!isValidId(identifier))
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Particle animation").create();

        addPlayer(player.getUuid(), identifier);

        KiloChat.sendMessageTo(ctx.getSource(), new ChatMessage("&c[Test]&r&e Set PAT: " + identifier.toString(), true));
        return SINGLE_SUCCESS;
    }

    private int disableSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        removePlayer(player.getUuid());

        KiloChat.sendMessageTo(ctx.getSource(), new ChatMessage("&c[Test]&r&e Set PAT: Disable", true));
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        map.forEach((id, animation) -> strings.add(id.getPath()));
        return CommandSource.suggestMatching(strings, builder);
    }

}
