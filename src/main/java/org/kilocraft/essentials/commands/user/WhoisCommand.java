package org.kilocraft.essentials.commands.user;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.user.settting.UserSettings;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.user.UserHomeHandler;
import org.kilocraft.essentials.user.setting.Settings;
import org.kilocraft.essentials.util.text.Texter;
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

        return AWAIT;
    }

    private int execute(CommandSourceUser src, User target) {
        Texter.InfoBlockStyle text = new Texter.InfoBlockStyle("Who's " + target.getNameTag(), Formatting.GOLD, Formatting.AQUA, Formatting.GRAY);

        text.append("DisplayName", target.getFormattedDisplayName())
                .space()
                .append("(").append(target.getUsername()).append(")")
                .space()
                .append(
                        Texter.appendButton(
                                Texter.toText("( More )").formatted(Formatting.GRAY),
                                Texter.toText("Click to see the name history"),
                                ClickEvent.Action.RUN_COMMAND,
                                "/whowas " + target.getUsername()
                        )
                );

        text.append("UUID",
                Texter.appendButton(
                        new LiteralText(target.getUuid().toString()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getUuid().toString()
                )
        );
        text.append("IP (Last Saved)",
                Texter.appendButton(
                        new LiteralText(target.getLastSocketAddress()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getLastSocketAddress()
                )
        );

        UserSettings settings = target.getSettings();
        text.append("Status",
                new String[]{"Invulnerable", "GameMode", "Online"},
                settings.get(Settings.INVULNERABLE),
                settings.get(Settings.GAME_MODE).getName(),
                target.isOnline()
        );

        if (target.isOnline()) {
            OnlineUser user = (OnlineUser) target;
            text.append("Survival status",
                    new String[]{"Health", "FoodLevel", "Saturation"},
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHealth()),
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHungerManager().getFoodLevel()),
                    ModConstants.DECIMAL_FORMAT.format(user.asPlayer().getHungerManager().getSaturationLevel())
            );
        }

        text.append("Artifacts",
                new String[]{"IsStaff", "May Fly", "May Sit"},
                ((ServerUser) target).isStaff(),
                settings.get(Settings.CAN_FLY),
                settings.get(Settings.CAN_SEAT)
        );

        if (target.getTicksPlayed() >= 0) {
            text.append("Playtime", TimeDifferenceUtil.convertSecondsToString(target.getTicksPlayed() / 20, '6', 'e'));
        }
        if (target.getFirstJoin() != null) {
            text.append("First joined", Texter.toText("&e" + TimeDifferenceUtil.formatDateDiff(target.getFirstJoin().getTime())).styled((style) -> {
                style.setHoverEvent(Texter.Events.onHover("&d" + ModConstants.DATE_FORMAT.format(target.getFirstJoin())));
                return style;
            }));
        }

        if (!target.isOnline() && target.getLastOnline() != null) {
            text.append("Last Online", Texter.toText("&e" +  TimeDifferenceUtil.formatDateDiff(target.getLastOnline().getTime())).styled((style) -> {
                style.setHoverEvent(Texter.Events.onHover("&d" + ModConstants.DATE_FORMAT.format(target.getLastOnline())));
                return style;
            }));
        }

        assert target.getHomesHandler() != null;
        text.append("Meta", new String[]{"Homes", "Random Teleports Left", "Selected channel"},
                UserHomeHandler.isEnabled() ? target.getHomesHandler().homes() : 0,
                target.getSetting(Settings.RANDOM_TELEPORTS_LEFT),
                target.getSetting(Settings.CHAT_CHANNEL).getId());

        text.append("Is Spying", new String[]{"On Commands", "On Social"},
                target.getSetting(Settings.COMMAND_SPY),
                target.getSetting(Settings.SOCIAL_SPY));

        Vec3dLocation vec = ((Vec3dLocation) target.getLocation()).shortDecimals();
        assert vec.getDimension() != null;
        MutableText loc = Texter.toText(vec.asFormattedString());
        text.append("Location", vecLocToText(loc, vec));

        if (target.getLastSavedLocation() != null) {
            Vec3dLocation savedVec = ((Vec3dLocation) target.getLastSavedLocation()).shortDecimals();
            MutableText lastLoc = Texter.toText(savedVec.asFormattedString());
            text.append("Saved Location", vecLocToText(lastLoc, savedVec));
        }

        src.sendMessage(text.build());
        return SUCCESS;
    }

    private MutableText vecLocToText(MutableText text, Vec3dLocation vec) {
        assert vec.getDimension() != null;
        return Texter.appendButton(
                text,
                new LiteralText(tl("general.click_tp")),
                ClickEvent.Action.SUGGEST_COMMAND,
                "/tpin " + vec.getDimension().toString() + " " +
                        vec.getX() + " " + vec.getY() + " " + vec.getZ()
        );
    }

}
