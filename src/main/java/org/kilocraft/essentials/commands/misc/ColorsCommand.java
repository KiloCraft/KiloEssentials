package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;

import static net.minecraft.server.command.CommandManager.literal;

public class ColorsCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("chatformats").executes(ColorsCommand::execute));
	}

	public static int execute(CommandContext<ServerCommandSource> context) {
		TextFormat.sendToUniversalSource('~', context.getSource(),
				"~f~lColour codes:~r\n~0&0Black ~1&1Dark Blue ~2&2Dark Green ~3&3Dark Aqua ~4&4Dark Red ~5&5Dark Purple " +
						"~6&6Gold ~7&7Gray ~8&8Dark Gray ~9&9Blue ~a&aGreen ~b&bAqua ~c&cRed ~d&dPurple ~e&eYellow ~f&fWhite\n\n" +
						"~f~lFormatting codes:\n~r~f~l&lBold ~r~f~o&oItalic ~r~f~n&nUnderline ~r~f~m&mStrikethrough ~r~f&kObfuscated" +
						"~kexample ~f&rReset To Default\n",
				false);
		return 0;
	}
}
