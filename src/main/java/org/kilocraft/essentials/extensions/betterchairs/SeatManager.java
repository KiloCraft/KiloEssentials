package org.kilocraft.essentials.extensions.betterchairs;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
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
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> this.unseat(handler.getPlayer()));
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> this.onInteractBlock((ServerPlayer) player, hitResult, hand) ? InteractionResult.SUCCESS : InteractionResult.PASS);
        return true;
    }

    @Override
    public void onTick() {
        Iterator<UUID> iterator = this.stands.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            ArmorStand armorStand = this.getArmorStand(uuid);
            if (armorStand == null) {
                iterator.remove();
                continue;
            }
            if (armorStand.hasPassenger(entity -> entity instanceof ServerPlayer) && armorStand.getPassengers().get(0) instanceof ServerPlayer playerEntity) {
                OnlineUser user = KiloEssentials.getUserManager().getOnline(playerEntity);

                if (user != null) {
                    SummonType summonType = user.getPreference(Preferences.SITTING_TYPE);
                    BlockPos pos = summonType == SummonType.COMMAND ? armorStand.blockPosition().above() : armorStand.blockPosition().above(2);

                    if (armorStand.getCommandSenderWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                        this.unseat(armorStand);
                    }

                    if (user.getPreference(Preferences.SITTING_TYPE) == SummonType.INTERACT_SLAB || user.getPreference(Preferences.SITTING_TYPE) == SummonType.COMMAND) {
                        armorStand.setYRot(user.asPlayer().getYRot());
                    }
                }
            } else {
                armorStand.kill();
            }
        }
    }

    @Nullable
    private ArmorStand getArmorStand(UUID uuid) {
        for (ServerLevel world : KiloEssentials.getMinecraftServer().getAllLevels()) {
            Entity entity = world.getEntity(uuid);
            if (entity instanceof ArmorStand armorStandEntity) return armorStandEntity;
        }
        return null;
    }

    public boolean onInteractBlock(@NotNull final ServerPlayer player,
                                   @NotNull final BlockHitResult hitResult, @NotNull final InteractionHand hand) {
        OnlineUser user = KiloEssentials.getUserManager().getOnline(player);

        if (
                user == null ||
                        hand != InteractionHand.MAIN_HAND ||
                        !player.getMainHandItem().equals(ItemStack.EMPTY) ||
                        player.getVehicle() != null ||
                        !KiloEssentials.hasPermissionNode(player.createCommandSourceStack(), EssentialPermission.SIT_SELF) ||
                        player.isSecondaryUseActive() ||
                        hitResult.getDirection() == Direction.DOWN ||
                        !user.getPreference(Preferences.CAN_SEAT)
        ) {
            return false;
        }

        BlockPos pos = hitResult.getBlockPos();

        if (player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5, pos.getZ() + 0.5) > 3.85D) {
            return false;
        }

        ServerLevel world = player.getLevel();
        BlockState state = world.getBlockState(pos);

        if (world.getBlockState(pos.above()).getBlock() != Blocks.AIR) {
            return false;
        }

        Vec3dLocation vec3dLoc = Vec3dLocation.of(pos.getX(), pos.getY() + 1, pos.getZ(),
                player.getYRot(), player.getXRot(), RegistryUtils.toIdentifier(world.dimensionType()));

        if (state.getBlock() instanceof StairBlock && state.getValue(BlockStateProperties.HALF) == Half.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.40D);
            float newYaw = this.getYawForStand(state);
            player.setYRot(newYaw);
            return this.seat(user, this.getPosForStair(state, vec3dLoc.center()), SummonType.INTERACT_STAIR, newYaw);
        }

        if (state.getBlock() instanceof SlabBlock && state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.45D);
            return this.seat(user, vec3dLoc.center(), SummonType.INTERACT_SLAB);
        }

        return false;
    }

    public boolean seat(@NotNull final OnlineUser user,
                        @NotNull final Vec3dLocation loc,
                        @NotNull final SummonType summonType) {
        return this.seat(user, loc, summonType, user.asPlayer().getYRot());
    }

    public boolean seat(@NotNull final OnlineUser user,
                        @NotNull final Vec3dLocation loc,
                        @NotNull final SummonType summonType, float yaw) {
        ServerPlayer player = user.asPlayer();

        if (player.isSpectator() || this.isSitting(player)) {
            return false;
        }

        ArmorStand stand = EntityType.ARMOR_STAND.create(
                loc.getWorld(), null,
                new TextComponent("KE$SitStand#" + this.stands.size() + user.getUsername()), null, loc.toPos(),
                MobSpawnType.TRIGGERED, true, true
        );

        if (stand == null) {
            return false;
        }

        player.swing(InteractionHand.MAIN_HAND, true);

        stand.setInvisible(true);
        stand.setNoGravity(true);
        stand.setInvulnerable(true);
        stand.addTag("KE$SitStand#" + user.getUsername());
        stand.addTag("KESitStand");
        stand.absMoveTo(loc.getX(), loc.getY() - 1.75, loc.getZ());
        user.getPreferences().set(Preferences.SITTING_TYPE, summonType);
        stand.setYRot(yaw);
        stand.absMoveTo(loc.getX(), loc.getY() - 1.75, loc.getZ());
        assert loc.getWorld() != null;
        loc.getWorld().addFreshEntity(stand);

        player.startRiding(stand, true);
        this.stands.add(stand.getUUID());

        return true;
    }

    public void unseat(@NotNull final ServerPlayer player) {
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof ArmorStand armorStand) {
            this.unseat(armorStand);
        }
    }

    private void unseat(ArmorStand armorStandEntity) {
        if (armorStandEntity.getTags().contains("KESitStand")) {
            Entity passenger = armorStandEntity.getFirstPassenger();
            if (passenger instanceof Player playerEntity) {
                passenger.stopRiding();
                armorStandEntity.kill();
                playerEntity.displayClientMessage(StringText.of("sit.stop_riding"), true);
            }
        }
    }

    public void killAll() {
        for (UUID uuid : this.stands) {
            ArmorStand armorStand = this.getArmorStand(uuid);
            if (armorStand != null) {
                armorStand.kill();
            }
        }
        this.stands.clear();

    }

    public boolean isSitting(@NotNull final ServerPlayer player) {
        if (!player.isPassenger() || !(player.getVehicle() instanceof ArmorStand stand)) {
            return false;
        }

        return stand.hasExactlyOnePlayerPassenger() && stand.getTags().contains("KE$SitStand#" + player.getScoreboardName());
    }

    private Vec3dLocation getPosForStair(@NotNull final BlockState state, @NotNull final Vec3dLocation loc) {
        double offset = 0.205D;
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        StairsShape shape = state.getValue(BlockStateProperties.STAIRS_SHAPE);
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
        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        float yaw;
        switch (direction) {
            case NORTH -> yaw = 180.0f;
            case WEST -> yaw = 90.0f;
            case EAST -> yaw = -90.0f;
            default -> yaw = 0.0f;
        }
        StairsShape shape = state.getValue(BlockStateProperties.STAIRS_SHAPE);
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
