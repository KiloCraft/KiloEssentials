package org.kilocraft.essentials.util;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.kilocraft.essentials.commands.CommandUtils;

import java.util.UUID;

public class InteractionHandler {

    static Object2LongMap<UUID> lastInteractions = new Object2LongArrayMap<>();

    public static void handleInteraction(ServerPlayerEntity playerEntity, Entity entity, boolean leftClick) {
        if (lastInteractions.getOrDefault(playerEntity.getUuid(), 0) + 50 > Util.getMeasuringTimeMs()) return;
        if (entity != null) {
            EntityDataObject entityDataObject = new EntityDataObject(entity);
            NbtCompound tag = entityDataObject.getTag();
            String command = tag.getString("command");
            String specificCommand = leftClick ?
                    tag.getString("leftCommand") :
                    tag.getString("rightCommand");
            if (!command.equals("")) {
                CommandUtils.runCommandWithFormatting(playerEntity.getCommandSource(), command);
            }
            if (!specificCommand.equals("")) {
                CommandUtils.runCommandWithFormatting(playerEntity.getCommandSource(), specificCommand);
            }
        }
        lastInteractions.put(playerEntity.getUuid(), Util.getMeasuringTimeMs());
    }

}
