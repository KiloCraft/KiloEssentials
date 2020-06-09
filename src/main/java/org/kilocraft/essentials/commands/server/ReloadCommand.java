package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.user.CommandSourceServerUser;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class ReloadCommand extends EssentialCommand {
    public ReloadCommand() {
        super("reload", CommandPermission.RELOAD, new String[]{"rl"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        argumentBuilder.executes(this::execute);
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser src = this.getServerUser(ctx);
        StopWatch watch = new StopWatch();
        KiloChat.sendLangMessageTo(ctx.getSource(), "command.reload.start");

        watch.start();
        server.reload((throwable) -> {
            watch.stop();
            String str = tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
            logger.error(str);
            src.sendMessage(str);
        });

        watch.stop();
        src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        return AWAIT;
    }
}
