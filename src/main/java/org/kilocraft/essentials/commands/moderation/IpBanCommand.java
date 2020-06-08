package org.kilocraft.essentials.commands.moderation;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.punishment.Punishment;
import org.kilocraft.essentials.util.GlobalUtils;
import org.kilocraft.essentials.util.TimeDifferenceUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpBanCommand extends EssentialCommand {
    public static final Pattern PATTERN = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    private static final SimpleCommandExceptionType INVALID_IP_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.banip.invalid"));

    public IpBanCommand() {
        super("ipban", CommandPermission.IPBAN);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        final RequiredArgumentBuilder<ServerCommandSource, String> userArgument = this.getUserArgument("victim")
                .executes(ctx -> this.execute(ctx, null, null));
        final RequiredArgumentBuilder<ServerCommandSource, String> reasonArgument = argument("reason", StringArgumentType.string())
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), null));
        final RequiredArgumentBuilder<ServerCommandSource, String> lengthArgument = argument("length", StringArgumentType.string()).suggests(TimeDifferenceUtil::listSuggestions)
                .executes(ctx -> this.execute(ctx, StringArgumentType.getString(ctx, "reason"), StringArgumentType.getString(ctx, "length")));
        reasonArgument.then(lengthArgument);
        userArgument.then(reasonArgument);
        this.argumentBuilder.then(userArgument);
    }

    private int execute(final CommandContext<ServerCommandSource> ctx, @Nullable String reason, String expiryString) throws CommandSyntaxException {
        final CommandSourceUser src = this.getServerUser(ctx);
        final String input = this.getUserArgumentInput(ctx, "victim");
        final String ip = getIP(input);
        Date expiry = expiryString == null ? null : new Date(TimeDifferenceUtil.parse(expiryString, true));
        Punishment punishment = new Punishment(src, null, ip, reason, expiry);
        this.server.getUserManager().performPunishment(punishment, Punishment.Type.DENY_ACCESS_IP, (result) -> {
        });
        return AWAIT;
    }

    private String getIP(String target) throws CommandSyntaxException {
        KiloEssentials.getLogger().info("Input: " + target);
        Matcher matcher = PATTERN.matcher(target);
        if (matcher.matches()) {
            return target;
        } else {
            GameProfile gameProfile = this.server.getMinecraftServer().getUserCache().findByName(target);
            if(gameProfile != null) {
                KiloEssentials.getLogger().info("UUID: " + gameProfile.getId());
                InetSocketAddress socketAddress = (InetSocketAddress) GlobalUtils.getSocketAddress(gameProfile.getId());
                if(socketAddress != null) {
                    String ip = socketAddress.getAddress().toString();
                    ip = ip.substring(1);
                    KiloEssentials.getLogger().info("IP: " + ip);
                    return ip;
                } else {
                    KiloEssentials.getLogger().info("Couldn't find IP adress");
                }
            } else {
                KiloEssentials.getLogger().info("Couldn't find GameProfile");
            }
            ServerPlayerEntity serverPlayerEntity = this.server.getPlayerManager().getPlayer(target);
            if (serverPlayerEntity != null) {
                return serverPlayerEntity.getIp();
            } else {
                throw INVALID_IP_EXCEPTION.create();
            }
        }
    }
}
