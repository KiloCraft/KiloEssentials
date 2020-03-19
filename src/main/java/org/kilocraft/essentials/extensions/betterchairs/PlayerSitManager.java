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
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.RegistryUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSitManager implements ConfigurableFeature {
    public static PlayerSitManager INSTANCE;
    public static boolean enabled = false;
    private static Map<Identifier, UUID> sitStands;

    @Override
    public boolean register() {
        INSTANCE = this;
        enabled = true;
        KiloEssentials.getInstance().getCommandHandler().register(new SitCommand());
        sitStands = new HashMap<>();
        return true;
    }


    public PlayerSitManager() {
    }

    private boolean hasPermission(ServerPlayerEntity player) {
        return KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.SIT_SELF);
    }

    public boolean onInteractBlock(ServerPlayerEntity player, BlockHitResult hitResult, Hand hand) {
        if (hand != Hand.MAIN_HAND || !player.getMainHandStack().equals(ItemStack.EMPTY) ||
                player.getVehicle() != null || !hasPermission(player) || player.isSneaking())
            return false;

        if (hitResult.getSide() == Direction.DOWN)
            return false;

        if (!KiloServer.getServer().getOnlineUser(player).canSit())
            return false;

        BlockPos targetBlock = hitResult.getBlockPos();
        ServerWorld world = player.getServerWorld();
        BlockState blockState = world.getBlockState(targetBlock);

        Vec3dLocation vec3dLoc = Vec3dLocation.of(targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ(),
                player.yaw, player.pitch, RegistryUtils.toIdentifier(world.dimension.getType()));

        if (blockState.getBlock() instanceof StairsBlock && blockState.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.40D);
            return sitOn(player, getPosForStair(blockState, vec3dLoc.center()), SummonType.INTERACT_BLOCK, true);
        } else if (blockState.getBlock() instanceof SlabBlock && blockState.get(Properties.SLAB_TYPE) == SlabType.BOTTOM) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.45D);
            return sitOn(player, vec3dLoc.center(), SummonType.INTERACT_BLOCK, true);
        }

        return false;
    }

    public boolean isSitting(ServerPlayerEntity player) {
        if (!player.hasVehicle())
            return false;

        ArmorStandEntity armorStand = (ArmorStandEntity) player.getVehicle();
        return armorStand != null && armorStand.hasPlayerRider() && armorStand.getCustomName() != null && armorStand.getCustomName().asString().startsWith("KE$SitStand#");
    }

    public boolean sitOn(ServerPlayerEntity player, Vec3dLocation loc, SummonType type, boolean swingHand) {
        if (player.isSpectator() || !KiloServer.getServer().getOnlineUser(player).canSit())
            return false;

        ArmorStandEntity armorStand = EntityType.ARMOR_STAND.create(loc.getWorld(), null,
                new LiteralText("KE$SitStand#" + sitStands.size() + "$" + player.getEntityName()), null,
                loc.toPos(), SpawnType.TRIGGERED, true, true);

        if (armorStand == null)
            return false;

        if (swingHand)
            player.swingHand(Hand.MAIN_HAND, true);

        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        armorStand.addScoreboardTag("KE$SitStand@" + player.getUuid().toString());
        armorStand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());
        KiloServer.getServer().getOnlineUser(player).setSittingType(type);

        loc.getWorld().spawnEntity(armorStand);
        player.startRiding(armorStand, true);
        sitStands.put(RegistryUtils.toIdentifier(player.dimension), armorStand.getUuid());

        return true;
    }

    public void sitOff(ServerPlayerEntity player) {
        if (player == null || !player.hasVehicle() || !(player.getVehicle() instanceof ArmorStandEntity))
            return;

        ArmorStandEntity armorStand = (ArmorStandEntity) player.getVehicle();
        if (armorStand != null && armorStand.hasPlayerRider() && armorStand.getCustomName() != null &&
                armorStand.getCustomName().asString().startsWith("KE$SitStand#")
                && armorStand.getScoreboardTags().contains("KE$SitStand@" + player.getUuid().toString())) {
            sitStands.remove(RegistryUtils.toIdentifier(armorStand.dimension), armorStand.getUuid());
            armorStand.setPos(armorStand.getX(), armorStand.getY() + 0.55D, armorStand.getZ());
            armorStand.kill();

            player.addMessage(LangText.get(true, "sit.stop_riding"), true);
        }

    }

    public void onStopRiding(ServerPlayerEntity player) {
        sitOff(player);
    }

    public void onScheduledUpdate() {
        sitStands.forEach((dim, uuid) -> {
            ServerWorld world = KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dim));
            ArmorStandEntity armorStand = (ArmorStandEntity) world.getEntity(uuid);
            if (armorStand != null) {
                if (!armorStand.hasPlayerRider() && armorStand.getCustomName() != null && armorStand.getCustomName().asString().startsWith("KE$SitStand#"))
                    armorStand.kill();

                if (world.getBlockState(armorStand.getBlockPos().up().up()).getBlock() == Blocks.AIR &&
                        armorStand.hasPassengers() && armorStand.getPassengerList().get(0) instanceof PlayerEntity) {

                    OnlineUser user = KiloServer.getServer().getOnlineUser((ServerPlayerEntity) armorStand.getPassengerList().get(0));
                    if (user.getSittingType() != null && user.getSittingType() != SummonType.COMMAND)
                        armorStand.kill();
                }
            }
        });
    }

    public void killAll() {
        sitStands.forEach((dim, uuid) -> {
            ArmorStandEntity armorStand = (ArmorStandEntity) KiloServer.getServer().getVanillaServer().getWorld(RegistryUtils.toDimension(dim)).getEntity(uuid);
            if (armorStand != null && !armorStand.hasPlayerRider() && armorStand.getCustomName() != null && armorStand.getCustomName().asString().startsWith("KE$SitStand#"))
                armorStand.kill();
        });
    }

    private Vec3dLocation getPosForStair(BlockState blockState, Vec3dLocation vec3dLoc) {
        Direction direction = blockState.get(Properties.HORIZONTAL_FACING);
        Vec3dLocation loc = vec3dLoc;
        double d = 0.205D;

        if (direction == Direction.NORTH) {
            loc.setZ(vec3dLoc.getZ() + d);
        }

        if (direction == Direction.WEST) {
            loc.setX(vec3dLoc.getX() + d);
        }

        if (direction == Direction.SOUTH) {
            loc.setZ(vec3dLoc.getZ() - d);
        }

        if (direction == Direction.EAST) {
            loc.setX(vec3dLoc.getX() - d);
        }

        return loc;
    }

    public enum SummonType {
        COMMAND,
        INTERACT_BLOCK,
        OTHERS,
    }
}