package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;
import org.kilocraft.essentials.craft.user.User;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RealNameCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("realname");
		dispatcher.register(literal("realname")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("realname"), 2))
				.then(argument("target", string()).executes(RealNameCommand::execute)));
	}

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		String input = getString(ctx, "target");

		for (int i = 0; i < ctx.getSource().getWorld().getPlayers().size(); i++) {
			PlayerEntity player = ctx.getSource().getWorld().getPlayers().get(i);
			User user = KiloServer.getServer().getUserManager().getUser(player.getUuid());

			if (input.equals(user.getNickname()) && input != "") {
				ctx.getSource().getPlayer().sendMessage(LangText.getFormatter(true,
						"command.realname.success", input, player.getName().asString()));
				return 0;
			}
		}

		ctx.getSource().sendFeedback(LangText.getFormatter(true, "command.realname.error", input), false);

		return 0;
	}
}
