package org.kilocraft.essentials.extensions.betterchairs;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.events.PlayerEvents;
import org.kilocraft.essentials.user.preference.Preferences;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SeatManager implements ConfigurableFeature, TickListener {
    private static SeatManager INSTANCE;
    private static boolean enabled = false;
    private final List<UUID> stands = new ArrayList<>();

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
        KiloCommands.register(new SitCommand());
        PlayerEvents.STOP_RIDING.register(this::unseat);
        PlayerEvents.DEATH.register(this::unseat);
        PlayerEvents.LEAVE.register(this::unseat);
        PlayerEvents.INTERACT_BLOCK.register((player, world, stack, hand, hitResult) -> onInteractBlock(player, hitResult, hand) ? ActionResult.SUCCESS : ActionResult.PASS);
        return true;
    }

    private boolean hasPermission(@NotNull final ServerPlayerEntity player) {
        return KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.SIT_SELF);
    }

    @Override
    public void onTick() {
        Iterator<UUID> iterator = stands.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            ArmorStandEntity armorStand = getArmorStand(uuid);
            if (armorStand == null) {
                iterator.remove();
                continue;
            }
            if (armorStand.hasPassengerType(entity -> entity instanceof ServerPlayerEntity) && armorStand.getPassengerList().get(0) instanceof ServerPlayerEntity playerEntity) {
                OnlineUser user = KiloEssentials.getUserManager().getOnline(playerEntity);

                if (user != null) {
                    SummonType summonType = user.getPreference(Preferences.SITTING_TYPE);
                    BlockPos pos = summonType == SummonType.COMMAND ? armorStand.getBlockPos().up() : armorStand.getBlockPos().up(2);

                    if (armorStand.getEntityWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                        this.unseat(armorStand);
                    }

                    if (user.getPreference(Preferences.SITTING_TYPE) == SummonType.INTERACT_SLAB || user.getPreference(Preferences.SITTING_TYPE) == SummonType.COMMAND) {
                        armorStand.setYaw(user.asPlayer().getYaw());
                    }
                }
            } else {
                armorStand.kill();
            }
        }
    }

    @Nullable
    private ArmorStandEntity getArmorStand(UUID uuid) {
        for (ServerWorld world : KiloEssentials.getMinecraftServer().getWorlds()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof ArmorStandEntity armorStandEntity) return armorStandEntity;
        }
        return null;
    }

    public boolean onInteractBlock(@NotNull final ServerPlayerEntity player,
                                   @NotNull final BlockHitResult hitResult, @NotNull final Hand hand) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);

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
            float newYaw = getYawForStand(state);
            player.setYaw(newYaw);
            return seat(user, getPosForStair(state, vec3dLoc.center()), SummonType.INTERACT_STAIR, newYaw);
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
        return seat(user, loc, summonType, user.asPlayer().getYaw());
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

        player.swingHand(Hand.MAIN_HAND, true);

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.addScoreboardTag("KE$SitStand#" + user.getUsername());
        stand.addScoreboardTag("KESitStand");
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        user.getPreferences().set(Preferences.SITTING_TYPE, summonType);
        stand.setYaw(yaw);
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        assert loc.getWorld() != null;
        loc.getWorld().spawnEntity(stand);

        player.startRiding(stand, true);
        stands.add(stand.getUuid());

        return true;
    }

    public void unseat(@NotNull final ServerPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof ArmorStandEntity armorStand) {
            unseat(armorStand);
        }
    }

    private void unseat(ArmorStandEntity armorStandEntity) {
        if (armorStandEntity.getScoreboardTags().contains("KESitStand")) {
            Entity passenger = armorStandEntity.getFirstPassenger();
            if (passenger instanceof PlayerEntity playerEntity) {
                passenger.stopRiding();
                armorStandEntity.kill();
                playerEntity.sendMessage(StringText.of(true, "sit.stop_riding"), true);
            }
        }
    }

    public void killAll() {
        for (UUID uuid : stands) {
            ArmorStandEntity armorStand = getArmorStand(uuid);
            if (armorStand != null) {
                armorStand.kill();
            }
        }
        stands.clear();

    }

    public boolean isSitting(@NotNull final ServerPlayerEntity player) {
        if (!player.hasVehicle() || !(player.getVehicle() instanceof ArmorStandEntity stand)) {
            return false;
        }

        return stand.hasPlayerRider() && stand.getScoreboardTags().contains("KE$SitStand#" + player.getEntityName());
    }

    private Vec3dLocation getPosForStair(@NotNull final BlockState state, @NotNull final Vec3dLocation loc) {
        double offset = 0.205D;
        Direction direction = state.get(Properties.HORIZONTAL_FACING);
        StairShape shape = state.get(Properties.STAIR_SHAPE);
        switch (shape) {
            case OUTER_LEFT, INNER_LEFT -> {
                switch (direction) {
                    case NORTH -> loc.setX(loc.getX() + offset);
                    case WEST -> loc.setZ(loc.getZ() - offset);
                    case SOUTH -> loc.setX(loc.getX() - offset);
                    case EAST -> loc.setZ(loc.getZ() + offset);
                }
            }
            case OUTER_RIGHT, INNER_RIGHT -> {
                switch (direction) {
                    case NORTH -> loc.setX(loc.getX() - offset);
                    case WEST -> loc.setZ(loc.getZ() + offset);
                    case SOUTH -> loc.setX(loc.getX() + offset);
                    case EAST -> loc.setZ(loc.getZ() - offset);
                }
            }
        }
        switch (direction) {
            case NORTH -> loc.setZ(loc.getZ() + offset);
            case WEST -> loc.setX(loc.getX() + offset);
            case SOUTH -> loc.setZ(loc.getZ() - offset);
            case EAST -> loc.setX(loc.getX() - offset);
        }

        return loc;
    }

    private float getYawForStand(@NotNull final BlockState state) {
        Direction direction = state.get(Properties.HORIZONTAL_FACING).getOpposite();
        float yaw;
        switch (direction) {
            case NORTH -> yaw = 180.0f;
            case WEST -> yaw = 90.0f;
            case EAST -> yaw = -90.0f;
            default -> yaw = 0.0f;
        }
        StairShape shape = state.get(Properties.STAIR_SHAPE);
        switch (shape) {
            case OUTER_LEFT, INNER_LEFT -> yaw -= 45;
            case OUTER_RIGHT, INNER_RIGHT -> yaw += 45;
        }
        return yaw;
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
