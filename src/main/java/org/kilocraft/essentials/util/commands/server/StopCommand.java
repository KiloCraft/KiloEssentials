package org.kilocraft.essentials.util.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.ArgumentSuggestions;
import org.kilocraft.essentials.user.CommandSourceServerUser;
import org.kilocraft.essentials.util.EssentialPermission;
import org.kilocraft.essentials.util.commands.CommandUtils;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class StopCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = literal("stop")
                .then(argument("args", greedyString())
                        .suggests(ArgumentSuggestions::noSuggestions)
                        .executes(c -> execute(c.getSource(), getString(c, "args"))))
                .requires(s -> KiloEssentials.hasPermissionNode(s, EssentialPermission.SERVER_MANAGE_STOP, 4))
                .executes(c -> execute(c.getSource(), ""));

        dispatcher.register(builder);
    }

    private static int execute(CommandSourceStack source, String args) {
        boolean confirmed = args.contains("-confirmed");

        if (!confirmed && !CommandUtils.isConsole(source)) {
            TextComponent literalText = new TextComponent("Please confirm your action by clicking on this message!");
            literalText.withStyle((style) -> style.applyFormat(ChatFormatting.RED).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("[!] Click here to stop the server").withStyle(ChatFormatting.YELLOW))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stop -confirmed")));

            CommandSourceServerUser.of(source).sendMessage(literalText);
        } else
            KiloEssentials.getMinecraftServer().halt(false);

        return 1;
    }

}
