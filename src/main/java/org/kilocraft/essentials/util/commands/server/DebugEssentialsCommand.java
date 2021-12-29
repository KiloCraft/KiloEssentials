package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class DebugEssentialsCommand extends EssentialCommand {
    public DebugEssentialsCommand() {
        super("debug", src -> src.hasPermission(3));
        this.withForkType(ForkType.MAIN_ONLY);
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> modeOn = this.literal("on")
                .executes(ctx -> this.setDebugMode(ctx, true));
        LiteralArgumentBuilder<CommandSourceStack> modeOff = this.literal("off")
                .executes(ctx -> this.setDebugMode(ctx, false));

        this.argumentBuilder.then(modeOn);
        this.argumentBuilder.then(modeOff);
    }

    private int setDebugMode(final CommandContext<CommandSourceStack> ctx, boolean set) {
        this.getCommandSource(ctx).sendLangMessage("command.debug.mode", set);
        SharedConstants.IS_RUNNING_IN_IDE = set;
        return set ? 1 : 0;
    }
}
