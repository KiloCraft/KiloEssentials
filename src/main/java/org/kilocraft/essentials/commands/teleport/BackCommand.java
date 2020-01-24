package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.HashMap;
import java.util.UUID;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class BackCommand extends EssentialCommand {
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

	public BackCommand() {
		super("back", CommandPermission.BACK_SELF);
	}

	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArgument = argument("player", player())
				.requires(src -> KiloCommands.hasPermission(src, CommandPermission.BACK_OTHERS))
				.suggests(TabCompletions::allPlayers)
				.executes(c -> goBack(getPlayer(c, "player")));

		argumentBuilder.executes(ctx -> goBack(ctx.getSource().getPlayer()));
	}

	public static int goBack(ServerPlayerEntity player) {
		if (backLocations.containsKey(player.getUuid())) {
			DimensionType dimension = backDimensions.get(player.getUuid());
			ServerWorld world = player.getServer().getWorld(dimension);
			player.teleport(world, backLocations.get(player.getUuid()).getX(), backLocations.get(player.getUuid()).getY(),
					backLocations.get(player.getUuid()).getZ(), 90, 0);
			backLocations.remove(player.getUuid());
			backDimensions.remove(player.getUuid());
			KiloChat.sendLangMessageTo(player, "command.back.success", player.getName().asFormattedString());
			
			OnlineUser user = KiloServer.getServer().getUserManager().getOnline(player);
			user.setBackPos((Vec3d)null);
			user.setBackDim(null);
		} else
			KiloChat.sendLangMessageTo(player, "command.back.failture");

		return 0;
	}

	public static void saveLocation(OnlineUser user) {
		if (user.getPlayer() == null)
			return;

		backLocations.put(user.getUuid(), new Vector3f(user.getPlayer().getPos()));
		backDimensions.put(user.getUuid(), DimensionType.byId(user.getBackDimId()));
	}

	public static void saveLocation(ServerPlayerEntity player) {
		saveLocation(KiloServer.getServer().getOnlineUser(player));
	}

}
