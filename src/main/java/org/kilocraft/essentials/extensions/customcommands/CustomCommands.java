package org.kilocraft.essentials.extensions.customcommands;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.DefaultObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.feature.ConfigurableFeature;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomCommands implements ConfigurableFeature {
    public static boolean enabled = false;
    static Map<Identifier, SimpleCommand> map = new HashMap<>();
    private static ConfigurationNode configNode;
    private static CustomCommandsConfig config;

    @Override
    public boolean register() {
        enabled = true;
        load();
        return true;
    }

    public static void load() {
        try {
            KiloFile CONFIG_FILE = new KiloFile("customCommands.hocon", KiloEssentials.getEssentialsPath());
            if (!CONFIG_FILE.exists()) {
                CONFIG_FILE.createFile();
                CONFIG_FILE.pasteFromResources("assets/config/customCommands.hocon");
            }

            ConfigurationLoader<CommentedConfigurationNode> loader = HoconConfigurationLoader.builder()
                    .setFile(CONFIG_FILE.getFile()).build();

            configNode = loader.load(ConfigurationOptions.defaults()
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
        map.clear();
        config.commands.forEach((string, cs) -> {
            SimpleCommandManager.unregister(string);
            SimpleCommand simpleCommand = new SimpleCommand(string, cs.label, (source, args, server) -> runCommand(source, args, server, cs));

            simpleCommand.requires(cs.reqSection.op);
            SimpleCommandManager.register(simpleCommand);
            map.put(new Identifier(string), simpleCommand);
        });
    }

    private static int runCommand(ServerCommandSource src, String[] args, Server server, CustomCommandConfigSection cs) throws CommandSyntaxException {
        int var = 0;
        int iArgs = 0;
        List<String> commands = new ArrayList<>();
        for (String s : cs.executablesList) {
            String cmd = s.replace("${source.name}", src.getName());
            //Checks if the command contains an argument object: ${args[<number>]}
            //\$\{args\[\d+]}
            if (cmd.contains("${args[")) {
                String[] strings = cmd.split(" ");

                for (String string : strings) {
                    if (string.startsWith("${args["))
                        iArgs++;
                }

                if (iArgs >= args.length) {
                    throw new SimpleCommandExceptionType(LangText.getFormatter(true, "general.usage", cs.usage)
                            .formatted(Formatting.RED)).create();
                }

                for (int i = 0; i <= args.length + 1; i++) {
                    try {
                        cmd = cmd.replaceAll("\\$\\{args\\[" + (i + 1) + "]}", args[i]);
                    } catch (ArrayIndexOutOfBoundsException ignored) { }
                }

            }

            commands.add(cmd);
        }

        for (String s : commands) {
            if (s.startsWith("!"))
                server.execute(operatorSource(src), s.replace("!", ""));
            else if (s.startsWith("?"))
                    server.execute(s.replaceFirst("\\?", ""));
            else
                server.execute(src, s);

            var++;
        }

        return var;
    }

    private static ServerCommandSource operatorSource(ServerCommandSource src) {
        return new ServerCommandSource(commandOutput(src), src.getPosition(), src.getRotation(),
                src.getWorld(), 4, src.getName(), src.getDisplayName(), src.getMinecraftServer(), src.getEntity());
    }

    private static CommandOutput commandOutput(ServerCommandSource src) {
        return new CommandOutput() {
            @Override
            public void sendMessage(Text text) {
                src.sendFeedback(text, false);
            }

            @Override
            public boolean shouldReceiveFeedback() {
                return true;
            }

            @Override
            public boolean shouldTrackOutput() {
                return false;
            }

            @Override
            public boolean shouldBroadcastConsoleToOps() {
                return false;
            }
        };
    }

}
