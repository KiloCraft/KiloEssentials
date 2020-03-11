package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.PagedText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlaytimetopCommand extends EssentialCommand {
    private static long cacheTime = 0L;

    public PlaytimetopCommand() {
        super("playtimetop", CommandPermission.PLAYTIMETOP, new String[]{"pttop", "topplaytimes"});
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, Integer> page = argument("page", IntegerArgumentType.integer(0))
                .executes(ctx -> this.send(ctx, IntegerArgumentType.getInteger(ctx, "page")));

        this.argumentBuilder.executes(this::execute);
        this.commandNode.addChild(page.build());
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) {
        return this.send(ctx, 1);
    }

    private int send(final CommandContext<ServerCommandSource> ctx, int page) {
        final CommandSourceUser src = this.getServerUser(ctx);
        final String LINE_FORMAT = ModConstants.translation("command.playtimetop.format");

        this.essentials.getAllUsersThenAcceptAsync(src, "general.wait_users", list -> {
            final HashMap<String, Integer> map = new HashMap<>();
            long totalTicks = 0L;

            for (User user : list) {
                map.put(user.getFormattedDisplayName(), user.getTicksPlayed());
                totalTicks += user.getTicksPlayed();
            }

            final List<Map.Entry<String, Integer>> sorted = new ArrayList<>(map.entrySet());
            sorted.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

            PagedText paged = new PagedText("pttop");

            paged.append(ModConstants.translation("command.playtimetop.total", TimeDifferenceUtil.convertSecondsToString((int) (totalTicks / 20L), 'e', '6')));
            for (int i = 0; i < sorted.size(); i++) {
                Map.Entry<String, Integer> entry = sorted.get(i);

                String pt = TimeDifferenceUtil.convertSecondsToString(entry.getValue() / 20, 'e', '6');
                paged.append(String.format(LINE_FORMAT, i + 1, entry.getKey(), pt));
            }


            paged.sendPage(src.getCommandSource(), page, 9, "Top Play Times", "/playtimetop %page%", false);
        });

        return SINGLE_SUCCESS;
    }
}
