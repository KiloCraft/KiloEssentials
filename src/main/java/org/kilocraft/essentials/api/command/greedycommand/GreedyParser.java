package org.kilocraft.essentials.api.command.greedycommand;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.api.KiloEssentials;

public class GreedyParser {
    private boolean allowMultipleSelections;
    private String label;
    private String argLabel;
    private CommandContext<ServerCommandSource> ctx;
    @Nullable private String ctxIn;
    private String[] args;


    public GreedyParser(CommandContext<ServerCommandSource> context, String argumentLabel, boolean multipleSelections) {
        this.ctx = context;
        this.label = context.getNodes().get(0).getNode().getName();
        this.argLabel = argumentLabel;
        this.allowMultipleSelections = multipleSelections;

        KiloEssentials.getLogger().debug("New GreedyParser instance for Command: " + this.label);
    }

    public void setAllowMultipleSelections(boolean set) {
        this.allowMultipleSelections = set;
    }

    public GreedyParser parse() {
        this.ctxIn = this.ctx.getArgument(this.argLabel, String.class);
        this.args = this.ctxIn.split(" ");

        return this;
    }

    public String getContextInput() {
        return this.ctxIn;
    }

    public String[] getArguments() {
        return this.args;
    }

    public ServerPlayerEntity getPlayer(int argPos) throws CommandSyntaxException {
        ServerPlayerEntity target = KiloEssentials.getServer().getPlayer(getString(argPos));
        if (target != null)
            return target;
        else
            throw new SimpleCommandExceptionType(new LiteralText("Can not find that player!")).create();
    }

    public int getInteger(int argPos) {
        return Integer.parseInt(getArgument(argPos));
    }

    public String getString(int argPos) {
        return getArgument(argPos);
    }

    public String getRawArgument(int argPos) {
        return getArgument(argPos);
    }

    private String getArgument(int pos) {
        return this.args.length > pos ? this.args[pos] : "";
    }

    public String getCommandLabel() {
        return this.label;
    }

}
