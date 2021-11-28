package org.kilocraft.essentials.util.commands.moderation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.StringUtils;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.text.Texter;

public class IpInfoCommand extends EssentialCommand {
    public IpInfoCommand() {
        super("ipinfo", CommandPermission.IPINFO, 3, new String[]{"ip"});
    }

    @Override
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, String> targetArgument = this.getUserArgument("user")
                .executes(this::execute);

        this.commandNode.addChild(targetArgument.build());
    }

    private int execute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        OnlineUser source = this.getOnlineUser(ctx);

        this.getUserManager().getUserThenAcceptAsync(source.getCommandSource(), this.getUserArgumentInput(ctx, "user"), (user) -> {
            if (user.getLastSocketAddress() == null) {
                source.sendLangError("exception.no_value_set_user", "lastSocketAddress");
                return;
            }

            String address = user.getLastIp();

            source.sendMessage(
                    Texter.newText(this.tl("command.ipinfo", user.getUsername()))
                            .append(Texter.newRawText(address).withStyle((style) -> style.applyFormat(ChatFormatting.GOLD)
                                    .withHoverEvent(Texter.Events.onHover(ModConstants.translation("general.click_copy")))
                                    .withClickEvent(Texter.Events.onClickCopy(address))
                            )).append(" ").append(
                            Texter.newText().append(StringUtils.socketAddressToPort(user.getLastSocketAddress()))
                                    .withStyle((style) -> style.applyFormat(ChatFormatting.AQUA)
                                            .withHoverEvent(Texter.Events.onHover(ModConstants.translation("general.click_copy")))
                                            .withClickEvent(Texter.Events.onClickCopy(user.getLastSocketAddress()))
                                    )
                    )
            );
        });

        return SUCCESS;
    }

}
