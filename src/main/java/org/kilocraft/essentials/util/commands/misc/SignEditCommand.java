package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.mixin.accessor.SignBlockEntityAccessor;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.HitResult;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;
import static net.minecraft.commands.arguments.ResourceLocationArgument.getId;
import static net.minecraft.commands.arguments.ResourceLocationArgument.id;

public class SignEditCommand extends EssentialCommand {
    public SignEditCommand() {
        super("signedit", src ->
                KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_TEXT) ||
                        KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_TEXT) ||
                        KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COMMAND) ||
                        KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_SELF) ||
                        KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_OTHERS) ||
                        KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COLOR));
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> editNode = this.literal("text");
        LiteralArgumentBuilder<CommandSourceStack> guiNode = this.literal("gui")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_GUI_SELF))
                .executes(ctx -> this.openGui(ctx, ctx.getSource().getPlayerOrException()));
        LiteralArgumentBuilder<CommandSourceStack> dyeColorNode = this.literal("color");
        LiteralArgumentBuilder<CommandSourceStack> executesNode = this.literal("runs")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_COMMAND));
        LiteralArgumentBuilder<CommandSourceStack> typeNode = this.literal("type")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_COLOR));

        RequiredArgumentBuilder<CommandSourceStack, Integer> lineArgument = this.argument("line", integer(1, 4))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<CommandSourceStack, String> stringArgument = this.argument("string", greedyString())
                .suggests(this::setTextSuggestions)
                .executes(this::setText);

        RequiredArgumentBuilder<CommandSourceStack, EntitySelector> guiSelectorArgument = this.argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.openGui(ctx, getPlayer(ctx, "target")));

        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> dyeColorArgument = this.argument("color", id())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COLOR))
                .suggests(this::dyeColorSuggestions)
                .executes(this::setDyeColor);

        RequiredArgumentBuilder<CommandSourceStack, Integer> executesLineArgument = this.argument("line", integer(1, 4))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<CommandSourceStack, String> executesArgument = this.argument("command", greedyString())
                .suggests(this::setCommandSuggestions)
                .executes(this::setCommand);

        RequiredArgumentBuilder<CommandSourceStack, String> typeArgument = this.argument("type", string())
                .suggests(this::typeSuggestions)
                .executes(this::setType);

        lineArgument.then(stringArgument);
        editNode.then(lineArgument);
        guiNode.then(guiSelectorArgument);
        dyeColorNode.then(dyeColorArgument);
        executesLineArgument.then(executesArgument);
        executesNode.then(executesLineArgument);
        typeNode.then(typeArgument);
        this.commandNode.addChild(editNode.build());
        this.commandNode.addChild(guiNode.build());
        this.commandNode.addChild(dyeColorNode.build());
        this.commandNode.addChild(executesNode.build());
        this.commandNode.addChild(typeNode.build());
        this.argumentBuilder.executes(ctx -> this.openGui(ctx, ctx.getSource().getPlayerOrException()));
    }

    private int setText(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int line = getInteger(ctx, "line") - 1;
        String input = getString(ctx, "string");
        OnlineUser user = this.getOnlineUser(ctx);

        if (ComponentText.clearFormatting(input).length() > 17)
            throw KiloCommands.getException("exception.string_too_long", 17).create();

        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;

        if (input.equals("reset")) {
            sign.setMessage(line, new TextComponent(""));
            this.updateSign(sign, player.getLevel(), blockEntity.getBlockPos());
            user.sendLangMessage("command.signedit.reset_text", line + 1);
            return SUCCESS;
        }

        sign.setMessage(line, ComponentText.toText(input));

        this.updateSign(sign, player.getLevel(), blockEntity.getBlockPos());
        user.sendLangMessage("command.signedit.set_text", line + 1, input);
        return SUCCESS;
    }

    private int setCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        int line = getInteger(ctx, "line") - 1;
        String input = getString(ctx, "command");
        OnlineUser user = this.getOnlineUser(ctx);

        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        SignBlockEntityAccessor signText = ((SignBlockEntityAccessor) sign);

        if (input.equals("reset")) {
            Component text = ((MutableComponent) signText.getMessages()[line]).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "")));
            sign.setMessage(line, text);
            user.sendLangMessage("command.signedit.reset_command", line + 1);
            return SUCCESS;
        }

        Component text = ((MutableComponent) signText.getMessages()[line]).withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, input)));
        sign.setMessage(line, text);
        this.updateSign(sign, player.getLevel(), blockEntity.getBlockPos());
        user.sendLangMessage("command.signedit.set_command", line + 1, input);
        return SUCCESS;
    }

    private int setDyeColor(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        OnlineUser user = this.getOnlineUser(ctx);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        String inputColor = getId(ctx, "color").getPath();
        if (!this.isValidColor(inputColor))
            throw KiloCommands.getException("exception.incorrect_identifier", "dye color").create();

        DyeColor dyeColor = DyeColor.valueOf(inputColor.toUpperCase());
        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setColor(dyeColor);
        this.updateSign(sign, player.getLevel(), sign.getBlockPos());

        user.sendLangMessage("command.signedit.set_color", inputColor);
        return SUCCESS;
    }

    private int openGui(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        OnlineUser user = this.getOnlineUser(ctx);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setAllowedPlayerEditor(target.getUUID());

        ClientboundOpenSignEditorPacket packet = new ClientboundOpenSignEditorPacket(sign.getBlockPos());
        target.connection.send(packet);

        if (CommandUtils.areTheSame(ctx.getSource(), target))
            user.sendLangMessage("general.open_gui", "Sign");
        else
            user.sendLangMessage("general.open_gui.others", "Sign", target.getScoreboardName());

        return SUCCESS;
    }

    private int setType(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        OnlineUser user = this.getOnlineUser(ctx);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        String inputType = getString(ctx, "type");
        SignType type = SignType.getByName(inputType);
        if (type == null) {
            throw KiloCommands.getException("exception.invalid", "Sign Type").create();
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        BlockState oldState = sign.getBlockState();
        boolean wallSign = oldState.getBlock() instanceof WallSignBlock;
        Block newBlock = wallSign ? type.getWallBlock() : type.getBlock();
        BlockState newState = newBlock.defaultBlockState();

        if (wallSign) {
            newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING, oldState.getValue(BlockStateProperties.HORIZONTAL_FACING));
        } else {
            newState = newState.setValue(BlockStateProperties.ROTATION_16, oldState.getValue(BlockStateProperties.ROTATION_16));
        }

        SignBlockEntity newSign = new SignBlockEntity(sign.getBlockPos(), sign.getBlockState());
        for (int i = 0; i < 4; i++) {
            newSign.setMessage(i, ((SignBlockEntityAccessor) sign).getMessages()[i]);
        }
        if (sign.getColor() != DyeColor.BLACK)
            newSign.setColor(sign.getColor());

        ServerLevel world = player.getLevel();
        world.setBlockAndUpdate(sign.getBlockPos(), newState);
        world.blockUpdated(sign.getBlockPos(), newState.getBlock());
        world.setBlockEntity(newSign);
        user.sendLangMessage("command.signedit.set_type", inputType);
        return SUCCESS;
    }

    private void updateSign(SignBlockEntity sign, ServerLevel world, BlockPos pos) {
        sign.setChanged();
        BlockState state = world.getBlockState(pos);
        world.sendBlockUpdated(pos, state, state, 3);
    }

    private BlockEntity getBlockEntityAtCursor(ServerPlayer player) {
        ServerLevel world = player.getLevel();
        HitResult hitResult = ((EntityServerRayTraceable) player).rayTrace(10, 1, true);
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return null;
        BlockEntity entity = world.getBlockEntity(new BlockPos(hitResult.getLocation().x(), hitResult.getLocation().y(), hitResult.getLocation().z()));
        if (entity != null && entity.getType() == BlockEntityType.SIGN)
            return entity;

        return null;
    }

    private CompletableFuture<Suggestions> dyeColorSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (DyeColor value : DyeColor.values()) strings.add(value.getName());
        return SharedSuggestionProvider.suggest(strings, builder);
    }

    private CompletableFuture<Suggestions> setTextSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        strings.add("reset");
        return SharedSuggestionProvider.suggest(strings, builder);
    }

    private CompletableFuture<Suggestions> setCommandSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        strings.add("reset");
        ServerPlayer player = context.getSource().getPlayerOrException();
        int line = getInteger(context, "line") - 1;

        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        if (blockEntity != null) {
            SignBlockEntity sign = (SignBlockEntity) blockEntity;
            ClickEvent clickEvent = ((SignBlockEntityAccessor) sign).getMessages()[line].getStyle().getClickEvent();

            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                strings.add(clickEvent.getValue());
                return SharedSuggestionProvider.suggest(strings, builder);
            }
        }

        return ArgumentSuggestions.noSuggestions(context, builder);
    }

    private CompletableFuture<Suggestions> typeSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (SignType value : SignType.values()) strings.add(value.getName());
        return SharedSuggestionProvider.suggest(strings, builder);
    }

    private boolean isValidColor(String str) {
        for (DyeColor value : DyeColor.values()) {
            if (value.getName().equals(str))
                return true;
        }

        return false;
    }

    private enum SignType {
        OAK("oak", Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN),
        BIRCH("birch", Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN),
        SPRUCE("spruce", Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN),
        JUNGLE("jungle", Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN),
        ACACIA("acacia", Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN),
        DARK_OAK("dark_oak", Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN),
        CRIMSON("crimson", Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN),
        WARPED("warped", Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN);

        private final String name;
        private final Block block;
        private final Block wallBlock;

        SignType(String name, Block block, Block wallBlock) {
            this.name = name;
            this.block = block;
            this.wallBlock = wallBlock;
        }

        public String getName() {
            return this.name;
        }

        public Block getBlock() {
            return this.block;
        }

        public Block getWallBlock() {
            return this.wallBlock;
        }

        @Nullable
        public static SignType getByName(String name) {
            for (SignType value : values()) {
                if (value.name.equals(name))
                    return value;
            }

            return null;
        }
    }
}
