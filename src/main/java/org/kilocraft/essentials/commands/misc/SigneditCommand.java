package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.chat.KiloChat;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class SigneditCommand extends EssentialCommand {
    public SigneditCommand() {
        super("signedit", CommandPermission.SIGNEDIT);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> editNode = literal("text");
        LiteralArgumentBuilder<ServerCommandSource> guiNode = literal("gui");
        LiteralArgumentBuilder<ServerCommandSource> dyeColorNode = literal("color");
        LiteralArgumentBuilder<ServerCommandSource> editableNode = literal("editable");

        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 4))
                .suggests(TabCompletions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = argument("string", greedyString())
                .executes(this::setText);

        editNode.then(lineArgument);
        lineArgument.then(stringArgument);
        commandNode.addChild(lineArgument.build());
    }

    private int setText(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerCommandSource source = ctx.getSource();
        ServerPlayerEntity player = source.getPlayer();
        int line = getInteger(ctx, "line") - 1;
        String input = getString(ctx, "string");

        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity == null) {
            KiloChat.sendLangMessageTo(player, "command.signedit.invalid_block");
            return SINGLE_FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setTextOnRow(line, new LiteralText(TextFormat.translate(input)));

        updateSign(sign, player.getServerWorld(), blockEntity.getPos());
        return SINGLE_SUCCESS;
    }

    private int openGui(CommandContext<ServerCommandSource> ctx) {

        return SINGLE_SUCCESS;
    }

    private void updateSign(SignBlockEntity sign, ServerWorld world, BlockPos pos) {
        sign.markDirty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
    }

    private BlockEntity getBlockEntityAtCrosshair(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        HitResult hitResult = ((EntityServerRayTraceable) player).rayTrace(5, 1, true);

        if (hitResult.getType() != HitResult.Type.BLOCK)
            return null;

        BlockEntity entity = world.getBlockEntity(new BlockPos(hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ()));

        if (entity != null && entity.getType() == BlockEntityType.SIGN)
            return entity;

        return null;
    }

}
