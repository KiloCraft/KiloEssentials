package org.kilocraft.essentials.craft;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.SharedConstants;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.Mod;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.util.SomeGlobals;
import org.kilocraft.essentials.craft.commands.BackCommand;
import org.kilocraft.essentials.craft.commands.GamemodeCommand;
import org.kilocraft.essentials.craft.commands.InfoCommand;
import org.kilocraft.essentials.craft.commands.essentials.*;
import org.kilocraft.essentials.craft.commands.essentials.ItemCommands.ItemCommand;
import org.kilocraft.essentials.craft.commands.essentials.locateCommands.LocateCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ReloadCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.ServerModNameCommand;
import org.kilocraft.essentials.craft.commands.servermanagement.StopCommand;
import org.kilocraft.essentials.craft.config.KiloConifg;

import java.util.Map;

public class KiloCommands {
    private CommandDispatcher<ServerCommandSource> dispatcher;
    public KiloCommands() {
        this.dispatcher = SomeGlobals.commandDispatcher;
        register(false);
    }

    public static KiloCommands INSTANCE;

    private void register(boolean devEnv) {
        if (devEnv) {
            Mod.getLogger().debug("Server is running in debug mode!");
            SharedConstants.isDevelopment = true;
        }

        /**
         * @Misc
         */
        InfoCommand.register(this.dispatcher);
        ReloadCommand.register(this.dispatcher);

        /**
         * @Essentials
         */
        RandomTeleportCommand.register(this.dispatcher);
        ColorsCommand.register(this.dispatcher);
        GamemodeCommand.register(this.dispatcher);
        LocateCommand.register(this.dispatcher);
        EnderchestCommand.register(this.dispatcher);
        TpaCommand.register(this.dispatcher);
        ItemCommand.register(this.dispatcher);
        AnvilCommand.register(this.dispatcher);
        CraftingbenchCommand.register(this.dispatcher);
        NickCommand.register(this.dispatcher);
        KillCommand.register(this.dispatcher);
        RealNameCommand.register(this.dispatcher);
        
        /**
         * @ServerManagement
         */
        ServerCommand.register(this.dispatcher);
        ServerModNameCommand.register(this.dispatcher);
        StopCommand.register(this.dispatcher);
        BackCommand.register(this.dispatcher);
        OperatorCommand.register(this.dispatcher);
    }

    public String buildSmartUsage(LiteralCommandNode<ServerCommandSource> literalCommandNode, ServerCommandSource source) {
        String string = KiloConifg.getMessages().get("command.context.usage");
        Map<CommandNode<ServerCommandSource>, String> usage = this.dispatcher.getSmartUsage(literalCommandNode, source);
        return string.replaceFirst("%s", usage.toString());
    }

    public String buildUsage(String usage, ServerCommandSource source) {
        String string = KiloConifg.getMessages().get("command.context.usage");
        return string.replaceFirst("%s", usage);
    }

    public static LiteralText getPermissionError(String permissionNode) {
        LiteralText literalText = LangText.get(true, "command.exception.permission");
        literalText.styled((style) -> {
           style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText(permissionNode).formatted(Formatting.YELLOW)));
        });
        return literalText;
    }
}
