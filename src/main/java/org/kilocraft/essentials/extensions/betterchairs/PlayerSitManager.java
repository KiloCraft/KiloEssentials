package org.kilocraft.essentials.extensions.betterchairs;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.RegistryUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerSitManager implements ConfigurableFeature {
    public static PlayerSitManager INSTANCE;
    public static boolean enabled = false;
    private static Map<ArmorStandEntity, UUID> map;

    @Override
    public boolean register() {
        INSTANCE = this;
        enabled = true;
        KiloEssentials.getInstance().getCommandHandler().register(new SitCommand());
        map = new HashMap<>();
        return true;
    }

    public PlayerSitManager() {
    }

    private boolean hasPermission(ServerPlayerEntity player) {
        return KiloEssentials.hasPermissionNode(player.getCommandSource(), EssentialPermission.SIT);
    }

    public boolean onInteractBlock(ServerPlayerEntity player, BlockHitResult hitResult, Hand hand) {
        if (hand != Hand.MAIN_HAND || !player.getMainHandStack().equals(ItemStack.EMPTY) ||
                player.getVehicle() != null || !KiloServer.getServer().getOnlineUser(player).canSit() || !hasPermission(player) || player.isSneaking())
            return false;

        BlockPos targetBlock = hitResult.getBlockPos();
        ServerWorld world = player.getServerWorld();
        BlockState blockState = world.getBlockState(targetBlock);

        player.swingHand(Hand.MAIN_HAND, true);
        Vec3dLocation vec3dLoc = Vec3dLocation.of(targetBlock.getX(), targetBlock.getY() + 1, targetBlock.getZ(),
                player.yaw, player.pitch, RegistryUtils.toIdentifier(world.dimension.getType()));

        if (blockState.getBlock() instanceof StairsBlock) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.40D);
            return sitOn(player, vec3dLoc.center());
        } else if (blockState.getBlock() instanceof SlabBlock) {
            vec3dLoc.setY(vec3dLoc.getY() - 0.45D);
            return sitOn(player, vec3dLoc.center());
        }

        return false;
    }

    public boolean set(ServerPlayerEntity player, Vec3dLocation loc, boolean sit) {
        return sit ? sitOn(player, loc) : sitOff(player);
    }

    public boolean sitOn(ServerPlayerEntity player, Vec3dLocation loc) {
        if (player.isSpectator() || !KiloServer.getServer().getOnlineUser(player).canSit())
            return false;

        ArmorStandEntity armorStand = EntityType.ARMOR_STAND.create(loc.getWorld(), null,
                new LiteralText("KE$SitStand#" + map.size() + "$" + player.getEntityName()), null,
                loc.toPos(), SpawnType.TRIGGERED, true, true);

        if (armorStand == null)
            return false;

        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        armorStand.addScoreboardTag("KE$SitStand");
        armorStand.updatePosition(loc.getX(), loc.getY() - 1.75, loc.getZ());

        loc.getWorld().spawnEntity(armorStand);
        player.startRiding(armorStand, true);
        map.put(armorStand, player.getUuid());
        KiloServer.getServer().getOnlineUser(player).setSitting(true);

        return true;
    }

    public boolean sitOff(ServerPlayerEntity player) {
        AtomicBoolean result = new AtomicBoolean(false);
        map.forEach(((armorStand, uuid) -> {
            if (player.getUuid() == uuid && armorStand != null && armorStand.getScoreboardTags() != null && armorStand.getScoreboardTags().contains("KE$SitStand")) {
                player.stopRiding();
                map.remove(armorStand);
                armorStand.kill();
                result.set(true);
                KiloServer.getServer().getOnlineUser(player).setSitting(false);
            }
        }));

        return result.get();
    }

    public void onScheduledUpdate() {
        map.forEach((armorStand, player) -> {
            if (armorStand != null && !armorStand.hasPlayerRider() && armorStand.getScoreboardTags().contains("KE$SitStand"))
                armorStand.kill();
        });
    }

    public void killAll() {
        map.forEach((armorStand, player) -> {
            if (armorStand != null)
                armorStand.kill();
        });
    }

    public enum SummonType {
        COMMAND,
        INTERACT_BLOCK
    }
}