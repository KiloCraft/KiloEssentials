package org.kilocraft.essentials.api.util;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.spongepowered.include.com.google.common.base.Strings;

import java.util.UUID;

public class EntityCommands {

    private static final Object2LongMap<UUID> LAST_ENTITY_INTERACTION = new Object2LongArrayMap<>();

    public static void registerEvents() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> handleInteraction(player, entity, false));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> handleInteraction(player, entity, true));
    }

    private static InteractionResult handleInteraction(Player playerEntity, Entity entity, boolean leftClick) {
        if (isOnCooldown(playerEntity)) return InteractionResult.PASS;
        if (entity != null) {
            EntityDataAccessor dataObject = new EntityDataAccessor(entity);
            CompoundTag nbt = dataObject.getData();
            if (runCommand(playerEntity, nbt.getString("command"))) return InteractionResult.SUCCESS;
            if (runCommand(playerEntity, leftClick ?
                    nbt.getString("leftCommand") :
                    nbt.getString("rightCommand"))) return InteractionResult.SUCCESS;
            LAST_ENTITY_INTERACTION.put(playerEntity.getUUID(), Util.getMillis());
        }
        return InteractionResult.PASS;
    }

    private static boolean isOnCooldown(Player playerEntity) {
        return LAST_ENTITY_INTERACTION.getOrDefault(playerEntity.getUUID(), 0) + 50 > Util.getMillis();
    }

    private static boolean runCommand(Player playerEntity, String command) {
        if (!Strings.isNullOrEmpty(command)) {
            CommandUtils.runCommandWithFormatting(playerEntity.createCommandSourceStack(), command);
            return true;
        }
        return false;
    }

}
