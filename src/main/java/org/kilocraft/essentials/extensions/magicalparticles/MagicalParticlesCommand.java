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
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
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
                src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.MAGIC_PARTICLES_SELF), new String[]{"mp"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, Identifier> idArgument = argument("animationId", identifier())
                .suggests(this::particleIdSuggestions)
                .executes(this::setSelf);

        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, EssentialPermission.MAGIC_PARTICLES_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(this::setOthers);

        idArgument.then(userArgument);
        commandNode.addChild(idArgument.build());
    }

    private int setSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Identifier identifier = getIdentifier(ctx, "animationId");

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

        KiloChat.sendLangMessageTo(player, "command.magicalparticles.set.self", identifier.getPath());
        return SINGLE_SUCCESS;
    }

    private int setOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser source = getOnlineUser(ctx);
        Identifier identifier = getIdentifier(ctx, "animationId");
        String targetName = getUserArgumentInput(ctx, "user");
        if (!isValidId(identifier))
            identifier = getIdFromPath(identifier.getPath());

        if (!isValidId(identifier))
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Particle animation").create();

        final Identifier finalIdentifier = identifier;
        essentials.getUserThenAcceptAsync(source, targetName, (user) -> {
            if (finalIdentifier.getPath().equals("disable")) {
                source.sendLangMessage("command.magicalparticles.disabled.others", finalIdentifier.getPath());
                removePlayer(source.getUuid());
                return;
            }

            addPlayer(user.getUuid(), finalIdentifier);

            if (source.getUuid().equals(user.getUuid())) {
                source.sendLangMessage("command.magicalparticles.set.self", finalIdentifier.getPath());
            } else {
                source.sendLangMessage("command.magicalparticles.set.others", finalIdentifier.getPath(), user.getNameTag());
            }
        });

        return AWAIT_RESPONSE;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        strings.add("disable");
        map.forEach((id, animation) -> strings.add(id.getPath()));
        return CommandSource.suggestMatching(strings, builder);
    }

}
