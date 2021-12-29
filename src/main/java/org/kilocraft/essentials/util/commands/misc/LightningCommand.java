package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.util.EntityServerRayTraceable;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.util.CommandPermission;

public class LightningCommand extends EssentialCommand {
    public LightningCommand() {
        super("lightning", CommandPermission.SMITE);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Vec3 vec3d = ((EntityServerRayTraceable) player).rayTrace(90.0D, 1.0F, true).getLocation();
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(player.getLevel(),
                null, null, player, new BlockPos(vec3d), MobSpawnType.COMMAND, true, false);

        player.displayClientMessage(StringText.of("command.smite"), true);
        return player.getLevel().addFreshEntity(lightning) ? SUCCESS : FAILED;
    }

}
