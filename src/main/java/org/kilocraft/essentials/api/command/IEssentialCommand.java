package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.user.OnlineUser;

public interface IEssentialCommand {
    int SINGLE_SUCCESS = 1;
    int SINGLE_FAILED = -1;
    LiteralArgumentBuilder<ServerCommandSource> literal(String label);
    <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String string, ArgumentType<T> argumentType);
    OnlineUser getOnlineUser(String name);
    OnlineUser getOnlineUser(ServerCommandSource source) throws CommandSyntaxException;

    String getLabel();

    String[] getAlias();

    void register(CommandDispatcher<ServerCommandSource> dispatcher);
}
