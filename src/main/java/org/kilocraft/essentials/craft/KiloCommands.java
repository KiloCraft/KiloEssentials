package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import io.github.indicode.fabric.permissions.Thimble;
import io.github.indicode.fabric.permissions.command.CommandPermission;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.chat.ChatMessage;
import org.kilocraft.essentials.craft.chat.KiloChat;
import org.kilocraft.essentials.craft.commands.GamemodeCommand;
import org.kilocraft.essentials.craft.commands.KiloInfoCommand;
import org.kilocraft.essentials.craft.commands.RainbowCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.essentials.locateCommands.LocateCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.OperatorCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.StopCommand;
import org.kilocraft.essentials.craft.commands.staffcommands.BanCommand;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class KiloCommands {
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
        }

        /**
         * @Misc
         */
        KiloInfoCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);

        /**
         * @Essentials
         */
        ColorsCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        TpaCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher);
        AnvilCommand.register(this.dispatcher);
        CraftingbenchCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);
        FlyCommand.register(this.dispatcher);
        SpeedCommand.register(this.dispatcher);
        //RandomTeleportCommand.register(this.dispatcher);
        //NickCommand.register(this.dispatcher);
        //BackCommand.register(this.dispatcher);
        //PlayerParticlesCommand.register(this.dispatcher);
        MessageCommand.register(this.dispatcher);
        HealCommand.register(this.dispatcher);
        FeedCommand.register(this.dispatcher);
        SudoCommand.register(this.dispatcher);
        BroadcastCommand.register(this.dispatcher);
        InvulnerablemodeCommand.register(this.dispatcher);
        DiscordCommand.register(this.dispatcher);
        KiloInfoCommand.register(this.dispatcher);
        TimeCommand.register(this.dispatcher);
        InstantbuildCommand.register(this.dispatcher);
        //InfoCommand.register(this.dispatcher);

        /**
         * @ServerManagement
         */
        StopCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);

        /**
         * @Staff
         */
        BanCommand.register(this.dispatcher);

        Thimble.permissionWriters.add((map, server) -> {
            try {
                map.getPermission("kiloessentials", CommandPermission.class);
                map.getPermission("kiloessentials.command", CommandPermission.class);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                e.printStackTrace();
            }
            initializedPerms.forEach(perm -> {
                try {
                    map.getPermission("kiloessentials.command." + perm, CommandPermission.class);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public static int executeUsageFor(String langKey, ServerCommandSource source) {
        String fromLang = Mod.getLang().getProperty(langKey);
        if (fromLang != null)
            KiloChat.sendMessageToSource(source, new ChatMessage("&6Correct usage:\n" + fromLang, true));
        else
            KiloChat.sendLangMessageTo(source, "general.usage.help");
        return 1;
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
