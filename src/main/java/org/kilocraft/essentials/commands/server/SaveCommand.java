package org.kilocraft.essentials.commands.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.KiloServer;

import static net.minecraft.server.command.CommandManager.literal;
import static org.kilocraft.essentials.KiloCommands.SUCCESS;

public class SaveCommand {
    private static final SimpleCommandExceptionType SAVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.save.failed", new Object[0]));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> saveCommand = literal("save")
                .requires(s -> KiloEssentials.hasPermissionNode(s, EssentialPermission.SERVER_MANAGE_SAVE))
                .executes(ctx -> executeAll(ctx.getSource(), false))
                .then(literal("-flush")
                        .executes(ctx -> executeAll(ctx.getSource(), true)));;

        LiteralArgumentBuilder<ServerCommandSource> usersArgument = literal("users")
                .requires(s -> KiloEssentials.hasPermissionNode(s, EssentialPermission.SERVER_MANAGE_SAVE))
                .executes(ctx -> executeUsers(ctx.getSource()));

        LiteralArgumentBuilder<ServerCommandSource> minecraftArgument = literal("game")
                .requires(s -> KiloEssentials.hasPermissionNode(s, EssentialPermission.SERVER_MANAGE_SAVE))
                .executes(ctx -> executeAll(ctx.getSource(), false))
                .then(literal("flush")
                        .executes(ctx -> executeMinecraft(ctx.getSource(), true)));

        saveCommand.then(usersArgument);
        saveCommand.then(minecraftArgument);
        dispatcher.register(saveCommand);
    }

    private static int executeAll(ServerCommandSource source, boolean flush) throws CommandSyntaxException {
        return executeMinecraft(source, flush);
    }

    private static int executeUsers(ServerCommandSource source) {
        source.sendFeedback(new LiteralText("Saving users data..."), false);
        KiloServer.getServer().getUserManager().saveAllUsers();
        return SUCCESS();
    }

    private static int executeMinecraft(ServerCommandSource source, boolean flush) throws CommandSyntaxException {
        source.sendFeedback(new TranslatableText("commands.save.saving"), false);
        MinecraftServer minecraftServer = source.getMinecraftServer();
        minecraftServer.getPlayerManager().saveAllPlayerData();
        if (!minecraftServer.save(true, flush, true))
            throw SAVE_FAILED_EXCEPTION.create();
        else {
            source.sendFeedback(new TranslatableText("commands.save.success"), true);
            return 1;
        }

    }
}
