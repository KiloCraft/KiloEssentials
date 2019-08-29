package org.kilocraft.essentials.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import org.kilocraft.essentials.Mod;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class DonatorParticlesCommandArgument implements ArgumentType<String> {

	private static final Collection<String> NAMES = Arrays.asList("flames", "glass");
	public static final DynamicCommandExceptionType INVALID_PARTICLE_EXCEPTION = new DynamicCommandExceptionType(
			(name) -> {
				return new LiteralText(Mod.messages.getProperty("particlenotfound") + name);
			});

	public static DonatorParticlesCommandArgument particles() {
		return new DonatorParticlesCommandArgument();
	}

	public static String getParticleName(CommandContext<ServerCommandSource> commandContext_1, String string_1)
			throws CommandSyntaxException {
		return commandContext_1.getArgument(string_1, String.class);
	}

	public String method_9348(StringReader stringReader_1) throws CommandSyntaxException {
		String name = stringReader_1.getString();
		if (NAMES.contains(name)) {
			return name;
		} else {
			return INVALID_PARTICLE_EXCEPTION.create(name).toString();
		}
	}

	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext_1,
			SuggestionsBuilder suggestionsBuilder_1) {
		return CommandSource.suggestIdentifiers((Iterable) NAMES, suggestionsBuilder_1);
	}

	public Collection<String> getExamples() {
		return NAMES;
	}

	public String parse(StringReader var1) throws CommandSyntaxException {
		return this.method_9348(var1).toString();
	}
}
