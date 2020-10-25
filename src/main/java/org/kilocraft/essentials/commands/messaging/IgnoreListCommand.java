package org.kilocraft.essentials.commands.messaging;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.commands.CommandUtils;
import org.kilocraft.essentials.user.preference.Preferences;

import java.util.Map;
import java.util.UUID;

public class IgnoreListCommand extends EssentialCommand {
    public IgnoreListCommand() {
        super("ignorelist");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.IGNORELIST_OTHERS))
                .executes(ctx -> execute(ctx, getUserArgumentInput(ctx, "user")));

        commandNode.addChild(userArgument.build());
        argumentBuilder.executes(ctx -> execute(ctx, ctx.getSource().getName()));
    }

    private int execute(CommandContext<ServerCommandSource> ctx, String target) throws CommandSyntaxException {
        OnlineUser src = getOnlineUser(ctx);
        getEssentials().getUserThenAcceptAsync(src, target, (user) -> {
            Map<String, UUID> ignoreList = user.getPreference(Preferences.IGNORE_LIST);

            if (ignoreList.isEmpty()) {
                src.sendLangMessage("command.ignorelist.empty");
                return;
            }

            int listSize = ignoreList.size();
            String prefix = CommandUtils.areTheSame(src, user) ? "Ignore list" : user.getFormattedDisplayName() + "'s Ignore list";
            MutableText text = new LiteralText(prefix).formatted(Formatting.GOLD)
                    .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                    .append(new LiteralText(String.valueOf(listSize)).formatted(Formatting.LIGHT_PURPLE))
                    .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

            final int[] i = {0};
            final boolean[] nextColor = {false};
            ignoreList.forEach((name, uuid) -> {
                LiteralText thisIgnored = new LiteralText("");
                i[0]++;

                Formatting thisFormat = nextColor[0] ? Formatting.WHITE : Formatting.GRAY;

                thisIgnored.append(new LiteralText(name).styled((style) -> style.withFormatting(thisFormat).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("[i] ").formatted(Formatting.YELLOW)
                                .append(new LiteralText("Click to remove!").formatted(Formatting.GREEN)))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ignore " + name))));

                if (listSize != i[0])
                    thisIgnored.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

                nextColor[0] = !nextColor[0];
                text.append(thisIgnored);
            });

            src.sendMessage(text);
        });

        return SUCCESS;
    }
}
