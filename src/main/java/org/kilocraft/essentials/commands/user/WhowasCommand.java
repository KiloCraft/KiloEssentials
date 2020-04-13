package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.util.*;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class WhowasCommand extends EssentialCommand {
    private static final String LINE_FORMAT = ModConstants.translation("command.whowas.format");
    private static final String DATE_FORMAT = ModConstants.translation("command.whowas.format.time");
    private static final String INITIAL_FORMAT = ModConstants.translation("command.whowas.format.initial");
    private static final String CACHE_ID = "command.whowas";

    public WhowasCommand() {
        super("whowas", CommandPermission.WHOWAS_SELF, new String[]{"namehistory"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> usernameArgument = getUserArgument("username")
                .executes(ctx -> executeOthers(ctx, 1));

        RequiredArgumentBuilder<ServerCommandSource, Integer> pageArgument = argument("page", IntegerArgumentType.integer(1))
                .executes(ctx -> executeOthers(ctx, IntegerArgumentType.getInteger(ctx, "page")));

        argumentBuilder.executes(this::executeSelf);
        usernameArgument.then(pageArgument);
        commandNode.addChild(usernameArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = this.getOnlineUser(ctx);
        return send(user, user.getUsername(), 1);
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx, int page) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String input = this.getUserArgumentInput(ctx, "username");

        if (!src.hasPermission(CommandPermission.WHOIS_OTHERS) && !src.getUsername().equals(input)) {
            return src.sendLangError("command.exception.permission");
        }

        CompletableFuture.supplyAsync(() -> {
            ServerUserManager.LoadingText loadingText = new ServerUserManager.LoadingText(src.asPlayer(), "general.querying");

            try {
                loadingText.start();
                send(src, input, page);
                loadingText.stop();
            } catch (Exception e) {
                src.sendLangError("api.mojang.request_failed", e.getMessage());
            }

            return AWAIT_RESPONSE;
        });

        return AWAIT_RESPONSE;
    }

    private int send(OnlineUser src, String name, int page) {
        String uuid = null;
        try {
            uuid = NameLookup.getPlayerUUID(name);

            if (uuid == null) {
                throw GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION.create();
            }
        } catch (Exception e) {
            src.sendMessage(Texter.exceptionToText(e, false));
        }

        if (CacheManager.shouldUse(getCacheId(uuid))) {
            AtomicReference<NameLookup.PreviousPlayerNameEntry[]> reference = new AtomicReference<>();
            CacheManager.getAndRun(getCacheId(uuid), (cached) -> reference.set((NameLookup.PreviousPlayerNameEntry[]) cached.get()));
            return send(src, name, page, reference.get());
        }

        try {
            src.sendLangMessage("api.mojang.wait");
            NameLookup.PreviousPlayerNameEntry[] entries = NameLookup.getPlayerPreviousNames(uuid);
            Cached<NameLookup.PreviousPlayerNameEntry[]> cached = new Cached<>(getCacheId(uuid), 3, TimeUnit.HOURS, entries);
            CacheManager.cache(cached);

            return send(src, name, page, entries);
        } catch (IOException e) {
            src.sendError("Can not get the data! " + e.getMessage());
            return SINGLE_FAILED;
        }
    }

    private int send(OnlineUser src, String name, int page, NameLookup.PreviousPlayerNameEntry[] nameEntries) {
        TextInput input = new TextInput();

        if (nameEntries == null) {
            return src.sendLangError("api.mojang.request_failed", "Could not get the name history of that player");
        }

        int i = 0;
        for (NameLookup.PreviousPlayerNameEntry entry : nameEntries) {
            i++;
            Text dateText;
            if (entry.isPlayersInitialName()) {
                dateText = Texter.toText(INITIAL_FORMAT);
            } else {
                dateText = Texter.toText(String.format(DATE_FORMAT, TimeDifferenceUtil.formatDateDiff(entry.getChangeTime())))
                        .styled((style) ->
                                style.setHoverEvent(Texter.Events.onHover("&d" + ModConstants.DATE_FORMAT.format(new Date(entry.getChangeTime()))))
                        );
            }

            input.append(Texter.toText(String.format(LINE_FORMAT, i, entry.getPlayerName())).append(" ").append(dateText));
        }

        Pager.Page paged = Pager.getPageFromText(Pager.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());

        paged.send(src.getCommandSource(), "Name history of " + name + " (&e" + i + "&6)", "/whowas " + name + " %page%");
        return SINGLE_SUCCESS;
    }

    private static String getCacheId(String username) {
        return CACHE_ID + "." + username;
    }
}
