package org.kilocraft.essentials.util.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.preference.Preference;
import org.kilocraft.essentials.api.util.schedule.AbstractScheduler;
import org.kilocraft.essentials.api.world.location.exceptions.InsecureDestinationException;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.RtpSpecsConfigSection;
import org.kilocraft.essentials.patch.ChunkManager;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Random;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.argument.EntityArgumentType.getPlayer;

public class RtpCommand extends EssentialCommand {
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_SELF);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_IGNORE_LIMIT = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_BYPASS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER_DIMENSIONS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERDIMENSIONS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_MANAGE = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_MANAGE);
    private static final Preference<Integer> RTP_LEFT = Preferences.RANDOM_TELEPORTS_LEFT;

    public RtpCommand() {
        super("rtp", PERMISSION_CHECK_SELF, new String[]{"wilderness", "wild"});
    }

    private void teleport(ServerCommandSource src, ServerPlayerEntity target, ServerWorld targetWorld) {
        OnlineUser targetUser = this.getUserManager().getOnline(target);
        RtpSpecsConfigSection cfg = KiloConfig.main().rtpSpecs();
        if (targetUser.getPreference(RTP_LEFT) < 0) {
            targetUser.getPreferences().set(RTP_LEFT, 0);
        }

        // Check if the player has any rtps left or permission to ignore the limit
        if (targetUser.getPreference(RTP_LEFT) <= 0 && !PERMISSION_CHECK_IGNORE_LIMIT.test(src)) {
            targetUser.sendLangMessage("command.rtp.empty");
            return;
        }

        // Check if the target is in the correct dimension or has permission to perform the command in other dimensions
        if (RegistryUtils.dimensionTypeToRegistryKey(targetWorld.getDimension()) != World.OVERWORLD && !PERMISSION_CHECK_OTHER_DIMENSIONS.test(src)) {
            targetUser.sendLangMessage("command.rtp.dimension_exception");
            return;
        }

        if (!cfg.broadcastMessage.isEmpty()) {
            KiloChat.broadCast(String.format(cfg.broadcastMessage, targetUser.getFormattedDisplayName()));
        }

        this.attemptTeleport(src, targetUser, targetWorld);
    }

    private void attemptTeleport(ServerCommandSource src, OnlineUser targetUser, ServerWorld targetWorld) {
        targetUser.sendLangMessage("command.rtp.searching");
        final BlockPos blockPos = this.getBlockPos(targetWorld);
        if (blockPos != null) {
            ServerPlayerEntity target = targetUser.asPlayer();
            targetUser.sendLangMessage("command.rtp.loading");
            // Add a custom ticket to gradually preload chunks
            targetWorld.getChunkManager().addTicket(ChunkTicketType.create("rtp", Integer::compareTo, 300), new ChunkPos(blockPos), 1, target.getId()); // Lag reduction
            this.teleport(src, targetUser, targetWorld, blockPos.mutableCopy(), 0);
        } else {
            targetUser.sendLangMessage("command.rtp.failed.invalid_biome");
        }
    }

    @Nullable
    private BlockPos getBlockPos(ServerWorld world) {
        Random random = new Random();
        RtpSpecsConfigSection config = KiloConfig.main().rtpSpecs();
        BlockPos pos;
        for (int i = 0; i < config.maxTries; i++) {
            int x = random.nextInt(config.max - config.min) + config.min * (random.nextBoolean() ? 1 : -1);
            x += config.centerX;
            int z = random.nextInt(config.max - config.min) + config.min * (random.nextBoolean() ? 1 : -1);
            z += config.centerZ;
            pos = new BlockPos(x, 64, z);
            Biome biome = world.getBiome(pos);
            final Identifier identifier = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
            assert identifier != null;
            boolean invalidBiome = false;
            for (final String blackListed : config.blackListedBiomes) {
                if (identifier.toString().equals(blackListed)) {
                    invalidBiome = true;
                    break;
                }
            }
            if (!invalidBiome) {
                return pos;
            }
        }
        return null;
    }

    private void teleport(final ServerCommandSource src, final OnlineUser targetUser, final ServerWorld world, final BlockPos.Mutable pos, int attempts) {
        // Check every ~4 ticks
        AbstractScheduler.start(175, () -> {
            final Chunk chunk = ChunkManager.getChunkIfLoaded(world, pos);
            if (chunk == null) {
                if (attempts > 70) {
                    targetUser.sendLangMessage("command.rtp.failed.too_slow");
                } else {
                    this.teleport(src, targetUser, world, pos, attempts + 1);
                }
            } else {
                try {
                    ServerPlayerEntity target = targetUser.asPlayer();
                    if (target != null) {
                        pos.setY(this.getY(world, pos.getX(), world.getTopY(), pos.getZ()));
                        target.teleport(world, pos.getX(), pos.getY(), pos.getZ(), target.getYaw(), target.getPitch());
                        if (!PERMISSION_CHECK_IGNORE_LIMIT.test(src)) {
                            targetUser.getPreferences().set(RTP_LEFT, targetUser.getPreference(RTP_LEFT) - 1);
                        }
                        Biome biome = world.getBiome(pos);
                        final Identifier identifier = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
                        assert identifier != null;
                        targetUser.sendLangMessage("command.rtp.success", identifier.getPath(), pos.getX(), pos.getY(), pos.getZ(), RegistryUtils.dimensionToName(world.getDimension()));
                    }
                } catch (InsecureDestinationException e) {
                    targetUser.sendLangMessage("command.rtp.failed.unsafe");
                }
            }
        });
    }

    private int getY(BlockView blockView, int x, int maxY, int z) throws InsecureDestinationException {
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, (double) (maxY + 1), z);
        boolean isAir = blockView.getBlockState(mutable).isAir();
        mutable.move(Direction.DOWN);

        boolean bl3;
        for (boolean bl2 = blockView.getBlockState(mutable).isAir(); mutable.getY() > blockView.getBottomY(); bl2 = bl3) {
            mutable.move(Direction.DOWN);
            bl3 = blockView.getBlockState(mutable).isAir();
            if (!bl3 && bl2 && isAir) {
                return mutable.getY() + 1;
            }

            isAir = bl2;
        }
        throw new InsecureDestinationException("The destination does not have any block at this position");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> addArgument = this.literal("add")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(this.argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                this.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeAdd)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> setArgument = this.literal("set")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(this.argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                this.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeSet)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = this.literal("remove")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(this.argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers).then(
                                this.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeRemove)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> sendArgument = this.literal("send")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(this.argument("target", EntityArgumentType.player())
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(ctx -> this.executeOthers(ctx, null))
                        .then(this.argument("dimension", DimensionArgumentType.dimension())
                                .executes(ctx -> this.executeOthers(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")))
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> checkArgument = this.literal("check")
                .executes(this::executeLeft)
                .then(this.argument("target", EntityArgumentType.player())
                        .requires(PERMISSION_CHECK_OTHERS)
                        .suggests(ArgumentSuggestions::allPlayers)
                        .executes(this::executeGet)
                );

        LiteralArgumentBuilder<ServerCommandSource> performArgument = this.literal("perform")
                .executes(ctx -> this.executePerform(ctx, null))
                .then(this.argument("dimension", DimensionArgumentType.dimension())
                        .executes(ctx -> this.executePerform(ctx, DimensionArgumentType.getDimensionArgument(ctx, "dimension")))
                );

        this.commandNode.addChild(addArgument.build());
        this.commandNode.addChild(setArgument.build());
        this.commandNode.addChild(removeArgument.build());
        this.commandNode.addChild(sendArgument.build());
        this.commandNode.addChild(sendArgument.build());
        this.commandNode.addChild(checkArgument.build());
        this.commandNode.addChild(performArgument.build());
        this.argumentBuilder.executes(this::executeSelf);
    }

    private int executeLeft(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getUserManager().getOnline(ctx.getSource());
        CommandSourceServerUser.of(ctx)
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getPreference(RTP_LEFT));

        return user.getPreference(RTP_LEFT);
    }

    private int executeAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getUserManager().getOnline(getPlayer(ctx, "target"));
        int amountToAdd = getInteger(ctx, "amount");
        user.getPreferences().set(RTP_LEFT, user.getPreference(RTP_LEFT) + amountToAdd);
        CommandSourceServerUser.of(ctx)
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeSet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getUserManager().getOnline(getPlayer(ctx, "target"));
        int amountToSet = getInteger(ctx, "amount");
        user.getPreferences().set(RTP_LEFT, amountToSet);
        CommandSourceServerUser.of(ctx)
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeGet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getUserManager().getOnline(getPlayer(ctx, "target"));
        CommandSourceServerUser.of(ctx)
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getPreference(RTP_LEFT));

        return user.getPreference(RTP_LEFT);
    }

    private int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getUserManager().getOnline(getPlayer(ctx, "target"));
        int amountToRemove = getInteger(ctx, "amount");

        if ((user.getPreference(RTP_LEFT) - amountToRemove) < 0)
            throw KiloCommands.getException("argument.general.negative_values").create();

        user.getPreferences().set(RTP_LEFT, user.getPreference(RTP_LEFT) - amountToRemove);
        CommandSourceServerUser.of(ctx)
                .sendLangMessage("template.#1", "RTPs left", user.getPreference(RTP_LEFT), user.getDisplayName());

        return user.getPreference(RTP_LEFT);
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Text text = Texter.confirmationMessage(
                "command.rtp.confirm",
                Texter.getButton("&8[&aClick Here to perform&8]", "/rtp perform", Texter.newText("&dConfirm"))
        );
        this.getOnlineUser(ctx).sendMessage(text);
        return SUCCESS;
    }

    private int executePerform(CommandContext<ServerCommandSource> ctx, @Nullable ServerWorld world) throws CommandSyntaxException {
        this.teleport(ctx.getSource(), ctx.getSource().getPlayer(), world == null ? ctx.getSource().getWorld() : world);
        return SUCCESS;
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx, @Nullable ServerWorld world) throws CommandSyntaxException {
        final ServerPlayerEntity player = getPlayer(ctx, "target");
        this.teleport(ctx.getSource(), player, world == null ? player.getWorld() : world);
        return SUCCESS;
    }
}