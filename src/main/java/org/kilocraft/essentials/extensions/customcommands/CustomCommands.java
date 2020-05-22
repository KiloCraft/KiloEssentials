package org.kilocraft.essentials.extensions.customcommands;

import com.google.common.reflect.TypeToken;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
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
import org.kilocraft.essentials.api.command.ArgumentCompletions;
import org.kilocraft.essentials.chat.LangText;
import org.kilocraft.essentials.api.feature.RelodableConfigurableFeature;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.extensions.customcommands.config.CustomCommandsConfig;
import org.kilocraft.essentials.extensions.customcommands.config.sections.CustomCommandConfigSection;
import org.kilocraft.essentials.provided.KiloFile;
import org.kilocraft.essentials.simplecommand.SimpleCommand;
import org.kilocraft.essentials.simplecommand.SimpleCommandManager;
import org.kilocraft.essentials.util.PermissionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CustomCommands implements RelodableConfigurableFeature {
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

    public void load() {
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
        if (!map.isEmpty()) {
            map.forEach((string, cs) -> SimpleCommandManager.unregister(string.toString()));
            map.clear();
        }

        config.commands.forEach((string, cs) -> {
            SimpleCommandManager.unregister(string);
            SimpleCommand simpleCommand = new SimpleCommand(string, cs.label, (source, args, server) -> runCommand(source, args, server, cs));

            if (cs.reqSection.op != 0) {
                simpleCommand.requires(cs.reqSection.op);
            }

            if (cs.reqSection.permission != null && !cs.reqSection.permission.equalsIgnoreCase("none")) {
                simpleCommand.requires(cs.reqSection.permission);
                PermissionUtil.registerNode(cs.reqSection.permission);
            }

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
            CommandUtils.runRespectingConventions(src, s);
            var++;
        }

        return var;
    }

    public enum SuggestionType {
        EMPTY("empty"),
        PLAYERS("players");

        private final String id;
        SuggestionType(String id) {
            this.id = id;
        }

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
            switch (this) {
                case PLAYERS:
                    return ArgumentCompletions.allPlayers(ctx, builder);

                default:
                    return ArgumentCompletions.noSuggestions(ctx, builder);
            }
        }
    }

}
