package org.kilocraft.essentials.commands.servermanagement;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.util.CommandHelper;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.chat.KiloChat;

public class StopCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        KiloCommands.getCommandPermission("server.stop");
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("stop")
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .executes(c -> execute(c.getSource(), StringArgumentType.getString(c, "args")))
                )
                .requires(s -> Thimble.hasPermissionOrOp(s, KiloCommands.getCommandPermission("server.manage.stop"), 4))
                .executes(c -> execute(c.getSource(), ""));

        dispatcher.register(builder);
    }

    private static int execute(ServerCommandSource source, String args) {
        boolean confirmed = args.contains("-confirmed");

        if (!confirmed && !CommandHelper.isConsole(source)) {
            LiteralText literalText = new LiteralText("Please confirm your action by clicking on this message!");
            literalText.styled((style) -> {
                style.setColor(Formatting.RED);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("[!] Click here to stop the server").formatted(Formatting.YELLOW)));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stop -confirmed"));
            });

            KiloChat.sendMessageTo(source, literalText);
        } else
            KiloServer.getServer().shutdown();


        return 1;
    }

}
