package org.kilocraft.essentials.extensions.betterchairs;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.TickListener;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.registry.RegistryUtils;

import java.util.HashMap;
import java.util.UUID;

public class SeatManager implements ConfigurableFeature, TickListener {
    private static SeatManager INSTANCE;
    private static boolean enabled = false;
    private HashMap<Identifier, UUID> stands = new HashMap<>();

    @Override
    public boolean register() {
        INSTANCE = this;
        enabled = true;
        KiloEssentials.getInstance().getCommandHandler().register(new SitCommand());

        return true;
    }

    public static SeatManager getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Its either too soon to access the seat manager or the feature is disabled");
        }

        return INSTANCE;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private boolean hasPermission(ServerPlayerEntity player) {
        return KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.SIT_SELF);
    }

    private static int tick = 0;
    @Override
    public void onTick() {
        tick++;

        if (tick != 10) {
            return;
        }

        tick = 0;
        stands.forEach((dim, uuid) -> {
            ArmorStandEntity stand = (ArmorStandEntity) KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dim)).getEntity(uuid);
            if (stand == null) {
                return;
            }

            if (stand.hasPlayerRider() && stand.hasPassengerType(PlayerEntity.class) && stand.getPassengerList().get(0) instanceof PlayerEntity) {
                OnlineUser user = KiloServer.getServer().getOnlineUser((ServerPlayerEntity) stand.getPassengerList().get(0));
                ServerWorld world = KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dim));

                if (user != null) {
                    if (user.getSetting(Settings.SITTING_TYPE) != SummonType.COMMAND && world.getBlockState(stand.getBlockPos().up().up()).getBlock() == Blocks.AIR) {
                        unseat(user);
                    }

                    if (user.getSetting(Settings.SITTING_TYPE) == SummonType.INTERACTION_SLAB) {
                        stand.bodyYaw = user.asPlayer().bodyYaw;
                        stand.yaw = user.asPlayer().bodyYaw;
                    }
                }
            }

            if (!stand.hasPlayerRider()) {
                stand.kill();
            }
        });
    }

    public boolean onInteractBlock(ServerPlayerEntity player, BlockHitResult hitResult, Hand hand) {
        OnlineUser user = KiloServer.getServer().getOnlineUser(player);

        if (
                user == null ||
                hand != Hand.MAIN_HAND ||
                !player.getMainHandStack().equals(ItemStack.EMPTY) ||
                player.getVehicle() != null ||
                !hasPermission(player) ||
                player.shouldCancelInteraction() ||
                hitResult.getSide() == Direction.DOWN ||
                !user.getSetting(Settings.CAN_SEAT)
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
                player.yaw, player.pitch, RegistryUtils.toIdentifier(world.dimension.getType()));

        if (state.getBlock() instanceof StairsBlock && state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.40D);
            return seat(user, getPosForStair(state, vec3dLoc.center()), SummonType.INTERACTION_STAIR, true, getYawForStand(state));
        }

        if (state.getBlock() instanceof SlabBlock && state.get(Properties.SLAB_TYPE) == SlabType.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.45D);
            return seat(user, vec3dLoc.center(), SummonType.INTERACTION_SLAB, true);
        }

        return false;
    }

    public boolean seat(OnlineUser user, Vec3dLocation loc, SummonType summonType, boolean swingHand) {
        return seat(user, loc, summonType, swingHand, user.asPlayer().bodyYaw);
    }

    public boolean seat(OnlineUser user, Vec3dLocation loc, SummonType summonType, boolean swingHand, float yaw) {
        ServerPlayerEntity player = user.asPlayer();

        if (player.isSpectator() || !user.getSetting(Settings.CAN_SEAT) || isSeating(player)) {
            return false;
        }

        ArmorStandEntity stand = EntityType.ARMOR_STAND.create(
                loc.getWorld(), null,
                new LiteralText("KE$SitStand#" + stands.size() + user.getUsername()), null, loc.toPos(),
                SpawnType.TRIGGERED, true, true
        );

        if (stand == null) {
            return false;
        }

        if (swingHand) {
            player.swingHand(Hand.MAIN_HAND, true);
        }

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.addScoreboardTag("KE$SitStand#" + user.getUsername());
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        user.getSettings().set(Settings.SITTING_TYPE, summonType);
        stand.bodyYaw = yaw;
        stand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        loc.getWorld().spawnEntity(stand);

        player.startRiding(stand, true);
        stands.put(loc.getDimension(), stand.getUuid());

        return true;
    }

    public void unseat(OnlineUser user) {
        if (user == null) {
            return;
        }

        ServerPlayerEntity player = user.asPlayer();

        if (player == null || !player.hasVehicle() || !(player.getVehicle() instanceof ArmorStandEntity)) {
            return;
        }

        ArmorStandEntity stand = (ArmorStandEntity) player.getVehicle();
        if (
                stand != null &&
                stand.getScoreboardTags().contains("KE$SitStand#" + user.getUsername())
        ) {
            player.sendMessage(LangText.get(true, "sit.stop_riding"), true);
            stands.remove(RegistryUtils.toIdentifier(stand.dimension), stand.getUuid());
            stand.kill();
        }

    }

    public void killAll() {
        stands.forEach((dim, uuid) -> {
            ArmorStandEntity armorStand = (ArmorStandEntity) KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dim)).getEntity(uuid);
            if (armorStand != null && !armorStand.hasPlayerRider()) {
                armorStand.kill();
            }
        });
    }

    public boolean isSeating(ServerPlayerEntity player) {
        if (!player.hasVehicle() || !(player.getVehicle() instanceof ArmorStandEntity)) {
            return false;
        }

        ArmorStandEntity stand = (ArmorStandEntity) player.getVehicle();
        return stand != null && stand.hasPlayerRider() && stand.getScoreboardTags().contains("KE$SitStand#" + player.getEntityName());
    }

    private Vec3dLocation getPosForStair(final BlockState state, final Vec3dLocation loc) {
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

    private float getYawForStand(final BlockState state) {
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
        INTERACTION_STAIR,
        INTERACTION_SLAB,
        NONE;

        @Nullable
        public static SummonType getByName(String name) {
            for (SummonType value : values()) {
                if (value.toString().equalsIgnoreCase(name)) {
                    return value;
                }
            }

            return null;
        }
    }

}
