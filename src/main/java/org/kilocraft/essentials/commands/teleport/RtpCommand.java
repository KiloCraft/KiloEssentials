package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.settting.Setting;
import org.kilocraft.essentials.api.world.location.Location;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.chat.TextMessage;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.config.main.sections.RtpSpecsConfigSection;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.LocationUtil;
import org.kilocraft.essentials.util.SimpleProcess;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.registry.RegistryUtils;
import org.kilocraft.essentials.util.text.Texter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;

public class RtpCommand extends EssentialCommand {
    private static final SimpleProcess<Void> PROCESS = new SimpleProcess<>("rtp_process");
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_SELF);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_IGNORE_LIMIT = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_BYPASS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER_DIMENSIONS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERDIMENSIONS);
    private static final Predicate<ServerCommandSource> PERMISSION_CHECK_MANAGE = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_MANAGE);
    private static final Setting<Integer> RTP_LEFT = Settings.RANDOM_TELEPORTS_LEFT;
    private static final String ACTION_MSG = ModConstants.translation("command.rtp.round_try");

    public RtpCommand() {
        super("rtp", PERMISSION_CHECK_SELF, new String[]{"wilderness", "wild"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> addArgument = literal("add")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentCompletions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeAdd)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> setArgument = literal("set")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentCompletions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeSet)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> removeArgument = literal("remove")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentCompletions::allPlayers).then(
                                argument("amount", IntegerArgumentType.integer(1))
                                        .executes(this::executeRemove)
                        )
                );

        LiteralArgumentBuilder<ServerCommandSource> sendArgument = literal("send")
                .requires(PERMISSION_CHECK_MANAGE)
                .then(argument("target", EntityArgumentType.player())
                        .suggests(ArgumentCompletions::allPlayers)
                        .executes(this::executeOthers)
                );

        LiteralArgumentBuilder<ServerCommandSource> checkArgument = literal("check")
                .executes(this::executeLeft)
                .then(argument("target", EntityArgumentType.player())
                        .requires(PERMISSION_CHECK_OTHERS)
                        .suggests(ArgumentCompletions::allPlayers)
                        .executes(this::executeGet)
                );

        LiteralArgumentBuilder<ServerCommandSource> performArgument = literal("perform")
                .executes(this::executePerform);

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
        OnlineUser user = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getSetting(RTP_LEFT));

        return user.getSetting(RTP_LEFT);
    }

    private int executeAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToAdd = getInteger(ctx, "amount");
        user.getSettings().set(RTP_LEFT, user.getSetting(RTP_LEFT) + amountToAdd);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getSetting(RTP_LEFT), user.getDisplayName());

        return user.getSetting(RTP_LEFT);
    }

    private int executeSet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToSet = getInteger(ctx, "amount");
        user.getSettings().set(RTP_LEFT, amountToSet);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getSetting(RTP_LEFT), user.getDisplayName());

        return user.getSetting(RTP_LEFT);
    }

    private int executeGet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("command.rtp.get", user.getDisplayName(), user.getSetting(RTP_LEFT));

        return user.getSetting(RTP_LEFT);
    }

    private int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
        int amountToRemove = getInteger(ctx, "amount");

        if ((user.getSetting(RTP_LEFT) - amountToRemove) < 0)
            throw KiloCommands.getArgException(ArgExceptionMessageNode.NO_NEGATIVE_VALUES).create();

        user.getSettings().set(RTP_LEFT, user.getSetting(RTP_LEFT) - amountToRemove);
        KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
                .sendLangMessage("template.#1", "RTPs left", user.getSetting(RTP_LEFT), user.getDisplayName());

        return user.getSetting(RTP_LEFT);
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Text text = Texter.confirmationMessage(
                "command.rtp.confirm",
                Texter.getButton("&8[&aClick Here to perform&8]", "/rtp perform", Texter.newText("&dConfirm"))
        );
        this.getOnlineUser(ctx).sendMessage(text);
        return SUCCESS;
    }

    private int executePerform(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);

        if (UserUtils.Process.isIn(user, PROCESS.getId())) {
            user.sendLangError("command.rtp.in_process");
            return FAILED;
        }

        UserUtils.Process.add(user, PROCESS);
        return execute(ctx.getSource(), ctx.getSource().getPlayer());
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser target = this.getOnlineUser(ctx);

        if (UserUtils.Process.isIn(target, PROCESS.getId())) {
            this.getServerUser(ctx).sendLangError("command.rtp.in_process");
            return FAILED;
        }

        UserUtils.Process.add(target, PROCESS);
        return execute(ctx.getSource(), target.asPlayer());
    }

    private int execute(ServerCommandSource source, ServerPlayerEntity target) {
        KiloServer.getServer().getOnlineUser(target).sendMessage(messages.commands().rtp().start);

        RandomTeleportThread rtp = new RandomTeleportThread(source, target);
        Thread rtpThread = new Thread(rtp, "RTP thread");
        rtpThread.start();
        return SUCCESS;
    }

    static void teleport(ServerCommandSource src, ServerPlayerEntity target, Logger logger) {
        OnlineUser targetUser = KiloServer.getServer().getOnlineUser(target.getUuid());
        CommandSourceUser sourceUser = KiloEssentials.getServer().getCommandSourceUser(src);

        if (targetUser.getSetting(RTP_LEFT) < 0) {
            targetUser.getSettings().set(RTP_LEFT, 0);
        }

        //Check if the player has any rtps left or permission to ignore the limit
        if (CommandUtils.areTheSame(src, target) && targetUser.getSetting(RTP_LEFT) <= 0 && !PERMISSION_CHECK_IGNORE_LIMIT.test(src)) {
            targetUser.sendMessage(KiloConfig.messages().commands().rtp().empty);
            return;
        }

        //Check if the target is in the correct dimension or has permission to perform the command in other dimensions
        if (RegistryUtils.dimensionTypeToRegistryKey(target.getServerWorld().getDimension()) != World.OVERWORLD && !PERMISSION_CHECK_OTHER_DIMENSIONS.test(src)) {
            targetUser.sendMessage(KiloConfig.messages().commands().rtp().dimensionException);
            return;
        }

        StopWatch watch = new StopWatch();
        watch.start();

        RtpSpecsConfigSection cfg = KiloConfig.main().rtpSpecs();

        if (!cfg.broadcastMessage.isEmpty()) {
            KiloChat.broadCast(new TextMessage(String.format(cfg.broadcastMessage, targetUser.getFormattedDisplayName())));
        }


        ServerWorld world = target.getServerWorld();
        Vec3dLocation loc;
        BlockPos pos;
        BlockState state;
        int tries = 0;
        boolean hasAirSpace;
        boolean isNether = target.getServerWorld().getDimension().isNether();
        boolean safe;

        do {
            tries++;
            loc = (Vec3dLocation) randomLocation(world, isNether ? 90 : world.getHeight(), cfg.minX, cfg.maxX, cfg.minZ, cfg.maxZ);
            loc = (Vec3dLocation) LocationUtil.posOnGround(loc, false);
            pos = loc.toPos();
            state = world.getBlockState(pos);
            Material material = state.getMaterial();
            Biome.Category category = world.getBiome(pos).getCategory();

            if (!LocationUtil.hasSolidGround(loc)) {
                safe = false;
                continue;
            }

            hasAirSpace = !isNether || world.getBlockState(pos.up()).isAir();
            safe = hasAirSpace && !material.isLiquid() && material != Material.FIRE && category != Category.OCEAN && category != Category.RIVER && !LocationUtil.isBlockLiquid(loc.down());

            if (cfg.showTries && target.networkHandler != null) {
                int finalTries = tries;
                KiloServer.getServer().getMinecraftServer().execute(() -> {
                    target.networkHandler.sendPacket(
                            new TitleS2CPacket(
                                    TitleS2CPacket.Action.ACTIONBAR,
                                    Texter.newText(String.format(ACTION_MSG, finalTries, cfg.maxTries))
                            )
                    );
                });
            }
        } while (tries <= cfg.maxTries && !safe);

        watch.stop();
        String timeElapsed = ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)) + "ms";

        if (!safe) {
            sourceUser.sendLangError("command.rtp.failed");
        } else {
            targetUser.saveLocation();
            loc.setY(loc.getY() + 3);
            targetUser.teleport(loc.center(), true);

            String biome = LocateBiomeProvided.getBiomeName(target.getServerWorld().getBiome(target.getBlockPos()));

            if (!PERMISSION_CHECK_IGNORE_LIMIT.test(src)) {
                targetUser.getSettings().set(RTP_LEFT, targetUser.getSetting(RTP_LEFT) - 1);
            }

            String cfgMessage = KiloConfig.messages().commands().rtp().teleported
                    .replace("{RTP_LEFT}", String.valueOf(targetUser.getSetting(RTP_LEFT)))
                    .replace("{cord.X}", String.valueOf(loc.getX()))
                    .replace("{cord.Y}", String.valueOf(target.getBlockPos().getY()))
                    .replace("{cord.Z}", String.valueOf(loc.getZ()))
                    .replace("{ELAPSED_TIME}", timeElapsed);

            TranslatableText translatable = (TranslatableText) target.getServerWorld().getBiome(target.getBlockPos()).getName();
            Text text = new LiteralText("")
                    .append(new LiteralText("You've been teleported to this ").formatted(Formatting.YELLOW))
                    .append(translatable.formatted(Formatting.GOLD))
                    .append(new LiteralText(" biome!").formatted(Formatting.YELLOW))
                    .append("\n").append(Texter.newText(cfgMessage));

            targetUser.sendMessage(text);
            if (!sourceUser.equals(targetUser)) {
                sourceUser.sendLangMessage("command.rtp.others", targetUser.getUsername(), biome);
            }

            logger.info("Finished RTP For " + targetUser.getUsername() + " in " + timeElapsed);
        }

        UserUtils.Process.remove(targetUser);
        Thread.currentThread().interrupt();
    }

    @NotNull
    private static Location randomLocation(World world, int height, int minX, int maxX, int minZ, int maxZ) {
        int randX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        int randZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        return Vec3dLocation.of(randX, height, randZ, 0, 0, RegistryUtils.toIdentifier(world.getDimension()));
    }
}

class RandomTeleportThread implements Runnable {
    private final Logger logger = LogManager.getLogger();
    private final ServerCommandSource source;
    private final ServerPlayerEntity target;

    public RandomTeleportThread(ServerCommandSource source, ServerPlayerEntity target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void run() {
        logger.info("Randomly teleporting " + target.getEntityName() + ". executed by " + source.getName());
        try {
            RtpCommand.teleport(this.source, this.target, logger);
        } catch (Exception e) {
            KiloEssentials.getLogger().error("RTP for {} failed.", target.getEntityName(), e);
        }
    }
}