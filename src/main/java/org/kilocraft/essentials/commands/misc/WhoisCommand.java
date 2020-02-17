package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

public class WhoisCommand extends EssentialCommand {
    public WhoisCommand() {
        super("whois", CommandPermission.WHOIS_SELF, new String[]{"info"});
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> userArgument = getUserArgument("user")
                .requires(src -> hasPermission(src, CommandPermission.WHOIS_OTHERS))
                .executes(this::executeOthers);

        argumentBuilder.executes(this::executeSelf);
        commandNode.addChild(userArgument.build());
    }

    private int executeSelf(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        CommandSourceUser user = getServerUser(ctx);
        return execute(user, getOnlineUser(ctx));
    }

    private int executeOthers(CommandContext<ServerCommandSource> ctx) {
        CommandSourceUser src = getServerUser(ctx);
        essentials.getUserThenAcceptAsync(src, getUserArgumentInput(ctx, "user"), (user) -> {
            execute(src, user);
        });

        return AWAIT_RESPONSE;
    }

    private static final String SEPERATOR = "-----------------------------------------------------";

    private int execute(CommandSourceUser src, User target) {
        Text text = new LiteralText("").append(new LiteralText(SEPERATOR).formatted(Formatting.GRAY)).append("\n");
        text.append(newHead("DisplayName", target.getFormattedDisplayName())
                .append(new LiteralText(" (").append(target.getUsername()).append(")")).formatted(Formatting.YELLOW));
        text.append("\n");
        text.append(newHead("UUID", new LiteralText(target.getUuid().toString()).styled((style) -> {
            style.setColor(Formatting.YELLOW);
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Copy!")));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, target.getUuid().toString()));
        })));
        text.append("\n");
        if (target.getLastSocketAddress() != null) {
            text.append(newHead("Socket Address (Last Saved)", target.getLastSocketAddress().replaceFirst("/", "")));
            text.append("\n");
        }
        text.append(newHead("Invulnerable", String.valueOf(target.isInvulnerable()))).append(" ")
                .append(newLine("May Fly", String.valueOf(target.canFly()))).append(" ")
                .append(newLine("May Seat", String.valueOf(target.canSit()))).append(" ")
                .append(newLine("Online", String.valueOf(target.isOnline())));
        text.append("\n");
        if (target.getGameMode() != GameMode.NOT_SET) {
            text.append(newHead("Gamemode", target.getGameMode().getName()));
            text.append("\n");
        }
        if (target.getFirstJoin() != null) {
            text.append(newHead("First Join", TimeDifferenceUtil.formatDateDiff(target.getFirstJoin().getTime())));
            text.append("\n");
        }
        text.append(newHead("Homes", String.valueOf(target.getHomesHandler().homes()))).append(" ")
                .append(newLine("RTPs Left", String.valueOf(target.getRTPsLeft()))).append(" ")
                .append(newLine("UpStreamChannelId", target.getUpstreamChannelId()));
        text.append("\n");
        text.append(newHead("Spy", newLine("On Commands", String.valueOf(target.isCommandSpyOn())).append(" ")
                .append(newLine("On Social", String.valueOf(target.isSocialSpyOn())))));

        text.append("\n");
        Vec3dLocation vec = ((Vec3dLocation) target.getLocation()).shortDecimals();
        text.append(newHead("Location",
                new LiteralText("").append(new LiteralText(vec.toString()).formatted(Formatting.LIGHT_PURPLE)).append(" ")
                        .append(newLine("World", vec.getDimension().getPath()).formatted(Formatting.GREEN))).styled((style) -> {
            style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to Teleport!")));
            style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                    "/tpin " + vec.getDimension().toString() + " " + vec.getX() + " " + vec.getY() + " " + vec.getZ() + " " + src.getUsername()));
        }));
        text.append(new LiteralText(SEPERATOR).formatted(Formatting.GRAY));

        src.sendMessage(text);
        return SINGLE_SUCCESS;
    }

    private Text newHead(String title, String value) {
        return new LiteralText("")
                .append(new LiteralText("- ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(title).append(": ").formatted(Formatting.GRAY))
                .append(new LiteralText(value).formatted(Formatting.YELLOW));
    }

    private Text newHead(String title, Text text) {
        return new LiteralText("")
                .append(new LiteralText("- ").formatted(Formatting.DARK_GRAY))
                .append(new LiteralText(title).append(": ").formatted(Formatting.GRAY))
                .append(text.formatted(Formatting.YELLOW));
    }

    private Text newLine(String title, String value) {
        return new LiteralText("")
                .append(new LiteralText(title).append(": ").formatted(Formatting.GRAY))
                .append(new LiteralText(value).formatted(Formatting.YELLOW));
    }

    private Text newLine(String title, Text text) {
        return new LiteralText("")
                .append(new LiteralText(title).append(": ").formatted(Formatting.GRAY))
                .append(text.formatted(Formatting.YELLOW));
    }

}
