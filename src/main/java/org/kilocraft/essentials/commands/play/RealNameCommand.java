package org.kilocraft.essentials.commands.play;

import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.user.OnlineUser;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RealNameCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("realname")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("realname"), 2)).executes(KiloCommands::executeSmartUsage)
				.then(argument("target", string()).executes(context -> {

					String input = getString(context, "target");
					for (int i = 0; i < context.getSource().getWorld().getPlayers().size(); i++) {
						PlayerEntity player = context.getSource().getWorld().getPlayers().get(i);
						OnlineUser serverUser = KiloServer.getServer().getUserManager().getOnline(player.getUuid());
						if (serverUser.getNickname().isPresent() == true && input.equals(serverUser.getNickname().get()) && input != "") {
							context.getSource().getPlayer().sendMessage(LangText.getFormatter(true,
									"command.realname.success", input, player.getName().asString()));
							return 0;
						}
					}

					context.getSource().getPlayer()
							.sendMessage(LangText.getFormatter(true, "command.realname.error", input));

					return 0;
				})));
	}
}
