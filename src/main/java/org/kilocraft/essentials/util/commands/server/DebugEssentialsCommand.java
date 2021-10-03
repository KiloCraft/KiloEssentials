package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.chunk.Chunk;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class DebugEssentialsCommand extends EssentialCommand {
    public DebugEssentialsCommand() {
        super("debug", src -> src.hasPermissionLevel(3));
        this.withForkType(ForkType.MAIN_ONLY);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> modeOn = this.literal("on")
                .executes(ctx -> this.setDebugMode(ctx, true));
        LiteralArgumentBuilder<ServerCommandSource> modeOff = this.literal("off")
                .executes(ctx -> this.setDebugMode(ctx, false));

        LiteralArgumentBuilder<ServerCommandSource> bar = this.literal("bar");
        {
            LiteralArgumentBuilder<ServerCommandSource> barOn = this.literal("on")
                    .executes(ctx -> this.setDebugBar(ctx, true));
            LiteralArgumentBuilder<ServerCommandSource> barOff = this.literal("off")
                    .executes(ctx -> this.setDebugBar(ctx, false));

            bar.then(barOn);
            bar.then(barOff);
        }

        this.argumentBuilder.then(modeOn);
        this.argumentBuilder.then(modeOff);
        this.argumentBuilder.then(bar);
    }

    private int setDebugMode(final CommandContext<ServerCommandSource> ctx, boolean set) {
        KiloDebugUtils.setDebugMode(set);
        this.sendFeedback(ctx, "command.debug.mode", set);
        return set ? 1 : 0;
    }

    private int setDebugBar(final CommandContext<ServerCommandSource> ctx, boolean set) {
        KiloDebugUtils.setDebugBarVisible(set);
        this.sendFeedback(ctx, set ? "command.debug.bar.visible" : "command.debug.bar.invisible");
        return set ? 1 : 0;
    }

    private void sendFeedback(final CommandContext<ServerCommandSource> ctx, String key, Object... objects) {
        this.getCommandSource(ctx).sendLangMessage(key, objects);
    }
}
