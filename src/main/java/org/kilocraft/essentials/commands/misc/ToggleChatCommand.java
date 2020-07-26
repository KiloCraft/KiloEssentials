package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
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
                .executes(this::execute);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        String input = StringArgumentType.getString(ctx, "type");
        ServerChat.VisibilityPreference preference = ServerChat.VisibilityPreference.getByName(input);
        if (preference == null) {
            src.sendLangError("command.togglechat.invalid_type", input);
            return FAILED;
        }

        src.getPreferences().set(Preferences.CHAT_VISIBILITY, preference);
        src.sendLangMessage("command.togglechat.set", preference.toString());
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(ServerChat.VisibilityPreference.names(), builder);
    }

}
