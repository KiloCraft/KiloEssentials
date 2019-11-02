package org.kilocraft.essentials.craft;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.github.indicode.fabric.permissions.PermChangeBehavior;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TestCommand;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.commands.*;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.donatorcommands.*;
import org.kilocraft.essentials.craft.commands.essentials.itemcommand.ItemCommand;
import org.kilocraft.essentials.craft.commands.essentials.locatecommands.LocateCommand;
import org.kilocraft.essentials.craft.commands.essentials.staffcommands.*;
import org.kilocraft.essentials.craft.commands.servermanagement.OperatorCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.StopCommand;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class KiloCommands {
    private static final SimpleCommandExceptionType SMART_USAGE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new LiteralText("Unknown command or insufficient permissions"));

    private CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands() {
        this.dispatcher = SomeGlobals.commandDispatcher;
        register(true);
    }

    public static KiloCommands INSTANCE;
    private static List<String> initializedPerms = new ArrayList<>();

    public static String getCommandPermission(String command) {
        if (!initializedPerms.contains(command)) {
            initializedPerms.add(command);
        }
        return "kiloessentials.command." + command;
    }

    private void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().debug("Server is running in debug mode!");
            SharedConstants.isDevelopment = devEnv;
            RainbowCommand.register(this.dispatcher);
            DevEnvironmentCommand.register(this.dispatcher);
            TestCommand.register(this.dispatcher);
        }

        /**
         * @Misc
         */
        KiloInfoCommands.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);

        /**
         * @Players
         */
        ColorsCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher);
        InventoryCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        TpaCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);      
        RandomTeleportCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);
        MessageCommand.register(this.dispatcher);
        SudoCommand.register(this.dispatcher);
        BroadcastCommand.register(this.dispatcher);
        DiscordCommand.register(this.dispatcher);
        KiloInfoCommands.register(this.dispatcher);
        UsageCommand.register(this.dispatcher);
        //NicknameCommand.register(this.dispatcher);
        NickCommandOLD.register(this.dispatcher);

        /**
         * @Donators
         */
        PlayerParticlesCommand.register(this.dispatcher);
        AnvilCommand.register(this.dispatcher);
        CraftingbenchCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);

        /**
         * @ServerManagement
         */
        StopCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
        InvulnerablemodeCommand.register(this.dispatcher);

        /**
         * @Staff
         */
        LocateCommand.register(this.dispatcher);
        BanCommand.register(this.dispatcher);
        BackCommand.register(this.dispatcher);
        HealCommand.register(this.dispatcher);
        FeedCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        FlyCommand.register(this.dispatcher);
        SpeedCommand.register(this.dispatcher);
        InfoCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);

        Thimble.permissionWriters.add((map, server) -> {
            initializedPerms.forEach(perm -> map.registerPermission("kiloessentials.command." + perm, PermChangeBehavior.UPDATE_COMMAND_TREE));
        });
    }

    public static int executeUsageFor(String langKey, ServerCommandSource source) {
        String fromLang = Mod.getLang().getProperty(langKey);
        if (fromLang != null)
            KiloChat.sendMessageToSource(source, new ChatMessage("&6Command usage:\n" + fromLang, true));
        else
            KiloChat.sendLangMessageTo(source, "general.usage.help");
        return 1;
    }

    public static int executeSmartUsageFor(String command, ServerCommandSource source) throws CommandSyntaxException {
        ParseResults<ServerCommandSource> parseResults = getDispatcher().parse(command, source);
        if (parseResults.getContext().getNodes().isEmpty()) {
            throw SMART_USAGE_FAILED_EXCEPTION.create();
        } else {
            Map<CommandNode<ServerCommandSource>, String> commandNodeStringMap = getDispatcher().getSmartUsage(((ParsedCommandNode)Iterables.getLast(parseResults.getContext().getNodes())).getNode(), source);
            Iterator iterator = commandNodeStringMap.values().iterator();

            KiloChat.sendLangMessageTo(source, "command.usage.firstRow", command);
            KiloChat.sendLangMessageTo(source, "command.usage.commandRow", command, "");

            while (iterator.hasNext()) {
                String usage = (String) iterator.next();
                KiloChat.sendLangMessageTo(source, "command.usage.commandRow", command, usage);
            }

            return 1;
        }
    }

    public static LiteralText getPermissionError(String hoverText) {
        LiteralText literalText = LangText.get(true, "command.exception.permission");
        literalText.styled((style) -> {
           style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(hoverText).formatted(Formatting.YELLOW)));
        });
        return literalText;
    }

    public static CommandDispatcher<ServerCommandSource> getDispatcher() {
        return SomeGlobals.commandDispatcher;
    }
}
