package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandSuggestions;
import org.kilocraft.essentials.craft.KiloCommands;

import java.util.HashMap;

public class BackCommand {

	public static HashMap<ServerPlayerEntity, Vector3f> backLocations = new HashMap<ServerPlayerEntity, Vector3f>();
	public static HashMap<ServerPlayerEntity, DimensionType> backDimensions = new HashMap<ServerPlayerEntity, DimensionType>();
	
	public static void setLocation(ServerPlayerEntity player, Vector3f position, DimensionType dimension) {
		if (backLocations.containsKey(player)) {
			backLocations.replace(player, position);
			backDimensions.replace(player, dimension);
		} else {
			backLocations.put(player, position);
			backDimensions.put(player, dimension);
		}
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal("back")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("back.self"), 2))
				.executes(c -> goBack(c.getSource().getPlayer()))
				.then(
						CommandManager.argument("player", EntityArgumentType.player())
								.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("back.others"), 2))
								.suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
								.executes(c -> goBack(EntityArgumentType.getPlayer(c, "player")))
				);

		dispatcher.register(argumentBuilder);
	}

	public static int goBack(ServerPlayerEntity player) {
		if (backLocations.containsKey(player)) {
			player.teleport(backLocations.get(player).getX(), backLocations.get(player).getY(),
					backLocations.get(player).getZ());
			player.changeDimension(backDimensions.get(player));
			backLocations.remove(player);
			backDimensions.remove(player);
			player.sendMessage(LangText.getFormatter(true, "command.back.success", player.getName().asFormattedString()));
		} else {
			player.sendMessage(LangText.get(true, "command.back.failture"));
		}
		return 0;
	}

}
