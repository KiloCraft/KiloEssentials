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
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.feature.FeatureNotPresentException;
import org.kilocraft.essentials.api.feature.FeatureType;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.api.feature.ConfigurableFeatures;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.extensions.warps.WarpManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KiloEssentialsImpl implements KiloEssentials {
	public static CommandDispatcher<ServerCommandSource> commandDispatcher;
	private static Logger logger = LogManager.getFormatterLogger("KiloEssentials");
	private static List<String> initializedPerms = new ArrayList<>();
	private static KiloEssentialsImpl instance;
	private KiloCommands commands;
	private static ModConstants constants = new ModConstants();

	private List<FeatureType<?>> configurableFeatureRegistry = new ArrayList<>();
	private Map<FeatureType<?>, ConfigurableFeature> proxyFeatureList = new HashMap<>();

	public KiloEssentialsImpl(KiloEvents events, KiloCommands commands) {
		instance = this;
		logger.info("Running KiloEssentials version " + ModConstants.getVersion());

		new KiloConfig();
		this.commands = commands;

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

		ConfigurableFeatures features = new ConfigurableFeatures();
		features.tryToRegister(new UserHomeHandler(), "PlayerHomes");
		features.tryToRegister(new WarpManager(), "ServerWideWarps");

		Thimble.permissionWriters.add((map, server) -> initializedPerms.forEach(perm -> map.registerPermission("kiloessentials." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE)));
	}

	public static Logger getLogger() {
		return logger;
	}

	public static String getPermissionFor(String node) {
		if (!initializedPerms.contains(node))
			initializedPerms.add(node);
		return "kiloessentials." + node;
	}

    public static KiloEssentialsImpl getInstance() {
		if(instance==null) {
			throw new RuntimeException("Accessed KiloEssentials too early");
		}

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
		configurableFeatureRegistry.add(featureType);
		return featureType;
	}

	public <F extends ConfigurableFeature> F getFeature(FeatureType<F> type) throws FeatureNotPresentException {
		F ft = (F) proxyFeatureList.get(type);

		if(ft==null) {
			throw new FeatureNotPresentException();
		}

		return (F) proxyFeatureList.get(type);
	}
}
