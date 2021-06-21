package org.kilocraft.essentials.extensions.betterchairs;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.player.UserUtils;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeatManager implements ConfigurableFeature, TickListener {
    private static SeatManager INSTANCE;
    private static boolean enabled = false;
    private static int tick = 0;
    private final HashMap<ServerWorld, UUID> stands = new HashMap<>();

    public static SeatManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Its either too soon to access the seat manager or the feature is disabled");
        }

        return INSTANCE;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean register() {
        INSTANCE = this;
        enabled = true;
        KiloCommands.getInstance().register(new SitCommand());

        return true;
    }

    private boolean hasPermission(@NotNull final ServerPlayerEntity player) {
        return KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.SIT_SELF);
    }

    @Override
    public void onTick() {
        if (tick % 10 != 0) return;
        for (Map.Entry<ServerWorld, UUID> entry : stands.entrySet()) {
            ServerWorld world = entry.getKey();
            UUID uuid = entry.getValue();
            ArmorStandEntity stand = (ArmorStandEntity) world.getEntity(uuid);
            if (stand == null) {
                continue;
            }
            if (stand.hasPlayerRider() && stand.hasPassengerType(entity -> entity instanceof PlayerEntity) && stand.getPassengerList().get(0) instanceof PlayerEntity) {
                OnlineUser user = KiloServer.getServer().getOnlineUser((ServerPlayerEntity) stand.getPassengerList().get(0));

                if (user != null) {
                    SummonType summonType = user.getPreference(Preferences.SITTING_TYPE);
                    BlockPos pos = summonType == SummonType.COMMAND ? stand.getBlockPos().up() : stand.getBlockPos().up(2);

                    if (world.getBlockState(pos).getBlock() == Blocks.AIR) {
                        this.unseat(user);
                    }

                    if (user.getPreference(Preferences.SITTING_TYPE) == SummonType.INTERACT_SLAB) {
                        stand.bodyYaw = user.asPlayer().bodyYaw;
                        stand.setYaw(user.asPlayer().bodyYaw);
                    }
                }
            } else {
                stand.kill();
            }
        }
        tick++;
    }

    public boolean onInteractBlock(@NotNull final ServerPlayerEntity player,
                                   @NotNull final BlockHitResult hitResult, @NotNull final Hand hand) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (
                user == null ||
                        hand != Hand.MAIN_HAND ||
                        !player.getMainHandStack().equals(ItemStack.EMPTY) ||
                        player.getVehicle() != null ||
                        !hasPermission(player) ||
                        player.shouldCancelInteraction() ||
                        hitResult.getSide() == Direction.DOWN ||
                        !user.getPreference(Preferences.CAN_SEAT)
        ) {
            return false;
        }

        BlockPos pos = hitResult.getBlockPos();

        if (player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5, pos.getZ() + 0.5) > 3.85D) {
            return false;
        }

        ServerWorld world = player.getServerWorld();
        BlockState state = world.getBlockState(pos);

        if (world.getBlockState(pos.up()).getBlock() != Blocks.AIR) {
            return false;
        }

        Vec3dLocation vec3dLoc = Vec3dLocation.of(pos.getX(), pos.getY() + 1, pos.getZ(),
                player.getYaw(), player.getPitch(), RegistryUtils.toIdentifier(world.getDimension()));

        if (state.getBlock() instanceof StairsBlock && state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.40D);
            return seat(user, getPosForStair(state, vec3dLoc.center()), SummonType.INTERACT_STAIR, getYawForStand(state));
        }

        if (state.getBlock() instanceof SlabBlock && state.get(Properties.SLAB_TYPE) == SlabType.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.45D);
            return seat(user, vec3dLoc.center(), SummonType.INTERACT_SLAB);
        }

        return false;
    }

    public boolean seat(@NotNull final OnlineUser user,
                        @NotNull final Vec3dLocation loc,
                        @NotNull final SummonType summonType) {
        return seat(user, loc, summonType, user.asPlayer().bodyYaw);
    }

    public boolean seat(@NotNull final OnlineUser user,
                        @NotNull final Vec3dLocation loc,
                        @NotNull final SummonType summonType, float yaw) {
        ServerPlayerEntity player = user.asPlayer();

        if (player.isSpectator() || isSitting(player)) {
            return false;
        }

        ArmorStandEntity stand = EntityType.ARMOR_STAND.create(
                loc.getWorld(), null,
                new LiteralText("KE$SitStand#" + stands.size() + user.getUsername()), null, loc.toPos(),
                SpawnReason.TRIGGERED, true, true
        );

        if (stand == null) {
            return false;
        }

        UserUtils.Animate.swingHand(player);

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.addScoreboardTag("KE$SitStand#" + user.getUsername());
        stand.addScoreboardTag("KE$SitStand");
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        user.getPreferences().set(Preferences.SITTING_TYPE, summonType);
        stand.bodyYaw = yaw;
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        assert loc.getWorld() != null;
        loc.getWorld().spawnEntity(stand);

        player.startRiding(stand, true);
        stands.put(loc.getWorld(), stand.getUuid());

        return true;
    }

    public void unseat(@NotNull final OnlineUser user) {
        if (user == null) {
            KiloEssentials.getLogger().error("Seatmanager: OnlineUser is null");
            return;
        }
        if (user.asPlayer() == null) {
            KiloEssentials.getLogger().error("Seatmanager: OnlineUser.asPlayer() is null");
            return;
        }
        ServerPlayerEntity player = user.asPlayer();

        if (player == null) {
            return;
        }

        ArmorStandEntity stand = null;
        for (Map.Entry<ServerWorld, UUID> entry : stands.entrySet()) {
            ArmorStandEntity armorStand = (ArmorStandEntity) entry.getKey().getEntity(entry.getValue());
            if (armorStand != null && armorStand.getScoreboardTags().contains("KE$SitStand#" + user.getUsername())) {
                stand = armorStand;
                break;
            }
        }
        if (stand != null) {
            player.sendMessage(StringText.of(true, "sit.stop_riding"), true);
            stands.remove(RegistryUtils.toServerWorld(stand.getEntityWorld().getDimension()), stand.getUuid());
            stand.kill();
        }
    }

    public void killAll() {
        int i = 0;
        for (Map.Entry<ServerWorld, UUID> entry : stands.entrySet()) {
            ArmorStandEntity armorStand = (ArmorStandEntity) entry.getKey().getEntity(entry.getValue());
            if (armorStand != null) {
                i++;
                armorStand.kill();
            }
        }
        KiloEssentials.getLogger().info("Killed " + i + " armorstands!");
        i = 0;
        for (ServerWorld world : KiloEssentials.getServer().getMinecraftServer().getWorlds()) {
            for (Entity entity : world.iterateEntities()) {
                if (!(entity instanceof ArmorStandEntity)) continue;
                if (entity.getScoreboardTags().contains("KE$SitStand")) {
                    i++;
                    entity.kill();
                }
            }
        }
        if (i > 0) KiloEssentials.getLogger().info("Killed " + i + " leftover armorstands!");

    }

    public boolean isSitting(@NotNull final ServerPlayerEntity player) {
        if (!player.hasVehicle() || !(player.getVehicle() instanceof ArmorStandEntity)) {
            return false;
        }

        ArmorStandEntity stand = (ArmorStandEntity) player.getVehicle();
        return stand != null && stand.hasPlayerRider() && stand.getScoreboardTags().contains("KE$SitStand#" + player.getEntityName());
    }

    private Vec3dLocation getPosForStair(@NotNull final BlockState state, @NotNull final Vec3dLocation loc) {
        Direction direction = state.get(Properties.HORIZONTAL_FACING);
        double offset = 0.205D;

        if (direction == Direction.NORTH) {
            loc.setZ(loc.getZ() + offset);
        }

        if (direction == Direction.WEST) {
            loc.setX(loc.getX() + offset);
        }

        if (direction == Direction.SOUTH) {
            loc.setZ(loc.getZ() - offset);
        }

        if (direction == Direction.EAST) {
            loc.setX(loc.getX() - offset);
        }

        return loc;
    }

    private float getYawForStand(@NotNull final BlockState state) {
        Direction direction = state.get(Properties.HORIZONTAL_FACING).getOpposite();

        if (direction == Direction.NORTH) {
            return 180.0F;
        }

        if (direction == Direction.WEST) {
            return 90.0F;
        }

        if (direction == Direction.EAST) {
            return -90.0F;
        }

        return 0.0F;
    }

    public enum SummonType {
        COMMAND,
        INTERACT_STAIR,
        INTERACT_SLAB,
        NONE;

        @NotNull
        public static SummonType getByName(String name) {
            for (SummonType value : values()) {
                if (value.toString().equalsIgnoreCase(name)) {
                    return value;
                }
            }

            return NONE;
        }
    }

}
