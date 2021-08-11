package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.concurrent.CompletableFuture;

public class ToggleChatCommand extends EssentialCommand {
    public ToggleChatCommand() {
        super("togglechat", new String[]{"chatvisibility"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> type = argument("type", StringArgumentType.word())
                .suggests(this::listSuggestions)
                .executes((ctx) -> execute(ctx, StringArgumentType.getString(ctx, "type")));

        this.argumentBuilder.then(type);
        this.argumentBuilder.executes(this::toggle);
    }

    private int toggle(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        if (src.getPreference(Preferences.CHAT_VISIBILITY) == ServerChat.VisibilityPreference.ALL) {
            return set(src, ServerChat.VisibilityPreference.MENTIONS);
        } else {
            return set(src, ServerChat.VisibilityPreference.ALL);
        }
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, final String input) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        ServerChat.VisibilityPreference preference = ServerChat.VisibilityPreference.getByName(input);
        if (preference == null) {
            src.sendLangError("command.togglechat.invalid_type", input);
            return FAILED;
        }

        return set(src, preference);
    }

    private int set(final OnlineUser src, final ServerChat.VisibilityPreference preference) {
        src.getPreferences().set(Preferences.CHAT_VISIBILITY, preference);
        src.sendLangMessage("command.togglechat.set", preference.toString());
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ServerChat.VisibilityPreference.names(), builder);
    }

}
