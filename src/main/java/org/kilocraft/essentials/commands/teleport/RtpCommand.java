package org.kilocraft.essentials.commands.teleport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biome.Category;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.ChatMessage;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.provided.LocateBiomeProvided;
import org.kilocraft.essentials.util.messages.nodes.ArgExceptionMessageNode;

import java.util.Random;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RtpCommand {
	private static Predicate<ServerCommandSource> PERMISSION_CHECK_SELF = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_SELF);
	private static Predicate<ServerCommandSource> PERMISSION_CHECK_OTHERS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERS);
	private static Predicate<ServerCommandSource> PERMISSION_CHECK_IGNORE_LIMIT = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_BYPASS);
	private static Predicate<ServerCommandSource> PERMISSION_CHECK_OTHER_DIMENSIONS = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_OTHERDIMENSIONS);
	private static Predicate<ServerCommandSource> PERMISSION_CHECK_MANAGE = (src) -> KiloEssentials.hasPermissionNode(src, EssentialPermission.RTP_MANAGE);

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> rootCommand = literal("randomteleport").requires(PERMISSION_CHECK_SELF).executes(RtpCommand::executeSelf).build();

		RequiredArgumentBuilder<ServerCommandSource, EntitySelector> selectorArg = argument("target", player())
				.requires(PERMISSION_CHECK_OTHERS)
				.suggests(TabCompletions::allPlayers)
				.executes(RtpCommand::executeOthers);

		LiteralArgumentBuilder<ServerCommandSource> leftArg = literal("-left")
				.requires(PERMISSION_CHECK_SELF)
				.executes(RtpCommand::executeLeft);

		LiteralArgumentBuilder<ServerCommandSource> addArg = literal("add")
				.requires(PERMISSION_CHECK_MANAGE)
				.then(argument("amount", integer(1))
						.executes(RtpCommand::executeAdd));

		LiteralArgumentBuilder<ServerCommandSource> setArg = literal("set")
				.requires(PERMISSION_CHECK_MANAGE)
				.then(argument("amount", integer(0))
						.executes(RtpCommand::executeSet));

		LiteralArgumentBuilder<ServerCommandSource> getArg = literal("get")
				.requires(PERMISSION_CHECK_MANAGE)
				.executes(RtpCommand::executeGet);

		LiteralArgumentBuilder<ServerCommandSource> removeArg = literal("remove")
				.requires(PERMISSION_CHECK_MANAGE)
				.then(argument("amount", integer(0))
						.executes(RtpCommand::executeRemove));

		selectorArg.then(addArg);
		selectorArg.then(setArg);
		selectorArg.then(getArg);
		selectorArg.then(removeArg);
		rootCommand.addChild(leftArg.build());
		rootCommand.addChild(selectorArg.build());
		dispatcher.getRoot().addChild(literal("rtp").requires(PERMISSION_CHECK_SELF).executes(RtpCommand::executeSelf).redirect(rootCommand).build());
		dispatcher.getRoot().addChild(literal("wilderness").requires(PERMISSION_CHECK_SELF).executes(RtpCommand::executeSelf).redirect(rootCommand).build());
		dispatcher.getRoot().addChild(literal("wild").requires(PERMISSION_CHECK_SELF).executes(RtpCommand::executeSelf).redirect(rootCommand).build());

		dispatcher.getRoot().addChild(rootCommand);
	}

	private static int executeLeft(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		OnlineUser user = KiloServer.getServer().getOnlineUser(ctx.getSource().getPlayer());
		KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
				.sendLangMessage("command.rtp.get", user.getDisplayname(), user.getRTPsLeft());

		return user.getRTPsLeft();
	}

	private static int executeAdd(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
		int amountToAdd = getInteger(ctx, "amount");
		user.setRTPsLeft(user.getRTPsLeft() + amountToAdd);
		KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
				.sendLangMessage("template.#1", "RTPs left", user.getRTPsLeft(), user.getDisplayname());

		return user.getRTPsLeft();
	}

	private static int executeSet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
		int amountToSet = getInteger(ctx, "amount");
		user.setRTPsLeft(amountToSet);
		KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
				.sendLangMessage("template.#1", "RTPs left", user.getRTPsLeft(), user.getDisplayname());

		return user.getRTPsLeft();
	}

	private static int executeGet(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
		KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
				.sendLangMessage("command.rtp.get", user.getDisplayname(), user.getRTPsLeft());

		return user.getRTPsLeft();
	}

	private static int executeRemove(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		OnlineUser user = KiloServer.getServer().getOnlineUser(getPlayer(ctx, "target"));
		int amountToRemove = getInteger(ctx, "amount");

		if ((user.getRTPsLeft() - amountToRemove) < 0)
			throw KiloCommands.getArgException(ArgExceptionMessageNode.NO_NEGATIVE_VALUES).create();

		user.setRTPsLeft(user.getRTPsLeft() - amountToRemove);
		KiloEssentials.getServer().getCommandSourceUser(ctx.getSource())
				.sendLangMessage("template.#1", "RTPs left", user.getRTPsLeft(), user.getDisplayname());

		return user.getRTPsLeft();
	}

	private static int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return execute(ctx.getSource(), ctx.getSource().getPlayer());
	}

	private static int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		return execute(ctx.getSource(), getPlayer(ctx, "target"));
	}

	private static int execute(ServerCommandSource source, ServerPlayerEntity target) {
		KiloServer.getServer().getOnlineUser(target).sendConfigMessage("commands.rtp.start");

		RandomTeleportThread rtp = new RandomTeleportThread(source, target);
		Thread rtpThread = new Thread(rtp ,"RTP thread");
		rtpThread.start();
		return 1;
	}

	//TODO: Update this
	static void teleportRandomly(ServerCommandSource source, ServerPlayerEntity target) {
		OnlineUser targetUser = KiloServer.getServer().getOnlineUser(target.getUuid());
		CommandSourceUser sourceUser = KiloEssentials.getServer().getCommandSourceUser(source);

		if (targetUser.getRTPsLeft() < 0)
			targetUser.setRTPsLeft(0);

		//Check if the player has any rtps left or permission to ignore the limit
		if (CommandHelper.areTheSame(source, target) && targetUser.getRTPsLeft() <= 0 && !PERMISSION_CHECK_IGNORE_LIMIT.test(source)) {
			targetUser.sendConfigMessage("commands.rtp.empty");
			return;
		}

		//Check if the target is in the correct dimension or has permission to perform the command in other dimensions
		if (!target.dimension.equals(DimensionType.OVERWORLD) && !PERMISSION_CHECK_OTHER_DIMENSIONS.test(source)) {
			targetUser.sendConfigMessage("commands.rtp.dimension_exception");
			return;
		}

		//Generate random coordinates
		Random random = new Random();
		int randomX = random.nextInt(30000) - 15000; // -15000 to +15000
		int randomZ = random.nextInt(30000) - 15000; // -15000 to  +15000

		Biome.Category biomeCategory = target.world.getBiomeAccess().getBiome(new BlockPos(randomX, 65, randomZ)).getCategory();

		if (biomeCategory == Category.OCEAN || biomeCategory == Category.RIVER) {
			teleportRandomly(source, target);
			return;
		}

		target.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, 600, 255, false, false, false));
		target.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 600, 255, false, false, false));

		BackCommand.saveLocation(targetUser);
		target.teleport(target.getServerWorld(), randomX, 255, randomZ, 0, 0);

		String targetBiomeName = LocateBiomeProvided.getBiomeName(target.getServerWorld().getBiome(target.getBlockPos()));

		if (CommandHelper.areTheSame(source, target)) {
			if (!PERMISSION_CHECK_IGNORE_LIMIT.test(source))
				targetUser.setRTPsLeft(targetUser.getRTPsLeft() - 1);

			targetUser.sendMessage(new ChatMessage(
					KiloConfig.getProvider().getMessages().getMessage("commands.rtp.teleported")
							.replace("{BIOME}", targetBiomeName)
							.replace("{RTP_LEFT}", String.valueOf(targetUser.getRTPsLeft()))
							.replace("{cord.X}", String.valueOf(randomX))
							.replace("{cord.Y}", String.valueOf(target.getBlockPos().getY()))
							.replace("{cord.Z}", String.valueOf(randomZ))
					, true));
		} else
			sourceUser.sendLangMessage("command.rtp.others", targetUser.getUsername(), targetBiomeName);

		Thread.currentThread().interrupt();
	}

}

class RandomTeleportThread implements Runnable {
	private Logger logger = LogManager.getLogger();
	private ServerCommandSource source;
	private ServerPlayerEntity target;

	public RandomTeleportThread(ServerCommandSource source, ServerPlayerEntity target) {
		this.source = source;
		this.target = target;
	}

	@Override
	public void run() {
		logger.info("Randomly teleporting " + target.getEntityName() + ". executed by " + source.getName());
		RtpCommand.teleportRandomly(this.source, this.target);
	}
}
