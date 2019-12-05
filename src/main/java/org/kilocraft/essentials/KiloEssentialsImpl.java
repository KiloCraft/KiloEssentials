package org.kilocraft.essentials;

import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.feature.*;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.chat.channels.BuilderChat;
import org.kilocraft.essentials.chat.channels.GlobalChat;
import org.kilocraft.essentials.chat.channels.StaffChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.warps.WarpManager;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.util.messages.MessageUtil;
import org.kilocraft.essentials.util.messages.nodes.ExceptionMessageNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.indicode.fabric.permissions.Thimble.permissionWriters;

public class KiloEssentialsImpl implements KiloEssentials {
	public static CommandDispatcher<ServerCommandSource> commandDispatcher;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private static List<String> initializedPerms = new ArrayList<>();
	private static KiloEssentialsImpl instance;
	private KiloCommands commands;
	private static ModConstants constants = new ModConstants();
	public static String PERMISSION_PREFIX = "kiloessentials.";

	private List<FeatureType<?>> configurableFeatureRegistry = new ArrayList<>();
	private Map<FeatureType<?>, ConfigurableFeature> proxyFeatureList = new HashMap<>();

	private List<FeatureType<SingleInstanceConfigurableFeature>> singleInstanceConfigurationRegistry = new ArrayList<>();
	private Map<FeatureType<? extends SingleInstanceConfigurableFeature>, SingleInstanceConfigurableFeature> proxySingleInstanceFeatures = new HashMap<>();

	public KiloEssentialsImpl(KiloEvents events, KiloCommands commands) {
		instance = this;
		logger.info("Running KiloEssentials version " + ModConstants.getVersion());

		new KiloConfig();
		// ConfigDataFixer.getInstance(); // i509VCB: TODO Uncomment when I finish DataFixers.
		this.commands = commands;
		Thimble.disableVanillaCommandPerms();

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

		//TODO: Move this to the UserHomeHandler
		//Registers the limit permissions
		for (int i = 0; i == KiloConfig.getProvider().getMain().getIntegerSafely("homes.limit", 20); i++) {
			registerPermission(EssentialPermissions.HOME_SET_LIMIT.getNode() + i);
		}

		//Initializes the EssentialsPermissions, these permissions aren't used in the literal commands
		for (EssentialPermissions value : EssentialPermissions.values()) {
			initializedPerms.add(value.getNode());
		}

		registerPermissions();
	}

	public static Logger getLogger() {
		return logger;
	}

	public static void registerPermissions() {
		permissionWriters.add((map, server) -> initializedPerms.forEach(perm ->
						map.registerPermission(PERMISSION_PREFIX + perm, PermChangeBehavior.UPDATE_COMMAND_TREE)));
	}

	public static void registerPermission(String node) {
		if (!initializedPerms.contains(PERMISSION_PREFIX + node))
			initializedPerms.add(node);
	}

	public static String getPermissionFor(String node) {
		if (!initializedPerms.contains(node))
			initializedPerms.add(node);
		return "kiloessentials." + node;
	}

	public static boolean hasPermissionNode(ServerCommandSource source, String fullNode) {
		registerPermission(fullNode);
		return Thimble.hasPermissionOrOp(source, PERMISSION_PREFIX + fullNode, 4);
	}

	public static boolean hasPermissionNode(ServerCommandSource source, String fullNode, int opLevel) {
		registerPermission(fullNode);
		return Thimble.hasPermissionOrOp(source, PERMISSION_PREFIX + fullNode, opLevel);
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

    public static RuntimeException getRuntimeException(ExceptionMessageNode node, Object... objects) {
		String string = ModConstants.getMessageUtil().fromExceptionNode(node);
		return new RuntimeException((objects != null) ? String.format(string, objects) : string);
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
}
