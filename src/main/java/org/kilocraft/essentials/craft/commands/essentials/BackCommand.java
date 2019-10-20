package org.kilocraft.essentials.craft.commands.essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.CommandSuggestions;

import java.util.Collection;
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
		dispatcher.register(CommandManager.literal("back").executes(context -> {
			return goBack(context.getSource().getPlayer());
		}).then(CommandManager.argument("players", EntityArgumentType.players())
				.suggests((context, builder) -> CommandSuggestions.allPlayers.getSuggestions(context, builder))
				.executes(context -> goBack(EntityArgumentType.getPlayers(context, "players"))))
		);
	}

	private static int goBack(Collection<ServerPlayerEntity> players) {
		for (int i = 0; i < players.size(); i++) {
			goBack((ServerPlayerEntity) players.toArray()[i]);
		}
		return 0;
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
