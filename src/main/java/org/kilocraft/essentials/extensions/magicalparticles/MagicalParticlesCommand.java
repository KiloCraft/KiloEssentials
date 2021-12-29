package org.kilocraft.essentials.extensions.magicalparticles;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.ParticleAnimation;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.arguments.ResourceLocationArgument.getId;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;
import static org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager.*;

public class MagicalParticlesCommand extends EssentialCommand {
    public MagicalParticlesCommand() {
        super("magicalparticles",
                src -> KiloEssentials.hasPermissionNode(src, EssentialPermission.MAGIC_PARTICLES_SELF), new String[]{"mp", "particleanimation", "pa"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> setArg = this.literal("set");
        LiteralArgumentBuilder<CommandSourceStack> listArg = this.literal("list")
                .executes(this::list);
        LiteralArgumentBuilder<CommandSourceStack> disableArg = this.literal("disable")
                .executes(this::disable);

        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> idArgument = this.argument("animation", id())
                .suggests(this::particleIdSuggestions)
                .executes(this::set);

        setArg.then(idArgument);
        this.commandNode.addChild(listArg.build());
        this.commandNode.addChild(setArg.build());
        this.commandNode.addChild(disableArg.build());
        this.argumentBuilder.executes(this::list);
    }

    private int set(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        OnlineUser user = this.getOnlineUser(ctx);
        boolean silent = false;
        ResourceLocation identifier = getId(ctx, "animation");

        if (identifier.getPath().endsWith("--s")) {
            silent = true;
            identifier = new ResourceLocation(identifier.toString().replaceFirst("--s", ""));
        }

        if (!isValidId(identifier)) {
            identifier = getIdFromPath(identifier.getPath());
        }

        if (!isValidId(identifier)) {
            throw KiloCommands.getException("exception.invalid", "Particle animation").create();
        }

        if (!canUse(this.getOnlineUser(player), identifier)) {
            user.sendPermissionError("?");
            return FAILED;
        }

        addPlayer(player.getUUID(), identifier);
        player.displayClientMessage(StringText.of("command.magicalparticles.set", getAnimationName(identifier)), silent);
        return SUCCESS;
    }

    private int list(CommandContext<CommandSourceStack> ctx) {
        OnlineUser user = this.getCommandSource(ctx);
        Texter.ListStyle text = Texter.ListStyle.of(
                "Particle Animations", ChatFormatting.GOLD, ChatFormatting.DARK_GRAY, ChatFormatting.WHITE, ChatFormatting.GRAY
        );

        text.append("&cdisable",
                Texter.Events.onHover(new TextComponent("Click Here to Disable").withStyle(ChatFormatting.GOLD)
                ),
                Texter.Events.onClickRun("/mp disable")
        ).append("&7|&r");

        map.forEach((id, animation) -> text.append(id.getPath(),
                Texter.Events.onHover(new TextComponent("")
                        .append(new TextComponent(animation.getName()).withStyle(ChatFormatting.GOLD))
                        .append("\n")
                        .append(new TextComponent(animation.getId().toString()).withStyle(ChatFormatting.DARK_GRAY))
                        .append("\n")
                        .append(new TextComponent(ModConstants.translation("general.click_apply")).withStyle(ChatFormatting.YELLOW))
                ),
                Texter.Events.onClickRun("/mp set " + id + "--s")
        ));

        user.sendMessage(text.setSize(map.size()).build());
        return SUCCESS;
    }

    private int disable(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        CommandSourceUser user = this.getCommandSource(ctx);
        user.sendLangMessage("command.magicalparticles.disabled");
        removePlayer(player.getUUID());
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> particleIdSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(context);
        List<String> usableAnimations = new ArrayList<>();
        for (Map.Entry<ResourceLocation, ParticleAnimation> entry : map.entrySet()) {
            ParticleAnimation animation = entry.getValue();

            if (animation.canUse(user)) {
                usableAnimations.add(entry.getKey().getPath());
            }
        }
        return SharedSuggestionProvider.suggest(usableAnimations, builder);
    }

}