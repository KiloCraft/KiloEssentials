package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.text.TextFormat;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class RealNameCommand extends EssentialCommand {
	public RealNameCommand() {
		super("realname", CommandPermission.REALNAME);
		this.withUsage("command.realname.usage", "nickname");
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		RequiredArgumentBuilder<ServerCommandSource, String> nickArgument = argument("nickname", StringArgumentType.greedyString())
				.suggests(ArgumentSuggestions::allPlayerNicks)
				.executes(this::execute);

		commandNode.addChild(nickArgument.build());
	}

	private int execute(CommandContext<ServerCommandSource> ctx){
		String input = getString(ctx, "nickname");
        CommandSourceUser source = KiloServer.getServer().getCommandSourceUser(ctx.getSource());
		for (OnlineUser user : this.getServer().getUserManager().getOnlineUsersAsList()) {
			String nick = ComponentText.clearFormatting(user.getDisplayName());
			if (input.equalsIgnoreCase(nick) || input.equals(user.getUsername())) {
                source.sendLangMessage("command.realname.success", user.getFormattedDisplayName(), user.getUsername());
				return SUCCESS;
			}
		}

        source.sendLangMessage( "command.realname.error");
		return FAILED;
	}

}
