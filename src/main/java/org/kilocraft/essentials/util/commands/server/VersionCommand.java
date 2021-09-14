package org.kilocraft.essentials.util.commands.server;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class VersionCommand extends EssentialCommand {
    public VersionCommand() {
        super("version");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::executeVersion);
    }

    private int executeVersion(CommandContext<ServerCommandSource> ctx) {
        GameVersion gameVersion = MinecraftVersion.create();

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
