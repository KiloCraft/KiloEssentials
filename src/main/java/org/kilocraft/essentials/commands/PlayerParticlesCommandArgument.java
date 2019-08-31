package org.kilocraft.essentials.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;

public class PlayerParticlesCommandArgument implements ArgumentType<String> {

	public static final Collection<String> NAMES = Arrays.asList("flames", "glass");

	public static PlayerParticlesCommandArgument particles() {
		return new PlayerParticlesCommandArgument();
	}

	public static String getParticleName(CommandContext<ServerCommandSource> commandContext_1, String string_1)
			throws CommandSyntaxException {
		return commandContext_1.getArgument(string_1, PlayerParticlesCommandArgument.class).toString();
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext_1,
			SuggestionsBuilder suggestionsBuilder_1) {
		return CommandSource.suggestIdentifiers((Iterable) NAMES, suggestionsBuilder_1);
	}

	public Collection<String> getExamples() {
		return NAMES;
	}

	 @Override
	public String parse(StringReader var1) throws CommandSyntaxException {
		return var1.getString();
	}
}
