package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.NameLookup;
import org.kilocraft.essentials.util.Texter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WhowasCommand extends EssentialCommand {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String CACHE_ID = "command.whowas";

    public WhowasCommand() {
        super("whowas", CommandPermission.WHOWAS_SELF, new String[]{"namehistory"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.WHOIS_OTHERS))
                .executes(this::executeOthers);

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(userArgument.build());
    }


    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        CommandSourceUser user = getServerUser(ctx);
        return execute(user, getOnlineUser(ctx));
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getServerUser(ctx);
        essentials.getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            execute(src, user);
        });

        return AWAIT_RESPONSE;
    }

    private int execute(CommandSourceUser src, User target) {
        if (CacheManager.shouldUse(getCacheId(target.getUsername()))) {
            AtomicReference<NameLookup.PreviousPlayerNameEntry[]> reference = new AtomicReference<>();
            CacheManager.getAndRun(getCacheId(target.getUsername()), (cached) -> reference.set((NameLookup.PreviousPlayerNameEntry[]) cached.get()));
            return send(src, target, reference.get());
        }

        NameLookup.PreviousPlayerNameEntry[] entries;

        try {
            entries = NameLookup.getPlayerPreviousNames(target);
        } catch (IOException e) {
            src.sendError("Can not get the data! " + e.getMessage());
            return SINGLE_FAILED;
        }

        Cached<NameLookup.PreviousPlayerNameEntry[]> cached = new Cached<>(getCacheId(target.getUsername()), 3, TimeUnit.HOURS, entries);
        CacheManager.cache(cached);
        return send(src, target, entries);
    }

    private int send(CommandSourceUser src, User target, NameLookup.PreviousPlayerNameEntry[] nameEntries) {
        Texter.ListStyle text = Texter.ListStyle.of("Name history of " + target.getNameTag(), Formatting.GOLD, Formatting.YELLOW, Formatting.WHITE, Formatting.GRAY);
        for (NameLookup.PreviousPlayerNameEntry entry : nameEntries) {
            if (entry.isPlayersInitialName()) {
                text.append(
                        Texter.Events.onHover("&aInitial name\n&e" + dateFormat.format(new Date(entry.getChangeTime()))),
                        null,
                        Texter.toText(entry.getPlayerName()).formatted(Formatting.GREEN)
                );
            } else {
                text.append(
                        Texter.Events.onHover("&e" + dateFormat.format(new Date(entry.getChangeTime()))),
                        null,
                        entry.getPlayerName()
                );
            }
        }

        src.sendMessage(text.build());
        return SINGLE_SUCCESS;
    }

    private static String getCacheId(String username) {
        return CACHE_ID + "." + username;
    }
}
