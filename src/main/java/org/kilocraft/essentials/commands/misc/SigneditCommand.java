package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.network.packet.SignEditorOpenS2CPacket;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.command.arguments.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.arguments.IdentifierArgumentType.identifier;

public class SigneditCommand extends EssentialCommand {
    public SigneditCommand() {
        super("signedit", src ->
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_TEXT) ||
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_TEXT) ||
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COMMAND) ||
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_SELF) ||
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_OTHERS) ||
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COLOR));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> editNode = literal("text");
        LiteralArgumentBuilder<ServerCommandSource> guiNode = literal("gui")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_SELF))
                .executes(ctx -> openGui(ctx, ctx.getSource().getPlayer()));
        LiteralArgumentBuilder<ServerCommandSource> dyeColorNode = literal("color");
        LiteralArgumentBuilder<ServerCommandSource> executesNode = literal("runs")
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COMMAND));

        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = argument("line", integer(1, 4))
                .suggests(TabCompletions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = argument("string", greedyString())
                .suggests(this::setTextSuggestions)
                .executes(this::setText);

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> guiSelectorArgument = argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_OTHERS))
                .suggests(TabCompletions::allPlayers)
                .executes(ctx -> openGui(ctx, getPlayer(ctx, "target")));

        RequiredArgumentBuilder<ServerCommandSource, Identifier> dyeColorArgument = argument("color", identifier())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COLOR))
                .suggests(this::dyeColorSuggestions)
                .executes(this::setDyeColor);

        RequiredArgumentBuilder<ServerCommandSource, Integer> executesLineArgument = argument("line", integer(1, 4))
                .suggests(TabCompletions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> executesArgument = argument("command", greedyString())
                .suggests(this::setCommandSuggestions)
                .executes(this::setCommand);

        lineArgument.then(stringArgument);
        editNode.then(lineArgument);
        guiNode.then(guiSelectorArgument);
        dyeColorNode.then(dyeColorArgument);
        executesLineArgument.then(executesArgument);
        executesNode.then(executesLineArgument);
        commandNode.addChild(editNode.build());
        commandNode.addChild(guiNode.build());
        commandNode.addChild(dyeColorNode.build());
        commandNode.addChild(executesNode.build());
        argumentBuilder.executes(ctx -> openGui(ctx, ctx.getSource().getPlayer()));
    }

    private int setText(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        int line = getInteger(ctx, "line") - 1;
        String input = getString(ctx, "string");

        if (TextFormat.removeAlternateColorCodes('&', input).length() > 17)
            throw KiloCommands.getException(ExceptionMessageNode.STRING_TOO_LONG, 17).create();

        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity == null) {
            KiloChat.sendLangMessageTo(player, "command.signedit.invalid_block");
            return SINGLE_FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;

        if (input.equals("reset")) {
            sign.setTextOnRow(line, new LiteralText(""));
            updateSign(sign, player.getServerWorld(), blockEntity.getPos());
            KiloChat.sendLangMessageTo(player, "command.signedit.reset_text", line + 1);
            return SINGLE_SUCCESS;
        }

        sign.setTextOnRow(line, new LiteralText(TextFormat.translate(input)));

        updateSign(sign, player.getServerWorld(), blockEntity.getPos());
        KiloChat.sendLangMessageTo(player, "command.signedit.set_text", line + 1, input.substring(17));
        return SINGLE_SUCCESS;
    }

    private int setCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        int line = getInteger(ctx, "line") - 1;
        String input = getString(ctx, "command");

        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity == null) {
            KiloChat.sendLangMessageTo(player, "command.signedit.invalid_block");
            return SINGLE_FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;

        if (input.equals("reset")) {
            Text text = sign.text[line].styled((style) -> style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "")));
            sign.setTextOnRow(line, text);
            KiloChat.sendLangMessageTo(player, "command.signedit.reset_command", line + 1);
            return SINGLE_SUCCESS;
        }

        Text text = sign.text[line].styled((style) -> style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, input)));
        sign.setTextOnRow(line, text);
        updateSign(sign, player.getServerWorld(), blockEntity.getPos());
        KiloChat.sendLangMessageTo(player, "command.signedit.set_command", line + 1, input);
        return SINGLE_SUCCESS;
    }

    private int setDyeColor(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity == null) {
            KiloChat.sendLangMessageTo(player, "command.signedit.invalid_block");
            return SINGLE_FAILED;
        }

        String inputColor = getIdentifier(ctx, "color").getPath();
        if (!isValidColor(inputColor))
            throw KiloCommands.getException(ExceptionMessageNode.INVALID_DYE_COLOR).create();

        DyeColor dyeColor = DyeColor.valueOf(inputColor.toUpperCase());
        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setTextColor(dyeColor);
        updateSign(sign, player.getServerWorld(), sign.getPos());

        KiloChat.sendLangMessageTo(player, "command.signedit.set_color", inputColor);
        return SINGLE_SUCCESS;
    }

    private int openGui(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity == null) {
            KiloChat.sendLangMessageTo(player, "command.signedit.invalid_block");
            return SINGLE_FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setEditor(target);

        SignEditorOpenS2CPacket packet = new SignEditorOpenS2CPacket(sign.getPos());
        target.networkHandler.sendPacket(packet);

        if (CommandHelper.areTheSame(ctx.getSource(), target))
            KiloChat.sendLangMessageTo(target, "general.open_gui", "Sign");
        else
            KiloChat.sendLangMessageTo(ctx.getSource(), "general.open_gui.others", "Sign", target.getEntityName());

        return SINGLE_SUCCESS;
    }

    private void updateSign(SignBlockEntity sign, ServerWorld world, BlockPos pos) {
        sign.markDirty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
    }

    private BlockEntity getBlockEntityAtCrosshair(ServerPlayerEntity player) {
        ServerWorld world = player.getServerWorld();

        HitResult hitResult = ((EntityServerRayTraceable) player).rayTrace(10, 1, true);

        if (hitResult.getType() != HitResult.Type.BLOCK)
            return null;

        BlockEntity entity = world.getBlockEntity(new BlockPos(hitResult.getPos().getX(), hitResult.getPos().getY(), hitResult.getPos().getZ()));

        if (entity != null && entity.getType() == BlockEntityType.SIGN)
            return entity;

        return null;
    }

    private CompletableFuture<Suggestions> dyeColorSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (DyeColor value : DyeColor.values()) strings.add(value.getName());
        return CommandSource.suggestMatching(strings, builder);
    }

    private CompletableFuture<Suggestions> setTextSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        strings.add("reset");
        ServerPlayerEntity player = context.getSource().getPlayer();
        int line = getInteger(context, "line") - 1;

        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity != null) {
            SignBlockEntity sign = (SignBlockEntity) blockEntity;
            strings.add(TextFormat.reverseTranslate(sign.text[line].asFormattedString(), '&'));
            return CommandSource.suggestMatching(strings, builder);
        }

        return TabCompletions.noSuggestions(context, builder);
    }

    private CompletableFuture<Suggestions> setCommandSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        strings.add("reset");
        ServerPlayerEntity player = context.getSource().getPlayer();
        int line = getInteger(context, "line") - 1;

        BlockEntity blockEntity = getBlockEntityAtCrosshair(player);
        if (blockEntity != null) {
            SignBlockEntity sign = (SignBlockEntity) blockEntity;
            ClickEvent clickEvent = sign.text[line].getStyle().getClickEvent();

            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                strings.add(clickEvent.getValue());
                return CommandSource.suggestMatching(strings, builder);
            }
        }

        return TabCompletions.noSuggestions(context, builder);
    }

    private boolean isValidColor(String str) {
        for (DyeColor value : DyeColor.values()) {
            if (value.getName().equals(str))
                return true;
        }

        return false;
    }
}
