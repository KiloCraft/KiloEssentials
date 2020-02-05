package org.kilocraft.essentials.extensions.homes.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.commands.CommandHelper;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.extensions.homes.api.Home;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class HomesCommand extends EssentialCommand {
    public HomesCommand() {
        super("homes", CommandPermission.HOMES_SELF);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> targetArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.HOMES_OTHERS))
                .executes(this::executeOthers);

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(targetArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        OnlineUser user = getOnlineUser(ctx.getSource());
        return sendInfo(user, user);
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        OnlineUser source = getOnlineUser(player);
        String inputName = getString(ctx, "user");

        essentials.getUserThenAcceptAsync(player, inputName, (user) -> {
            if (user.getHomesHandler().getHomes().size() == 0) {
                source.sendMessage(KiloConfig.messages().commands().playerHomes().admin().noHome
                        .replace("{TARGET_TAG}", user.getNameTag()));
                return;
            }

            sendInfo(source, user);
        });

        return AWAIT_RESPONSE;
    }

    private int sendInfo(OnlineUser source, User user) {
        int homesSize = user.getHomesHandler().getHomes().size();
        String prefix = CommandHelper.areTheSame(source, user) ? "Homes" : user.getFormattedDisplayName() + "'s Homes";
        Text text = new LiteralText(prefix).formatted(Formatting.GOLD)
                .append(new LiteralText(" [ ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(String.valueOf(homesSize)).formatted(Formatting.LIGHT_PURPLE))
                .append(new LiteralText(" ]: ").formatted(Formatting.DARK_GRAY));

        int i = 0;
        boolean nextColor = false;
        for (Home home : user.getHomesHandler().getHomes()) {
            LiteralText thisHome = new LiteralText("");
            i++;

            Formatting thisFormat = nextColor ? Formatting.WHITE : Formatting.GRAY;

            thisHome.append(new LiteralText(home.getName()).styled((style) -> {
                style.setColor(thisFormat);
                style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new LiteralText("[i] ").formatted(Formatting.YELLOW)
                                .append(new LiteralText("Click to teleport!").formatted(Formatting.GREEN))));
                style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/home " + home.getName() + " " + user.getUsername()));
            }));

            if (homesSize != i)
                thisHome.append(new LiteralText(", ").formatted(Formatting.DARK_GRAY));

            nextColor = !nextColor;
            text.append(thisHome);
        }

        source.sendMessage(text);
        return SINGLE_SUCCESS;
    }
}
