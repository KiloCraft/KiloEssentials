package org.kilocraft.essentials.commands.play;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.ServerCommandSource;

import net.minecraft.server.network.ServerPlayerEntity;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.command.TabCompletions;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.commands.CommandHelper;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.string;

import java.util.ArrayList;
import java.util.function.Predicate;

import static net.minecraft.command.arguments.EntityArgumentType.getPlayer;
import static net.minecraft.command.arguments.EntityArgumentType.player;

public class MagicParticlesCommand extends EssentialCommand {

	public static final Predicate<ServerCommandSource> PERMISSION_USE_SELF = (s) -> KiloEssentials.hasPermissionNode(s, EssentialPermission.MAGIC_PARTICLES_SELF);
	public static final Predicate<ServerCommandSource> PERMISSION_USE_OTHERS = (s) -> KiloEssentials.hasPermissionNode(s, EssentialPermission.MAGIC_PARTICLES_OTHERS);
	public static ArrayList<String> particles = new ArrayList<String>(){};

	public MagicParticlesCommand() {
		super("magicparticles", PERMISSION_USE_SELF, new String[]{"mp"});
	}

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		// Add particles to list
		particles.add("flames");
		particles.add("glass");
		particles.add("stormcloud");
		particles.add("dragonbreath");
		particles.add("bonemeal");

		LiteralCommandNode<ServerCommandSource> setNode = literal("set").build();

		ArgumentCommandNode<ServerCommandSource, String> particleNameNode = argument("name", string()).suggests(TabCompletions::allParticles).executes((context) -> {
			return setParticle(context, context.getSource().getPlayer(), getString(context, "name"));
		}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> setTarget = argument("target", player())
				.requires(PERMISSION_USE_OTHERS)
				.suggests(TabCompletions::allPlayers)
				.executes((context) -> {
					return setParticle(context, getPlayer(context, "target"), getString(context, "name"));
				}).build();

		LiteralCommandNode<ServerCommandSource> disableNode = literal("disable").executes((context) -> {
			User serverUser = KiloServer.getServer().getUserManager().getOnline(context.getSource().getPlayer().getUuid());
			serverUser.setDisplayParticleId(0);
			KiloChat.sendMessageTo(context.getSource(), LangText.get(true, "command.playerparticles.disable"));
			return SINGLE_SUCCESS;
		}).build();

		ArgumentCommandNode<ServerCommandSource, EntitySelector> disableTarget = argument("target", player())
				.requires(PERMISSION_USE_OTHERS)
				.suggests(TabCompletions::allPlayers)
				.executes((context) -> {
					User serverUser = KiloServer.getServer().getUserManager().getOnline(getPlayer(context, "target"));
					serverUser.setDisplayParticleId(0);
					KiloChat.sendMessageTo(context.getSource(), LangText.getFormatter(true, "command.playerparticles.disable.other", getPlayer(context, "target").getName().asString()));
					return SINGLE_SUCCESS;
				}).build();

		disableNode.addChild(disableTarget);
		particleNameNode.addChild(setTarget);
		setNode.addChild(particleNameNode);
		commandNode.addChild(disableNode);
		commandNode.addChild(setNode);
	}

	private static int setParticle(CommandContext<ServerCommandSource> context, ServerPlayerEntity player, String name) throws CommandSyntaxException {
		if(!particles.contains(name)) {
			KiloChat.sendMessageTo(context.getSource(), (LangText.getFormatter(true, "command.playerparticles.particlenotfound", name)));
			return SINGLE_FAILED;
		}

		User user = KiloServer.getServer().getUserManager().getOnline(player.getUuid());
		user.setDisplayParticleId(particles.indexOf(name) + 1);

		if (CommandHelper.areTheSame(context.getSource(), player)) {
			KiloChat.sendMessageTo(context.getSource(), LangText.getFormatter(true, "command.playerparticles.particleset", name));
		} else {
			KiloChat.sendMessageTo(context.getSource(), LangText.getFormatter(true, "command.playerparticles.particleset.other", name, getPlayer(context, "target").getName().asString()));
		}
		return SINGLE_SUCCESS;
	}

}
