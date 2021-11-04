package org.kilocraft.essentials.extensions.customcommands;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.feature.ReloadableConfigurableFeature;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.commands.CommandUtils;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomCommands implements ReloadableConfigurableFeature {
    private static final List<SimpleCommand> commands = new ArrayList<>();
    private static CustomCommandsConfig config;

    @Override
    public boolean register() {
        this.load();
        return true;
    }

    public void load() {
        Path path = KiloEssentials.getEssentialsPath().resolve("customCommands.conf");
        final HoconConfigurationLoader hoconLoader = HoconConfigurationLoader.builder()
                .path(path)
                .build();
        try {
            final CommentedConfigurationNode rootNode = hoconLoader.load(KiloConfig.configurationOptions().header(CustomCommandsConfig.HEADER));
            config = rootNode.get(CustomCommandsConfig.class, new CustomCommandsConfig());
            if (!path.toFile().exists()) hoconLoader.save(rootNode);
        } catch (ConfigurateException e) {
            KiloEssentials.getLogger().error("Exception handling a configuration file!", e);
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
            // Checks if the command contains an argument object: ${args[<number>]}
            // Regex: \$\{args\[\d+]}
            if (command.contains("${args[")) {
                String[] strings = command.split(" ");

                for (String string : strings) {
                    if (string.startsWith("${args["))
                        iArgs++;
                }

                if (iArgs >= args.length) {
                    throw new SimpleCommandExceptionType(StringText.of("general.usage", cs.usage)
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
