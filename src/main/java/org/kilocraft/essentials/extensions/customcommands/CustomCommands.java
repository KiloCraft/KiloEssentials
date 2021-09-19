package org.kilocraft.essentials.extensions.customcommands;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.ReloadableConfigurableFeature;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomCommands implements ReloadableConfigurableFeature {
    public static boolean enabled = false;
    private static final List<SimpleCommand> commands = new ArrayList<>();
    private static CustomCommandsConfig config;

    @Override
    public boolean register() {
        enabled = true;
        this.load();
        return true;
    }

    public void load() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("customCommands.conf", KiloEssentials.getEssentialsPath());
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createFile();
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            ConfigurationNode configNode = loader.load(ConfigurationOptions.defaults()
                    .setHeader(CustomCommandsConfig.HEADER)
                    .setObjectMapperFactory(DefaultObjectMapperFactory.getInstance())
                    .setShouldCopyDefaults(true));

            config = configNode.getValue(TypeToken.of(CustomCommandsConfig.class), new CustomCommandsConfig());

            loader.save(configNode);
        } catch (IOException | ObjectMappingException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file! " + CustomCommands.class.getName());
            e.printStackTrace();
        }

        createFromConfig();
    }

    private static void createFromConfig() {
        if (!commands.isEmpty()) {
            commands.forEach(sc -> SimpleCommandManager.unregister(sc.getLabel()));
            commands.clear();
        }

        for (CustomCommandConfigSection cs : config.commands) {
            SimpleCommand simpleCommand = new SimpleCommand(cs.label, (source, args) -> runCommand(source, args, cs));

            if (cs.reqSection.op != 0) {
                simpleCommand.requires(cs.reqSection.op);
            }

            if (cs.reqSection.permission != null && !cs.reqSection.permission.equalsIgnoreCase("none")) {
                simpleCommand.requires(cs.reqSection.permission);
            }

            SimpleCommandManager.register(simpleCommand);
            commands.add(simpleCommand);
        }
        KiloCommands.updateGlobalCommandTree();
    }

    private static int runCommand(ServerCommandSource src, String[] args, CustomCommandConfigSection cs) throws CommandSyntaxException {
        int var = 0;
        int iArgs = 0;
        List<String> commands = new ArrayList<>();
        for (String command : cs.executablesList) {
            //Checks if the command contains an argument object: ${args[<number>]}
            //\$\{args\[\d+]}
            if (command.contains("${args[")) {
                String[] strings = command.split(" ");

                for (String string : strings) {
                    if (string.startsWith("${args["))
                        iArgs++;
                }

                if (iArgs >= args.length) {
                    throw new SimpleCommandExceptionType(StringText.of(true, "general.usage", cs.usage)
                            .formatted(Formatting.RED)).create();
                }

                for (int i = 0; i <= args.length + 1; i++) {
                    try {
                        command = command.replaceAll("\\$\\{args\\[" + (i + 1) + "]}", args[i]);
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }
                }

            }

            commands.add(command);
        }

        for (String s : commands) {
            CommandUtils.runCommandWithFormatting(src, s);
            var++;
        }

        return var;
    }

}
