package org.kilocraft.essentials.extensions.magicalparticles;

import com.mojang.brigadier.CommandDispatcher;
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
import org.kilocraft.essentials.chat.KiloChat;
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
        RequiredArgumentBuilder<ServerCommandSource, Identifier> idArgument = argument("identifier", identifier())
                .suggests(this::particleIdSuggestions)
                .executes(this::setSelf);

        commandNode.addChild(idArgument.build());
    }

    private int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Identifier identifier = getIdentifier(ctx, "identifier");

        if (identifier.getPath().equals("disable")) {
            KiloChat.sendLangMessageTo(player, "command.magicalparticles.disabled.self", identifier.getPath());
            removePlayer(player.getUuid());
            return SINGLE_SUCCESS;
        }

        if (!isValidId(identifier))
            identifier = getIdFromPath(identifier.getPath());

        if (!isValidId(identifier))
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Particle animation").create();

        addPlayer(player.getUuid(), identifier);

        KiloChat.sendLangMessageTo(player, "command.magicalparticles.set.self", getAnimationName(identifier));
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        strings.add("disable");
        map.forEach((id, animation) -> strings.add(id.getPath()));
        return CommandSource.suggestMatching(strings, builder);
    }

}
