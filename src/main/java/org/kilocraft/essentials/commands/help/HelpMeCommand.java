package org.kilocraft.essentials.commands.help;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.api.KiloServer;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.user.OnlineServerUser;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class HelpMeCommand extends EssentialCommand {
    public HelpMeCommand() {
        super("helpme", new String[]{"helpop"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> messageArgument = argument("message", greedyString())
                .executes(this::sendHelp);

        argumentBuilder.executes(this::execute);
        commandNode.addChild(messageArgument.build());
    }

    private int execute(CommandContext<ServerCommandSource> ctx) {
        getServerUser(ctx).sendLangMessage("command.helpme.usage");
        return SINGLE_SUCCESS;
    }

    private int sendHelp(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getServerUser(ctx);
        Text text = new LiteralText("")
                .append(LangText.getFormatter(true, "command.helpme.prefix"))
                .append(" ")
                .append(new LiteralText(ctx.getSource().getName()).formatted(Formatting.YELLOW)
                        .append(new LiteralText(": ").formatted(Formatting.WHITE)))
                .append(new LiteralText(getString(ctx, "message")).formatted(Formatting.WHITE));

        int i = 0;
        for (OnlineUser user : KiloServer.getServer().getUserManager().getOnlineUsersAsList()) {
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

        return SINGLE_SUCCESS;
    }

}
