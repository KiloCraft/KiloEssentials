package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.feature.*;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
/**
 * Main Implementation
 *
 * @see KiloEssentials
 * @author CODY_AI
 * @author MCRafterzz
 * @author I509VCB
 * @since KE 1.6
 */

public final class KiloEssentialsImpl implements KiloEssentials {
	private static final Logger LOGGER = LogManager.getLogger("KiloEssentials");
	private static final ModConstants constants = new ModConstants();
	public static final String PERMISSION_PREFIX = "kiloessentials.";
	private static KiloEssentialsImpl instance;
	private static KiloCommands commandHandler;
	private PermissionUtil permUtil;
	private final ConfigurableFeatures FEATURES;
	private final List<FeatureType<?>> configurableFeatureRegistry = new ArrayList<>();
	private final Map<FeatureType<?>, ConfigurableFeature> proxyFeatureList = new HashMap<>();
	private StartupScript startupScript;

	private final List<FeatureType<SingleInstanceConfigurableFeature>> singleInstanceConfigurationRegistry = new ArrayList<>();
	private final Map<FeatureType<? extends SingleInstanceConfigurableFeature>, SingleInstanceConfigurableFeature> proxySingleInstanceFeatures = new HashMap<>();

	public KiloEssentialsImpl(final KiloEvents events) {
		KiloEssentialsImpl.instance = this;
		KiloEssentialsImpl.LOGGER.info("Running KiloEssentials version " + ModConstants.getVersion());

		// ConfigDataFixer.getInstance(); // i509VCB: TODO Uncomment when I finish DataFixers.

		/*
		// TODO i509VCB: Uncomment when new feature system is done
		FeatureTypes.init(); // Register the built in feature types

		for(KiloHook hook : FabricLoader.getInstance().getEntrypoints(featureEntry("hook"), KiloHook.class)) { // Allow registration of extra features to be done via entrypoint.
			hook.hook(this);
		}

		for(FeatureType<?> type : configurableFeatureRegistry) {
			List<ConfigurableFeature> entrypoints = FabricLoader.getInstance().getEntrypoints(featureEntry(type.getId()), ConfigurableFeature.class);
			if(entrypoints.size()>1) {
				// We don't allow more than one entry point per feature type.
				continue;
			}

			for(ConfigurableFeature feature : entrypoints) {
				if(feature.getClass().isAssignableFrom(type.getType())) { // We check if it's the right type, otherwise don't load it.
					if (!feature.register()) { // Config checks should be in register
						continue;
					}
					this.proxyFeatureList.put(type, feature);
				} else {
					logger.error("Mismatched type: " + type.getId());
				}
			}
		}
		*/

		if (SharedConstants.isDevelopment) {
			new KiloDebugUtils();
		}

		ServerChat.load();

		FEATURES = new ConfigurableFeatures();
		FEATURES.tryToRegister(new UserHomeHandler(), "playerHomes");
		FEATURES.tryToRegister(new ServerWarpManager(), "serverWideWarps");
		FEATURES.tryToRegister(new PlayerWarpsManager(), "playerWarps");
		FEATURES.tryToRegister(new SeatManager(), "betterChairs");
		FEATURES.tryToRegister(new CustomCommands(), "customCommands");
		FEATURES.tryToRegister(new ParticleAnimationManager(), "magicalParticles");
		FEATURES.tryToRegister(new DiscordCommand(), "discordCommand");
		FEATURES.tryToRegister(new VoteCommand(), "voteCommand");
		FEATURES.tryToRegister(new PlaytimeCommands(), "playtimeCommands");

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
		return commandHandler;
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
	public <F extends ConfigurableFeature> FeatureType<F> registerFeature(final FeatureType<F> featureType) {
		if(featureType.getType().isAssignableFrom(SingleInstanceConfigurableFeature.class)) {
			this.singleInstanceConfigurationRegistry.add((FeatureType<SingleInstanceConfigurableFeature>) featureType);
			return featureType;
		}

		this.configurableFeatureRegistry.add(featureType);
		return featureType;
	}

	@Override
	public <F extends SingleInstanceConfigurableFeature> F getFeature(final FeatureType<F> type) throws FeatureNotPresentException {
		final F ft = (F) this.proxySingleInstanceFeatures.get(type);

		if (ft == null) {
			throw new FeatureNotPresentException();
		}

		return ft;
	}

	@Override
	public ConfigurableFeatures getFeatures() {
		return this.FEATURES;
	}

	@Override
	public PermissionUtil getPermissionUtil() {
		return this.permUtil;
	}

	public void onServerStop() {
		if (SeatManager.isEnabled()) {
			SeatManager.getInstance().killAll();
		}
	}

	public void onServerLoad() {
		this.permUtil = new PermissionUtil();
	}

	public static void firstInitialization(@NotNull final CommandDispatcher<ServerCommandSource> dispatcher) {
		new ModConstants().loadConstants();
		KiloConfig.load();

		if (commandHandler == null) {
			commandHandler = new KiloCommands(dispatcher);
		}
	}

}
