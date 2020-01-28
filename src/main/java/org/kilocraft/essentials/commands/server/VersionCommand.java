package org.kilocraft.essentials.commands.server;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.chat.KiloChat;

public class VersionCommand extends EssentialCommand {
    public VersionCommand() {
        super("version");
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::executeVersion);
    }

    private int executeVersion(CommandContext<ServerCommandSource> ctx) {
        GameVersion gameVersion = MinecraftVersion.create();

        KiloChat.sendLangMessageTo(ctx.getSource(), "command.info.version",
                ModConstants.getBuildType(),
                ModConstants.getVersionInt(),
                ModConstants.getGitHash(),
                ModConstants.getGitBranch(),
                ModConstants.getLoaderVersion(),
                ModConstants.getMappingsVersion(),
                ModConstants.getMinecraftVersion(),
                gameVersion.isStable(),
                gameVersion.getReleaseTarget(),
                gameVersion.getWorldVersion());

        return SINGLE_SUCCESS;
    }

}
