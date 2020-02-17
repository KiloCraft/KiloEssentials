package org.kilocraft.essentials.commands.misc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.api.world.location.Vec3dLocation;
import org.kilocraft.essentials.user.ServerUser;
import org.kilocraft.essentials.util.TextUtils;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private int execute(CommandSourceUser src, User target) {
        TextUtils.InfoBlockStyle text = new TextUtils.InfoBlockStyle("Who's " + target.getNameTag(),
                Formatting.GOLD, Formatting.AQUA, Formatting.GRAY);

        text.append("DisplayName", target.getFormattedDisplayName()).space().append("(").append(target.getUsername()).append(")");
        text.append("UUID",
                TextUtils.appendButton(
                        new LiteralText(target.getUuid().toString()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getUuid().toString()
                )
        );
        text.append("IP (Last Saved)",
                TextUtils.appendButton(
                        new LiteralText(target.getLastSocketAddress()),
                        new LiteralText(tl("general.click_copy")),
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        target.getLastSocketAddress()
                )
        );
        text.append("Abilities",
                new String[]{"Invulnerable", "May Fly", "May Seat", "Online", "isStaff", "GameMode"},
                target.isInvulnerable(), target.canFly(), target.canSit(), target.isOnline(), ((ServerUser) target).isStaff(), target.getGameMode().getName()
        );

        if (target.getTicksPlayed() >= 0) {
            text.append("Playtime", TimeDifferenceUtil.convertSecondsToString(target.getTicksPlayed() / 20, '6', 'e'));
        }
        if (target.getFirstJoin() != null) {
            text.append("Join Date", target.getFirstJoin().toString());
            text.append("First joined", TimeDifferenceUtil.formatDateDiff(target.getFirstJoin().getTime()));
        }

        text.append("Meta", new String[]{"Homes", "RTPs", "UpStreamChannelId"},
                target.getHomesHandler().homes(), target.getRTPsLeft(), target.getUpstreamChannelId());
        text.append("Is Spying", new String[]{"On Commands", "On Social"},
                target.isCommandSpyOn(), target.isSocialSpyOn());

        Map<String, UUID> ignoreList = ((ServerUser) target).getIgnoreList();
        if (ignoreList != null && ignoreList.size() > 1) {
            List<String> ignoreNames = new ArrayList<>();
            ignoreList.forEach((name, uuid) -> ignoreNames.add(name));
            text.append("IgnoreList", new String[]{"size", "list"}, ignoreList.size(), ignoreNames);
        }

        Vec3dLocation vec = ((Vec3dLocation) target.getLocation()).shortDecimals();
        text.append("Location", new String[]{"x", "y", "z", "World"},
                vec.getX(), vec.getY(), vec.getZ(),
                vec.getDimension().getPath(),
                getButtonForVec(vec)
        );

        if (target.getLastSavedLocation() != null) {
            Vec3dLocation savedVec = ((Vec3dLocation) target.getLastSavedLocation()).shortDecimals();
            text.append("Saved Loc", new String[]{"x", "y", "z", "World"},
                    vec.getX(), vec.getY(), vec.getZ(),
                    vec.getDimension().getPath(),
                    getButtonForVec(savedVec)
            );
        }

        src.sendMessage(text.get());
        return SINGLE_SUCCESS;
    }

    private Text getButtonForVec(Vec3dLocation vec) {
        return TextUtils.appendButton(
                new LiteralText("Click Here"),
                new LiteralText(tl("general.click_tp")),
                ClickEvent.Action.SUGGEST_COMMAND,
                "/tpin " + vec.getDimension().toString() + " " +
                        vec.getX() + " " + vec.getY() + " " + vec.getZ() + "  @s"
        );
    }

}
