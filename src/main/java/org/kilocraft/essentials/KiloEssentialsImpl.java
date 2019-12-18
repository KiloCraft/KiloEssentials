package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import net.minecraft.server.command.ServerCommandSource;
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
import org.kilocraft.essentials.chat.channels.BuilderChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.chat.channels.StaffChat;
import org.kilocraft.essentials.commands.misc.DiscordCommand;
import org.kilocraft.essentials.commands.misc.VoteCommand;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.WarpManager;
import org.kilocraft.essentials.modsupport.BungeecordSupport;
import org.kilocraft.essentials.modsupport.ModSupport;
import org.kilocraft.essentials.modsupport.VanishModSupport;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.StartupScript;
import org.kilocraft.essentials.util.messages.MessageUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * @author DrexHD
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

	private List<FeatureType<SingleInstanceConfigurableFeature>> singleInstanceConfigurationRegistry = new ArrayList<>();
	private Map<FeatureType<? extends SingleInstanceConfigurableFeature>, SingleInstanceConfigurableFeature> proxySingleInstanceFeatures = new HashMap<>();

	public KiloEssentialsImpl(KiloEvents events, KiloConfig config ,KiloCommands commands) {
		instance = this;
		logger.info("Running KiloEssentials version " + ModConstants.getVersion());

		// ConfigDataFixer.getInstance(); // i509VCB: TODO Uncomment when I finish DataFixers.
		this.commands = commands;

		KiloServer.getServer().setName(KiloConfig.getProvider().getMessages().getStringSafely("server.name", "Minecraft Server"));

		permissionWriters.add((map, server) -> {
			for (EssentialPermission perm : EssentialPermission.values()) {
				map.registerPermission(perm.getNode(), PermChangeBehavior.UPDATE_COMMAND_TREE);
			}
		});

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

		getServer().getChatManager().register(new GlobalChat());
		getServer().getChatManager().register(new StaffChat());
		getServer().getChatManager().register(new BuilderChat());

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new UserHomeHandler(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");
		features.tryToRegister(new DiscordCommand(), "DiscordCommand");
		features.tryToRegister(new VoteCommand(), "VoteCommand");

		ModSupport.register(new VanishModSupport());
		ModSupport.register(new BungeecordSupport());
		ModSupport.validateMods();

		if (KiloConfig.getProvider().getMain().getBooleanSafely("startup-script.auto-generate", true))
			new StartupScript();
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
		if(instance==null)
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
				return new SimpleMessage(KE_PREFIX + message);
			}
		};
	}

}
