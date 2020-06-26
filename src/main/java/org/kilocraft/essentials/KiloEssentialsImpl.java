package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.ServerChat;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.commands.misc.DiscordCommand;
import org.kilocraft.essentials.commands.misc.VoteCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.betterchairs.SeatManager;
import org.kilocraft.essentials.extensions.customcommands.CustomCommands;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.playtimecommands.PlaytimeCommands;
import org.kilocraft.essentials.extensions.warps.playerwarps.PlayerWarpsManager;
import org.kilocraft.essentials.extensions.warps.serverwidewarps.ServerWarpManager;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.PermissionUtil;
import org.kilocraft.essentials.util.StartupScript;
import org.kilocraft.essentials.util.messages.MessageUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;
import org.kilocraft.essentials.votifier.Votifier;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
/**
 * Main Implementation
 *
 * @see KiloEssentials
 * @author ItsIlya
 * @author MCRafterzz
 * @author I509VCB
 * @since KE 1.6
 */

public final class KiloEssentialsImpl implements KiloEssentials {
	public static boolean running = false;
	private static final Logger LOGGER = LogManager.getLogger("KiloEssentials");
	private static KiloEssentialsImpl instance;

    private PermissionUtil permUtil;
	private StartupScript startupScript;

	public static CommandDispatcher<ServerCommandSource> commandDispatcher;

	public static void onServerSet(final Server server) {
		KiloDebugUtils.validateDebugMode(false);
        try {
            getServer().getUserManager().getMutedPlayerList().load();
        } catch (IOException e) {
            KiloEssentials.getLogger().error("An unexpected error occurred while loading the Muted Player List", e);
        }
        new KiloEvents();
        if(KiloConfig.main().votifier().enabled)
        new Votifier().onEnable();
    }



	public KiloEssentialsImpl() {
        if (running) {
            throw new RuntimeException("KiloEssentialsImpl is already running!");
        } else {
            running = true;
        }
        KiloEssentialsImpl.instance = this;
        KiloEssentialsImpl.LOGGER.info("Running KiloEssentials version " + ModConstants.getVersion());

		if (SharedConstants.isDevelopment) {
			new KiloDebugUtils();
		}
		ServerChat.load();
		if (KiloConfig.main().startupScript().enabled) {
			this.startupScript = new StartupScript();
		}
		this.permUtil = new PermissionUtil();
	}

	public static Logger getLogger() {
		return KiloEssentialsImpl.LOGGER;
	}

	public static boolean hasPermissionNode(final ServerCommandSource source, final EssentialPermission perm) {
		return instance.permUtil.hasPermission(source, perm.getNode(), 2);
	}

	public static boolean hasPermissionNode(final ServerCommandSource source, final EssentialPermission perm, final int minOpLevel) {
		return instance.permUtil.hasPermission(source, perm.getNode(), minOpLevel);
	}

	@Override
	public MessageUtil getMessageUtil() {
		return ModConstants.getMessageUtil();
	}

	public static KiloEssentialsImpl getInstance() {
		if (KiloEssentialsImpl.instance != null) {
			return KiloEssentialsImpl.instance;
		}
		throw new RuntimeException("Its too early to get a static instance of KiloEssentials!");
    }

	private static String featureEntry(final String name) {
		return "kiloess:" + name;
	}

	public static Server getServer() {
	    return KiloServer.getServer();
    }

	@Override
	public KiloCommands getCommandHandler() {
	    return KiloCommands.getInstance();
//		return this.commands;
	}

	@Override
	public StartupScript getStartupScript() {
		return this.startupScript;
	}

	@Override
	public final CompletableFuture<List<User>> getAllUsersThenAcceptAsync(final OnlineUser requester,
																		  final String loadingTitle,
																		  final Consumer<? super List<User>> action) {
		CommandSourceUser src = getServer().getCommandSourceUser(requester.getCommandSource());
		final ServerUserManager.LoadingText loadingText = new ServerUserManager.LoadingText(requester.asPlayer(), loadingTitle);

		if (!src.isConsole()) {
			loadingText.start();
		}

		final CompletableFuture<List<User>> future = KiloEssentialsImpl.getServer().getUserManager().getAll();
		future.thenAcceptAsync(list -> {
			if (!src.isConsole()) {
				loadingText.stop();
			}

			try {
				action.accept(list);
			} catch (Exception e) {
				requester.sendError(e.getMessage());
			}
		});

		return future;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final ServerCommandSource requester,
																	final String username,
																	final Consumer<? super User> action) {
		if (CommandUtils.isOnline(requester)) {
			return this.getUserThenAcceptAsync(KiloEssentialsImpl.getServer().getOnlineUser(requester.getName()), username, action);
		}

		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
			if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
				KiloEssentialsImpl.getServer().getCommandSourceUser(requester).sendError(ExceptionMessageNode.USER_NOT_FOUND);
				return;
			}

			try {
				optionalUser.ifPresent(action);
			} catch (Exception e) {
				requester.sendError(new LiteralText(e.getMessage()).formatted(Formatting.RED));
			}
		}, KiloServer.getServer().getMinecraftServer());

		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final ServerPlayerEntity requester,
																	final String username,
																	final Consumer<? super User> action) {
		return this.getUserThenAcceptAsync(KiloEssentialsImpl.getServer().getOnlineUser(requester), username, action);
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final OnlineUser requester,
																	final String username,
																	final Consumer<? super User> action) {
		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(username);
		final ServerUserManager.LoadingText loadingText = new ServerUserManager.LoadingText(requester.asPlayer());
		optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
			loadingText.stop();

			if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
				requester.sendError(ExceptionMessageNode.USER_NOT_FOUND);
				return;
			}

			try {
				action.accept(optionalUser.get());
			} catch (Exception e) {
				requester.sendError(e.getMessage());
			}
		}, KiloServer.getServer().getMinecraftServer());

		if (!optionalCompletableFuture.isDone())
			loadingText.start();

		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final OnlineUser requester,
																	final UUID uuid,
																	final Consumer<? super User> action) {

		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(uuid);
		final ServerUserManager.LoadingText loadingText = new ServerUserManager.LoadingText(requester.asPlayer());
		optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
			loadingText.stop();

			if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
				requester.sendError(ExceptionMessageNode.USER_NOT_FOUND);
				return;
			}

			try {
				action.accept(optionalUser.get());
			} catch (Exception e) {
				requester.sendError(e.getMessage());
			}
		}, KiloServer.getServer().getMinecraftServer());

		if (!optionalCompletableFuture.isDone())
			loadingText.start();

		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final String username,
																	final Consumer<? super Optional<User>> action) {
		if (getServer().getUserManager().getOnline(username) != null) {
			return CompletableFuture.completedFuture(Optional.ofNullable(getServer().getUserManager().getOnline(username)));
		}

		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(action);
		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(UUID uuid, Consumer<? super Optional<User>> action) {
		if (getServer().getUserManager().getOnline(uuid) != null) {
			return CompletableFuture.completedFuture(Optional.ofNullable(getServer().getUserManager().getOnline(uuid)));
		}

		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(uuid);
		optionalCompletableFuture.thenAcceptAsync(action);
		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(final String username, final Consumer<? super Optional<User>> action, final Executor executor) {
		if (getServer().getUserManager().getOnline(username) != null) {
			return CompletableFuture.completedFuture(Optional.ofNullable(getServer().getUserManager().getOnline(username)));
		}

		final CompletableFuture<Optional<User>> optionalCompletableFuture = KiloEssentialsImpl.getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(action, executor);
		return optionalCompletableFuture;
	}

	@Override
	public PermissionUtil getPermissionUtil() {
		return this.permUtil;
	}

	@Override
	public ConfigurableFeatures getFeatures() {
		return ConfigurableFeatures.getInstance();
	}

	public void onServerStop() {
		if (SeatManager.isEnabled()) {
			SeatManager.getInstance().killAll();
		}
	}

	public void onServerLoad() {
	    new KiloCommands();
		this.permUtil = new PermissionUtil();
	}

}
