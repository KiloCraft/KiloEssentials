package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class RelnameCommand extends EssentialCommand {
	public RelnameCommand() {
		super("realname", CommandPermission.REALNAME);
		this.withUsage("command.realname.usage", "nickname");
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		RequiredArgumentBuilder<ServerCommandSource, String> nickArgument = argument("nickname", StringArgumentType.greedyString())
				.suggests(TabCompletions::allPlayerNicks)
				.executes(this::execute);

		commandNode.addChild(nickArgument.build());
	}

	private int execute(CommandContext<ServerCommandSource> ctx){
		String input = getString(ctx, "nickname");

		for (OnlineUser user : server.getUserManager().getOnlineUsersAsList()) {
			String nick = TextFormat.removeAlternateColorCodes('&', user.getDisplayName());
			if (input.equalsIgnoreCase(nick) || input.equals(user.getUsername())) {
				KiloChat.sendLangMessageTo(ctx.getSource(), "command.realname.success", user.getFormattedDisplayName(), user.getUsername());
				return SINGLE_SUCCESS;
			}
		}

		KiloChat.sendLangMessageTo(ctx.getSource(), "command.realname.error");
		return SINGLE_FAILED;
	}

}
