package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.util.text.Texter;

public class TextFormattingCommand extends EssentialCommand {
	private static final MutableText text = Texter.InfoBlockStyle.of(tl("command.textformatting.title"))
			.newLine().appendRaw(
					TextFormat.translateAlternateColorCodes(
							'~',
							"~0&0Black ~1&1Dark Blue ~2&2Dark Green ~3&3Dark Aqua ~4&4Dark Red ~5&5Dark Purple " +
									"~6&6Gold ~7&7Gray ~8&8Dark Gray ~9&9Blue ~a&aGreen ~b&bAqua ~c&cRed ~d&dPurple ~e&eYellow ~f&fWhite\n\n" +
									"~f~lFormatting codes:\n~r~f&l ~lBold ~r&o~r~f~oItalic ~r&n~r~f ~nUnderline ~r&m~r~f~m Strike through ~r~f&k (Obfuscated) " +
									"~kKiloEssentials! ~f&r Reset!"
					)
			)
			.newLine().append(tl("command.textformatting.footer")).newLine()
			.build();

	public TextFormattingCommand() {
		super("textformating", new String[]{"colours", "colors"});
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		argumentBuilder.executes(this::execute);
	}

	public int execute(CommandContext<ServerCommandSource> context) {
		this.sendMessage(context, text);
		return SUCCESS;
	}
}
