package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;

public interface IEssentialCommand {
    int SUCCESS = 1;
    int AWAIT = 0;
    int FAILED = -1;

    LiteralArgumentBuilder<CommandSourceStack> literal(String label);

    <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String string, ArgumentType<T> argumentType);

    OnlineUser getOnlineUser(String name);

    OnlineUser getOnlineUser(UUID uuid) throws CommandSyntaxException;

    String getLabel();

    String[] getAlias();

    void register(CommandDispatcher<CommandSourceStack> dispatcher);

    enum ForkType {
        DEFAULT,
        SUB_ONLY,
        MAIN_ONLY;

        public boolean shouldRegisterOnMain() {
            return this == DEFAULT || this == MAIN_ONLY;
        }
    }
}
