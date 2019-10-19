package org.kilocraft.essentials.craft.commands.essentials;

import org.kilocraft.essentials.api.chat.LangText;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.craft.KiloCommands;

public class RealNameCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		KiloCommands.getCommandPermission("realname");
		dispatcher.register(CommandManager.literal("realname")
				.requires(s -> Thimble.hasPermissionChildOrOp(s, KiloCommands.getCommandPermission("realname"), 2))
				.then(CommandManager.argument("target", StringArgumentType.string())).executes(context -> {
					String input = StringArgumentType.getString(context, "target");

					for (int i = 0; i < context.getSource().getWorld().getPlayers().size(); i++) {
						PlayerEntity player = context.getSource().getWorld().getPlayers().get(i);

						// TODO: check nick
						context.getSource().getPlayer().sendMessage(LangText.getFormatter(true,
								"command.realname.success", input, player.getName().asString()));
					}

					context.getSource().getPlayer()
							.sendMessage(LangText.getFormatter(true, "command.realname.error", input));

					return 0;
				}));
	}
}
