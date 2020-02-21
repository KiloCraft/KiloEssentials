package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.chat.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;

public class ColorsCommand extends EssentialCommand {
	public ColorsCommand() {
		super("colors", new String[]{"colours", "textformats"});
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		argumentBuilder.executes(this::execute);
	}

	public int execute(CommandContext<ServerCommandSource> context) {
		TextFormat.sendToUniversalSource('~', context.getSource(),
				"~f~lText Formats:~r\n~0&0Black ~1&1Dark Blue ~2&2Dark Green ~3&3Dark Aqua ~4&4Dark Red ~5&5Dark Purple " +
						"~6&6Gold ~7&7Gray ~8&8Dark Gray ~9&9Blue ~a&aGreen ~b&bAqua ~c&cRed ~d&dPurple ~e&eYellow ~f&fWhite\n\n" +
						"~f~lFormatting codes:\n~r~f~l&lBold ~r~f~o&oItalic ~r~f~n&nUnderline ~r~f~m&mStrikethrough ~r~f&kObfuscated" +
						"~kexample ~f&rReset To Default\n~ePro tip:\n~eUse ~a/formatpreview <text>~e to get a preview of it!",
				false);
		return 0;
	}
}
