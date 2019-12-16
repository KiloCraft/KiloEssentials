package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.chat.KiloChat;

import java.io.File;
import java.io.IOException;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RestartCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal("restart")
                .then(argument("args", greedyString())
                        .executes(c -> execute(c.getSource(), getString(c, "args"))))
                .requires(s -> KiloEssentials.hasPermissionNode(s, EssentialPermission.SERVER_MANAGE_RESTART, 4))
                .executes(c -> execute(c.getSource(), ""));

        dispatcher.register(builder);
    }

    private static int execute(ServerCommandSource source, String args) {
        boolean confirmed = args.contains("-confirmed");

        if (!confirmed && !KiloServer.getServer().getCommandSourceUser(source).isConsole()) {
            LiteralText literalText = new LiteralText("Please confirm your action by clicking on this message!");
            literalText.styled((style) -> {
                style.setColor(Formatting.RED);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("[!] Click here to restart the server").formatted(Formatting.YELLOW)));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/restart -confirmed"));
            });

            KiloChat.sendMessageTo(source, literalText);
            return 0;
        }

        source.getMinecraftServer().stop(false);
        File marker = new File(FabricLoader.getInstance().getGameDirectory(), "RESTARTME");

        try {
            marker.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 1;
    }
}
