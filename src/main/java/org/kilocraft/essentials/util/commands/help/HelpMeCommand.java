package org.kilocraft.essentials.util.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.chat.StringText;
import org.kilocraft.essentials.user.OnlineServerUser;
import org.kilocraft.essentials.util.text.Texter;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class HelpMeCommand extends EssentialCommand {
    public HelpMeCommand() {
        super("helpme", new String[]{"helpop"});
        this.withUsage("command.helpme.usage", "message");
    }

    @Override
    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = this.argument("message", greedyString())
                .executes(this::sendHelp);

        this.commandNode.addChild(messageArgument.build());
    }

    private int sendHelp(final CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        final Text text = new LiteralText("")
                .append(StringText.of("command.helpme.prefix"))
                .append(" ")
                .append(new LiteralText(ctx.getSource().getName()).formatted(Formatting.YELLOW)
                        .append(new LiteralText(": ").formatted(Formatting.WHITE)))
                .append(Texter.newText(getString(ctx, "message")).formatted(Formatting.WHITE));

        int i = 0;
        for (final OnlineUser user : KiloEssentials.getUserManager().getOnlineUsersAsList()) {
            if (((OnlineServerUser) user).isStaff()) {
                user.sendMessage(text);
                i++;
            }
        }

        if (i == 0) {
            src.sendLangMessage("command.helpme.no_staff");
        } else {
            src.sendLangMessage("command.helpme.sent");
        }

        return i;
    }

}
