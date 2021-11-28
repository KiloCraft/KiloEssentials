package org.kilocraft.essentials.util.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
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
    public void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
        final RequiredArgumentBuilder<CommandSourceStack, String> messageArgument = this.argument("message", greedyString())
                .executes(this::sendHelp);

        this.commandNode.addChild(messageArgument.build());
    }

    private int sendHelp(final CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        final Component text = new TextComponent("")
                .append(StringText.of("command.helpme.prefix"))
                .append(" ")
                .append(new TextComponent(ctx.getSource().getTextName()).withStyle(ChatFormatting.YELLOW)
                        .append(new TextComponent(": ").withStyle(ChatFormatting.WHITE)))
                .append(Texter.newText(getString(ctx, "message")).withStyle(ChatFormatting.WHITE));

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
