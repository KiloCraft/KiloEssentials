package org.kilocraft.essentials.commands.server;

import com.mojang.bridge.game.GameVersion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.chat.KiloChat;

public class VersionCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode<ServerCommandSource> versionCommand = dispatcher.register(
                CommandManager.literal("version").executes(context -> executeVersion(context.getSource()))
        );

        LiteralCommandNode<ServerCommandSource> infoCommand = dispatcher.register(
                CommandManager.literal("essentials").executes(context -> executeInfo(context.getSource()))
        );

        dispatcher.getRoot().addChild(versionCommand);
        dispatcher.getRoot().addChild(infoCommand);
    }

    private static int executeVersion(ServerCommandSource source) {
        GameVersion gameVersion = MinecraftVersion.create();

        KiloChat.sendLangMessageTo(source, "command.info.version",
                ModConstants.getBuildType(),
                ModConstants.getVersionInt(),
                ModConstants.getBuildTime(),
                ModConstants.getGitHash(),
                ModConstants.getGitBranch(),
                ModConstants.getLoaderVersion(),
                ModConstants.getMappingsVersion(),
                ModConstants.getMinecraftVersion(),
                gameVersion.isStable(),
                gameVersion.getReleaseTarget(),
                gameVersion.getWorldVersion(),
                gameVersion.getBuildTime()
        );

        return KiloCommands.SUCCESS();
    }

    private static int executeInfo(ServerCommandSource source) {
        KiloChat.sendLangMessageTo(source, "command.info",
                ModConstants.getMinecraftVersion()
        );

        return KiloCommands.SUCCESS();
    }

}
