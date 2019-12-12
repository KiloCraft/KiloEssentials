package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BackCommand {

	public static HashMap<UUID, Vector3f> backLocations = new HashMap<>();
	public static HashMap<UUID, DimensionType> backDimensions = new HashMap<>();

	public static void setLocation(ServerPlayerEntity player, Vector3f position, DimensionType dimension) {
		if (backLocations.containsKey(player.getUuid())) {
			backLocations.replace(player.getUuid(), position);
			backDimensions.replace(player.getUuid(), dimension);
		} else {
			backLocations.put(player.getUuid(), position);
			backDimensions.put(player.getUuid(), dimension);
		}
		
		OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);
		user.setBackPos(new BlockPos(player));
		user.setBackDim(DimensionType.getId(dimension));
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = literal("back")
				.requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("back.self"), 2))
				.executes(c -> goBack(c.getSource().getPlayer()))
				.then(argument("player", player())
						.requires(
								s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("back.others"), 2))
						.suggests(TabCompletions::allPlayers)
						.executes(c -> goBack(getPlayer(c, "player"))));

		dispatcher.register(argumentBuilder);
	}

	public static int goBack(ServerPlayerEntity player) {
		if (backLocations.containsKey(player)) {
			DimensionType dimension = backDimensions.get(player);
			ServerWorld world = player.getServer().getWorld(dimension);
			player.teleport(world, backLocations.get(player).getX(), backLocations.get(player).getY(),
					backLocations.get(player).getZ(), 90, 0);
			backLocations.remove(player);
			backDimensions.remove(player);
			player.sendMessage(
					LangText.getFormatter(true, "command.back.success", player.getName().asFormattedString()));
			
			OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);
			user.setBackPos((Vec3d)null);
			user.setBackDim(null);
		} else {
			player.sendMessage(LangText.get(true, "command.back.failture"));
		}
		return 0;
	}

	public static void saveLocation(OnlineUser user) {
		backLocations.put(user.getUuid(), new Vector3f(Objects.requireNonNull(user.getBackPos())));
		backDimensions.put(user.getUuid(), DimensionType.byId(user.getBackDimId()));
	}

	public static void saveLocation(ServerPlayerEntity player) {
		saveLocation(KiloServer.getServer().getOnlineUser(player));
	}

}
