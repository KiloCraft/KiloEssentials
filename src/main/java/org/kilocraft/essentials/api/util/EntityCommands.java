package org.kilocraft.essentials.api.util;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.spongepowered.include.com.google.common.base.Strings;

import java.util.UUID;

public class EntityCommands {

    private static final Object2LongMap<UUID> LAST_ENTITY_INTERACTION = new Object2LongArrayMap<>();

    public static void registerEvents() {
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> handleInteraction(player, entity, false));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> handleInteraction(player, entity, true));
    }

    private static ActionResult handleInteraction(PlayerEntity playerEntity, Entity entity, boolean leftClick) {
        if (isOnCooldown(playerEntity)) return ActionResult.PASS;
        if (entity != null) {
            EntityDataObject dataObject = new EntityDataObject(entity);
            NbtCompound nbt = dataObject.getNbt();
            if (runCommand(playerEntity, nbt.getString("command"))) return ActionResult.SUCCESS;
            if (runCommand(playerEntity, leftClick ?
                    nbt.getString("leftCommand") :
                    nbt.getString("rightCommand"))) return ActionResult.SUCCESS;
            LAST_ENTITY_INTERACTION.put(playerEntity.getUuid(), Util.getMeasuringTimeMs());
        }
        return ActionResult.PASS;
    }

    private static boolean isOnCooldown(PlayerEntity playerEntity) {
        return LAST_ENTITY_INTERACTION.getOrDefault(playerEntity.getUuid(), 0) + 50 > Util.getMeasuringTimeMs();
    }

    private static boolean runCommand(PlayerEntity playerEntity, String command) {
        if (!Strings.isNullOrEmpty(command)) {
            CommandUtils.runCommandWithFormatting(playerEntity.getCommandSource(), command);
            return true;
        }
        return false;
    }

}
