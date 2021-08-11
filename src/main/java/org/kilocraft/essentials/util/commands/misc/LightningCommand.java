package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.chat.StringText;

public class LightningCommand extends EssentialCommand {
    public LightningCommand() {
        super("lightning", CommandPermission.SMITE);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        Vec3d vec3d = ((EntityServerRayTraceable) player).rayTrace(90.0D, 1.0F, true).getPos();
        LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(player.getServerWorld(),
                null, null, player, new BlockPos(vec3d), SpawnReason.COMMAND, true, false);

        player.sendMessage(StringText.of(true, "command.smite"), true);
        return player.getServerWorld().spawnEntity(lightning) ? SUCCESS : FAILED;
    }

}
