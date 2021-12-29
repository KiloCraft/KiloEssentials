package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.CommandPermission;

public class SlimeChunkCommand extends EssentialCommand {

    public SlimeChunkCommand() {
        super("slimechunk", CommandPermission.SLIME_CHUNK);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ChunkPos chunkPos = ctx.getSource().getPlayerOrException().chunkPosition();
        boolean bl = WorldgenRandom.seedSlimeChunk(chunkPos.x, chunkPos.z, ((WorldGenLevel) ctx.getSource().getLevel()).getSeed(), 987234911L).nextInt(10) == 0;
        this.getCommandSource(ctx).sendLangMessage("command.slimechunk.info", bl ? "<green>true" : "<red>false");
        return SUCCESS;
    }
}
