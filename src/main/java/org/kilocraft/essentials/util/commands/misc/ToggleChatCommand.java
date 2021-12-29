package org.kilocraft.essentials.util.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class ToggleChatCommand extends EssentialCommand {
    public ToggleChatCommand() {
        super("togglechat", new String[]{"chatvisibility"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> type = this.argument("type", StringArgumentType.word())
                .suggests(this::listSuggestions)
                .executes((ctx) -> this.execute(ctx, StringArgumentType.getString(ctx, "type")));

        this.argumentBuilder.then(type);
        this.argumentBuilder.executes(this::toggle);
    }

    private int toggle(final CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        if (src.getPreference(Preferences.CHAT_VISIBILITY) == ServerChat.VisibilityPreference.ALL) {
            return this.set(src, ServerChat.VisibilityPreference.MENTIONS);
        } else {
            return this.set(src, ServerChat.VisibilityPreference.ALL);
        }
    }

    private int execute(final CommandContext<CommandSourceStack> ctx, final String input) throws CommandSyntaxException {
        OnlineUser src = this.getOnlineUser(ctx);
        ServerChat.VisibilityPreference preference = ServerChat.VisibilityPreference.getByName(input);
        if (preference == null) {
            src.sendLangError("command.togglechat.invalid_type", input);
            return FAILED;
        }

        return this.set(src, preference);
    }

    private int set(final OnlineUser src, final ServerChat.VisibilityPreference preference) {
        src.getPreferences().set(Preferences.CHAT_VISIBILITY, preference);
        src.sendLangMessage("command.togglechat.set", preference.toString());
        return SUCCESS;
    }

    private CompletableFuture<Suggestions> listSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ServerChat.VisibilityPreference.names(), builder);
    }

}
