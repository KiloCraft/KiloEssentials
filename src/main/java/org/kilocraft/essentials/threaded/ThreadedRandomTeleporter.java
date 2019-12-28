package org.kilocraft.essentials.threaded;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.commands.teleport.RtpCommand;

public class ThreadedRandomTeleporter implements Runnable, KiloThread {
    private Logger logger;
    private ServerPlayerEntity playerEntity;
    private ServerCommandSource commandSource;

    public ThreadedRandomTeleporter(ServerPlayerEntity player, ServerCommandSource source) {
        this.playerEntity = player;
        this.commandSource = source;

    }

    @Override
    public String getName() {
        return "RandomTeleporter";
    }

    @Override
    public void run() {
        logger = LogManager.getFormatterLogger(getName());
        logger.info("RandomTeleporter thread started by " + this.commandSource.getName());
        RtpCommand.teleportRandomly(this.commandSource, this.playerEntity);
    }

    @Override
    public Logger getLogger() {
        return LogManager.getFormatterLogger();
    }
}
