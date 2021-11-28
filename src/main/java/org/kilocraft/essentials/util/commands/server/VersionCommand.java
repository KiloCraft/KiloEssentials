package org.kilocraft.essentials.util.commands.server;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.DetectedVersion;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class VersionCommand extends EssentialCommand {
    public VersionCommand() {
        super("version");
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        this.argumentBuilder.executes(this::executeVersion);
    }

    private int executeVersion(CommandContext<CommandSourceStack> ctx) {
        GameVersion gameVersion = DetectedVersion.tryDetectVersion();

        this.getCommandSource(ctx).sendLangMessage("command.info.version",
                ModConstants.getVersionInt(),
                ModConstants.getBuildType(),
                ModConstants.getVersionType(),
                ModConstants.getVersionNick(),
                ModConstants.getGitHash(),
                ModConstants.getGitBranch(),
                ModConstants.getLoaderVersion(),
                ModConstants.getMappingsVersion(),
                ModConstants.getMinecraftVersion(),
                gameVersion.isStable() ? "release" : "snapshot",
                gameVersion.getReleaseTarget()
        );

        return SUCCESS;
    }

}
