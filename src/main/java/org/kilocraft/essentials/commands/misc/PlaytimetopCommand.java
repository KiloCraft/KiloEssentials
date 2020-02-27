package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;

public class PlaytimetopCommand extends EssentialCommand {
    public PlaytimetopCommand() {
        super("playtimetop", CommandPermission.PLAYTIMETOP, new String[]{"pttop"});
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        this.argumentBuilder.executes(this::execute);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        final OnlineUser src = getOnlineUser(ctx);

        this.essentials.getAllUsersThenAcceptAsync(src, "general.wait_users", list -> {
            for (User user : list) {
                System.out.println("Found: " + user.getUsername());
            }
        });

        return SINGLE_SUCCESS;
    }
}
