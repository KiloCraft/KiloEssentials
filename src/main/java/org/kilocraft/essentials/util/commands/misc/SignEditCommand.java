package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
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

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;

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
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> editNode = this.literal("text");
        LiteralArgumentBuilder<ServerCommandSource> guiNode = this.literal("gui")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_GUI_SELF))
                .executes(ctx -> this.openGui(ctx, ctx.getSource().getPlayer()));
        LiteralArgumentBuilder<ServerCommandSource> dyeColorNode = this.literal("color");
        LiteralArgumentBuilder<ServerCommandSource> executesNode = this.literal("runs")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_COMMAND));
        LiteralArgumentBuilder<ServerCommandSource> typeNode = this.literal("type")
                .requires(src -> this.hasPermission(src, CommandPermission.SIGNEDIT_COLOR));

        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = this.argument("line", integer(1, 4))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> stringArgument = this.argument("string", greedyString())
                .suggests(this::setTextSuggestions)
                .executes(this::setText);

        RequiredArgumentBuilder<ServerCommandSource, EntitySelector> guiSelectorArgument = this.argument("target", player())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_GUI_OTHERS))
                .suggests(ArgumentSuggestions::allPlayers)
                .executes(ctx -> this.openGui(ctx, getPlayer(ctx, "target")));

        RequiredArgumentBuilder<ServerCommandSource, Identifier> dyeColorArgument = this.argument("color", identifier())
                .requires(src -> KiloCommands.hasPermission(src, CommandPermission.SIGNEDIT_COLOR))
                .suggests(this::dyeColorSuggestions)
                .executes(this::setDyeColor);

        RequiredArgumentBuilder<ServerCommandSource, Integer> executesLineArgument = this.argument("line", integer(1, 4))
                .suggests(ArgumentSuggestions::noSuggestions);

        RequiredArgumentBuilder<ServerCommandSource, String> executesArgument = this.argument("command", greedyString())
                .suggests(this::setCommandSuggestions)
                .executes(this::setCommand);

        RequiredArgumentBuilder<ServerCommandSource, String> typeArgument = this.argument("type", string())
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
        this.argumentBuilder.executes(ctx -> this.openGui(ctx, ctx.getSource().getPlayer()));
    }

    private int setText(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
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
            sign.setTextOnRow(line, new LiteralText(""));
            this.updateSign(sign, player.getServerWorld(), blockEntity.getPos());
            user.sendLangMessage("command.signedit.reset_text", line + 1);
            return SUCCESS;
        }

        sign.setTextOnRow(line, ComponentText.toText(input));

        this.updateSign(sign, player.getServerWorld(), blockEntity.getPos());
        user.sendLangMessage("command.signedit.set_text", line + 1, input);
        return SUCCESS;
    }

    private int setCommand(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
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
            Text text = ((MutableText) signText.getTexts()[line]).styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "")));
            sign.setTextOnRow(line, text);
            user.sendLangMessage("command.signedit.reset_command", line + 1);
            return SUCCESS;
        }

        Text text = ((MutableText) signText.getTexts()[line]).styled((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, input)));
        sign.setTextOnRow(line, text);
        this.updateSign(sign, player.getServerWorld(), blockEntity.getPos());
        user.sendLangMessage("command.signedit.set_command", line + 1, input);
        return SUCCESS;
    }

    private int setDyeColor(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        OnlineUser user = this.getOnlineUser(ctx);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        String inputColor = getIdentifier(ctx, "color").getPath();
        if (!this.isValidColor(inputColor))
            throw KiloCommands.getException("exception.incorrect_identifier", "dye color").create();

        DyeColor dyeColor = DyeColor.valueOf(inputColor.toUpperCase());
        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setTextColor(dyeColor);
        this.updateSign(sign, player.getServerWorld(), sign.getPos());

        user.sendLangMessage("command.signedit.set_color", inputColor);
        return SUCCESS;
    }

    private int openGui(CommandContext<ServerCommandSource> ctx, ServerPlayerEntity target) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        OnlineUser user = this.getOnlineUser(ctx);
        if (blockEntity == null) {
            user.sendLangMessage("command.signedit.invalid_block");
            return FAILED;
        }

        SignBlockEntity sign = (SignBlockEntity) blockEntity;
        sign.setEditor(target.getUuid());

        SignEditorOpenS2CPacket packet = new SignEditorOpenS2CPacket(sign.getPos());
        target.networkHandler.sendPacket(packet);

        if (CommandUtils.areTheSame(ctx.getSource(), target))
            user.sendLangMessage("general.open_gui", "Sign");
        else
            user.sendLangMessage("general.open_gui.others", "Sign", target.getEntityName());

        return SUCCESS;
    }

    private int setType(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
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
        BlockState oldState = sign.getCachedState();
        boolean wallSign = oldState.getBlock() instanceof WallSignBlock;
        Block newBlock = wallSign ? type.getWallBlock() : type.getBlock();
        BlockState newState = newBlock.getDefaultState();

        if (wallSign) {
            newState = newState.with(Properties.HORIZONTAL_FACING, oldState.get(Properties.HORIZONTAL_FACING));
        } else {
            newState = newState.with(Properties.ROTATION, oldState.get(Properties.ROTATION));
        }

        SignBlockEntity newSign = new SignBlockEntity(sign.getPos(), sign.getCachedState());
        for (int i = 0; i < 4; i++) {
            newSign.setTextOnRow(i, ((SignBlockEntityAccessor) sign).getTexts()[i]);
        }
        if (sign.getTextColor() != DyeColor.BLACK)
            newSign.setTextColor(sign.getTextColor());

        ServerWorld world = player.getServerWorld();
        world.setBlockState(sign.getPos(), newState);
        world.updateNeighbors(sign.getPos(), newState.getBlock());
        world.addBlockEntity(newSign);
        user.sendLangMessage("command.signedit.set_type", inputType);
        return SUCCESS;
    }

    private void updateSign(SignBlockEntity sign, ServerWorld world, BlockPos pos) {
        sign.markDirty();
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 3);
    }

    private BlockEntity getBlockEntityAtCursor(ServerPlayerEntity player) {
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
        return CommandSource.suggestMatching(strings, builder);
    }

    private CompletableFuture<Suggestions> setCommandSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        List<String> strings = new ArrayList<>();
        strings.add("reset");
        ServerPlayerEntity player = context.getSource().getPlayer();
        int line = getInteger(context, "line") - 1;

        BlockEntity blockEntity = this.getBlockEntityAtCursor(player);
        if (blockEntity != null) {
            SignBlockEntity sign = (SignBlockEntity) blockEntity;
            ClickEvent clickEvent = ((SignBlockEntityAccessor) sign).getTexts()[line].getStyle().getClickEvent();

            if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                strings.add(clickEvent.getValue());
                return CommandSource.suggestMatching(strings, builder);
            }
        }

        return ArgumentSuggestions.noSuggestions(context, builder);
    }

    private CompletableFuture<Suggestions> typeSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> strings = new ArrayList<>();
        for (SignType value : SignType.values()) strings.add(value.getName());
        return CommandSource.suggestMatching(strings, builder);
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
