package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.SimpleMessage;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.*;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.NeverJoinedUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.channels.BuilderChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.chat.channels.StaffChat;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.commands.misc.DiscordCommand;
import org.kilocraft.essentials.commands.misc.VoteCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.events.server.ServerScheduledUpdateEventImpl;
import org.kilocraft.essentials.extensions.betterchairs.PlayerSitManager;
import org.kilocraft.essentials.extensions.magicalparticles.ParticleAnimationManager;
import org.kilocraft.essentials.extensions.warps.WarpManager;
import org.kilocraft.essentials.user.ServerUserManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.StartupScript;
import org.kilocraft.essentials.util.messages.MessageUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static io.github.indicode.fabric.permissions.Thimble.hasPermissionOrOp;
import static io.github.indicode.fabric.permissions.Thimble.permissionWriters;

/**
 * Main Implementation
 *
 * @see org.kilocraft.essentials.api.KiloEssentials
 * @author CODY_AI
 * @author MCRafterzz
 * @author GiantNuker
 * @author I509VCB
 * @since KE 1.6
 */

public class KiloEssentialsImpl implements KiloEssentials {
	public static CommandDispatcher<ServerCommandSource> commandDispatcher;
	private static String KE_PREFIX = "[KiloEssentials] ";
	private static final Logger logger = LogManager.getLogger("KiloEssentials", massageFactory());
	private static KiloEssentialsImpl instance;
	private static ModConstants constants = new ModConstants();
	public static String PERMISSION_PREFIX = "kiloessentials.";
	private KiloCommands commands;
	private List<FeatureType<?>> configurableFeatureRegistry = new ArrayList<>();
	private Map<FeatureType<?>, ConfigurableFeature> proxyFeatureList = new HashMap<>();
	private ScheduledExecutorService scheduledUpdateExecutorService;
	private KiloDebugUtils debugUtils;
	private ScheduledThreadPoolExecutor executor;
	private static MinecraftServer minecraftServer;

	private List<FeatureType<SingleInstanceConfigurableFeature>> singleInstanceConfigurationRegistry = new ArrayList<>();
	private Map<FeatureType<? extends SingleInstanceConfigurableFeature>, SingleInstanceConfigurableFeature> proxySingleInstanceFeatures = new HashMap<>();

	public KiloEssentialsImpl(KiloEvents events, KiloConfig config) {
		instance = this;
		logger.info("Running KiloEssentials version " + ModConstants.getVersion());
		this.executor = new ScheduledThreadPoolExecutor(4);
		minecraftServer = KiloServer.getServer().getVanillaServer();

		// ConfigDataFixer.getInstance(); // i509VCB: TODO Uncomment when I finish DataFixers.
		this.commands = new KiloCommands();

		KiloServer.getServer().setName(KiloConfig.getProvider().getMessages().getStringSafely("server.name", "Minecraft Server"));

		permissionWriters.add((map, server) -> {
			for (EssentialPermission perm : EssentialPermission.values()) {
				map.registerPermission(perm.getNode(), PermChangeBehavior.UPDATE_COMMAND_TREE);
			}

			for (int i = 1; i <= KiloConfig.getProvider().getMain().getIntegerSafely("homes.limit", 20); i++) {
				map.registerPermission(CommandPermission.HOME_LIMIT.getNode() + "." + i, PermChangeBehavior.UPDATE_COMMAND_TREE);
			}
		});

		logger.info("Registered " + (CommandPermission.values().length + EssentialPermission.values().length) + " permission nodes.");

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

		if (SharedConstants.isDevelopment)
			this.debugUtils = new KiloDebugUtils(this);

		getServer().getChatManager().register(new GlobalChat());
		getServer().getChatManager().register(new StaffChat());
		getServer().getChatManager().register(new BuilderChat());

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new UserHomeHandler(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");
		features.tryToRegister(new PlayerSitManager(), "BetterChairs");
		features.tryToRegister(new DiscordCommand(), "DiscordCommand");
		features.tryToRegister(new VoteCommand(), "VoteCommand");
		features.tryToRegister(new ParticleAnimationManager(), "MagicalParticles");

		if (KiloConfig.getProvider().getMain().getBooleanSafely("startup-script.auto-generate", true))
			new StartupScript();

		/*
		 * @Test TODO: Remove this Test
		 */

//		{
//			FileConfig fileConfig = FileConfig.of(KiloConfig.getConfigPath() + "particle_types.yml");
//			fileConfig.load();
//
////			System.out.println((ArrayList<Object>) fileConfig.get("types"));
//
//			ArrayList<Object> objects = fileConfig.get("types");
//
//
//			for (Object object : objects) {
//				System.out.println("ML: " + object.toString());
//
//				List<Object> innerObject = fileConfig.get("types." + object.toString());
//				System.out.println("IL: " + innerObject);
//			}
//
//			fileConfig.close();
//		}
//		{
//			System.out.println("Registering particle types");
//			ParticleFrame frame = new ParticleFrame(ParticleTypes.DRAGON_BREATH, true,
//					0.5, 0f, 0.5f, 0, 10);
//
//			ParticleAnimation animation = new ParticleAnimation(new Identifier("kiloessentials", "breath_of_dragon"));
//			animation.append(frame);
//			ParticleAnimationManager.registerAnimation(animation);
//		}
//		{
//			ParticleFrame frame = new ParticleFrame(ParticleTypes.CLOUD, true,
//					0.4, 0, 0.4, 0, 3);
//
//			ParticleAnimation animation = new ParticleAnimation(new Identifier("ess", "happy_villager"));
//			animation.append(frame);
//			ParticleAnimationManager.registerAnimation(animation);
//		}

	}

	public static Logger getLogger() {
		return logger;
	}

	public static boolean hasPermissionNode(ServerCommandSource source, EssentialPermission perm) {
		return hasPermissionOrOp(source, perm.getNode(), 2);
	}

	public static boolean hasPermissionNode(ServerCommandSource source, EssentialPermission perm, int minOpLevel) {
		return hasPermissionOrOp(source, perm.getNode(), minOpLevel);
	}

	@Override
	public MessageUtil getMessageUtil() {
		return ModConstants.getMessageUtil();
	}

	public static KiloEssentialsImpl getInstance() {
		if (instance == null)
			throw new RuntimeException("Its too early to get a static instance of KiloEssentials!");

		return instance;
    }

	private static String featureEntry(String name) {
		return "kiloess:" + name;
	}

	public Server getServer() {
	    return KiloServer.getServer();
    }

	@Override
	public ModConstants getConstants() {
		return constants;
	}

	@Override
	public KiloCommands getCommandHandler() {
		return this.commands;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(ServerCommandSource requester, String username, Consumer<? super User> action) {
		if (CommandHelper.isOnline(requester))
			return getUserThenAcceptAsync(getServer().getOnlineUser(requester.getName()), username, action);

		CompletableFuture<Optional<User>> optionalCompletableFuture = getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
			if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
				getServer().getCommandSourceUser(requester).sendError(ExceptionMessageNode.USER_NOT_FOUND);
				return;
			}

			optionalUser.ifPresent(action);
		}, minecraftServer);

		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(ServerPlayerEntity requester, String username, Consumer<? super User> action) {
		return getUserThenAcceptAsync(getServer().getOnlineUser(requester), username, action);
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(OnlineUser requester, String username, Consumer<? super User> action) {
		CompletableFuture<Optional<User>> optionalCompletableFuture = getServer().getUserManager().getOffline(username);
		ServerUserManager.UserLoadingText loadingText = new ServerUserManager.UserLoadingText(requester.getPlayer());
		optionalCompletableFuture.thenAcceptAsync(optionalUser -> {
			if (!optionalUser.isPresent() || optionalUser.get() instanceof NeverJoinedUser) {
				requester.sendError(ExceptionMessageNode.USER_NOT_FOUND);
				loadingText.stop();
			}

			optionalUser.ifPresent(action);
			loadingText.stop();
		}, minecraftServer);

		if (!optionalCompletableFuture.isCompletedExceptionally())
			loadingText.start();

		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(String username, Consumer<? super Optional<User>> action) {
		CompletableFuture<Optional<User>> optionalCompletableFuture = getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(action);
		return optionalCompletableFuture;
	}

	@Override
	public CompletableFuture<Optional<User>> getUserThenAcceptAsync(String username, Consumer<? super Optional<User>> action, Executor executor) {
		CompletableFuture<Optional<User>> optionalCompletableFuture = getServer().getUserManager().getOffline(username);
		optionalCompletableFuture.thenAcceptAsync(action, executor);
		return optionalCompletableFuture;
	}


	public <F extends ConfigurableFeature> FeatureType<F> registerFeature(FeatureType<F> featureType) {
		if(featureType.getType().isAssignableFrom(SingleInstanceConfigurableFeature.class)) {
			singleInstanceConfigurationRegistry.add((FeatureType<SingleInstanceConfigurableFeature>) featureType);
			return featureType;
		}

		configurableFeatureRegistry.add(featureType);
		return featureType;
	}

	public <F extends SingleInstanceConfigurableFeature> F getFeature(FeatureType<F> type) throws FeatureNotPresentException {
		F ft = (F) proxySingleInstanceFeatures.get(type);

		if(ft == null) {
			throw new FeatureNotPresentException();
		}

		return ft;
	}

	private static MessageFactory massageFactory() {
		return new MessageFactory() {
			@Override
			public Message newMessage(Object message) {
				return new SimpleMessage(KE_PREFIX + message);
			}

			@Override
			public Message newMessage(String message) {
				return new SimpleMessage(KE_PREFIX + message);
			}

			@Override
			public Message newMessage(String message, Object... params) {
				return new SimpleMessage(message);
			}
		};
	}

	public void onServerReady() {
		this.scheduledUpdateExecutorService = Executors.newSingleThreadScheduledExecutor();
		scheduledUpdateExecutorService.scheduleAtFixedRate(() ->
				KiloServer.getServer().triggerEvent(new ServerScheduledUpdateEventImpl()), 6, 6, TimeUnit.SECONDS);
	}

	public void onServerStop() {
		if (PlayerSitManager.enabled)
			PlayerSitManager.INSTANCE.killAll();

		this.scheduledUpdateExecutorService.shutdown();
	}

	public KiloDebugUtils getDebugUtils() {
		return this.debugUtils;
	}

}
