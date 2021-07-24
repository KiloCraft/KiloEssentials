package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.ChunkRandom;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class SlimeChunkCommand extends EssentialCommand {

    public SlimeChunkCommand() {
        super("slimechunk", CommandPermission.SLIME_CHUNK);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    public int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ChunkPos chunkPos = ctx.getSource().getPlayer().getChunkPos();
        boolean bl = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z, ((StructureWorldAccess)ctx.getSource().getWorld()).getSeed(), 987234911L).nextInt(10) == 0;
        getCommandSource(ctx).sendLangMessage("command.slimechunk.info", bl ? "<green>true" : "<red>false");
        return SUCCESS;
    }
}
