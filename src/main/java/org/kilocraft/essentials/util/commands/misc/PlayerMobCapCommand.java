package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnDensityCapper;
import net.minecraft.world.SpawnHelper;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.mixin.patch.performance.optimizedSpawning.SpawnHelperInfoAccessor;
import org.kilocraft.essentials.patch.optimizedSpawning.SpawnUtil;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.Objects;

public class PlayerMobCapCommand extends EssentialCommand {

    public PlayerMobCapCommand() {
        super("pmobcap", CommandPermission.PMOBCAP);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::info);
    }

    private int info(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerWorld world = ctx.getSource().getWorld();
        SpawnHelper.Info info = world.getChunkManager().getSpawnInfo();
        Objects.requireNonNull(info, "SpawnHelper.Info must not be null");
        SpawnDensityCapper.DensityCap densityCap = ((SpawnHelperInfoAccessor) info).getDensityCapper().playersToDensityCap.getOrDefault(player, new SpawnDensityCapper.DensityCap());
        MobCapCommand.sendMobCap(player, world, "Personal MobCap", densityCap.spawnGroupsToDensity, group -> SpawnUtil.getPersonalMobCap(world, group));
        return SUCCESS;
    }

}
