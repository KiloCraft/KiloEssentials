package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.mixin.accessor.SpawnStateAccessor;
import org.kilocraft.essentials.patch.SpawnUtil;
import org.kilocraft.essentials.util.CommandPermission;

import java.util.Objects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LocalMobCapCalculator;
import net.minecraft.world.level.NaturalSpawner;

public class PlayerMobCapCommand extends EssentialCommand {

    public PlayerMobCapCommand() {
        super("pmobcap", CommandPermission.PMOBCAP);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::info);
    }

    private int info(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel world = ctx.getSource().getLevel();
        NaturalSpawner.SpawnState info = world.getChunkSource().getLastSpawnState();
        Objects.requireNonNull(info, "SpawnHelper.Info must not be null");
        LocalMobCapCalculator.MobCounts densityCap = ((SpawnStateAccessor) info).getLocalMobCapCalculator().playerMobCounts.getOrDefault(player, new LocalMobCapCalculator.MobCounts());
        MobCapCommand.sendMobCap(player, world, "Personal MobCap", densityCap.counts, group -> SpawnUtil.getPersonalMobCap(world, group));
        return SUCCESS;
    }

}
