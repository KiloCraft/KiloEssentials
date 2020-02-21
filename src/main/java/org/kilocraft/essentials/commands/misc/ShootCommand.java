package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ThrowableEntities;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class ShootCommand extends EssentialCommand {
    public ShootCommand() {
        super("shoot", CommandPermission.SHOOT, new String[]{"throw"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> typeArgument = argument("projectileEntity", word())
                .suggests(this::suggestThrowableEntityTypes)
                .executes(this::execute);

        commandNode.addChild(typeArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ThrowableEntities.Type entityType = ThrowableEntities.Type.getByName(getString(ctx, "projectileEntity"));

        if (entityType == null)
            throw KiloCommands.getException(ExceptionMessageNode.INVALID, "Projectile Entity").create();

        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerWorld world = player.getServerWorld();
        Entity entity = ThrowableEntities.create(entityType, world, player, player.yaw, player.pitch);

        if (entity == null)
            throw KiloCommands.getException(ExceptionMessageNode.INTERNAL_ERROR, getClass().getName(), ctx.getSource().getName(), new Date().toString()).create();

        world.spawnEntity(entity);

        player.addMessage(new LiteralText("You have thrown a ").append(new TranslatableText(entity.getType().getTranslationKey())), true);
        return SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> suggestThrowableEntityTypes(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (ThrowableEntities.Type value : ThrowableEntities.Type.values()) strings.add(value.getName());
        return CommandSource.suggestMatching(strings, builder);
    }

}
