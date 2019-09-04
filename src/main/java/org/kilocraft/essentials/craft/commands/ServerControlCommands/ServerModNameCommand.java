package org.kilocraft.essentials.craft.commands.ServerControlCommands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.MinecraftVersion;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import org.kilocraft.essentials.craft.Mod;
import org.kilocraft.essentials.craft.utils.ServerModName;

public class ServerModNameCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("server")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.literal("config").then(CommandManager.literal("brandName")
                        .then(CommandManager.argument("name", StringArgumentType.greedyString())
                                .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "name"))))));


        dispatcher.register(literalArgumentBuilder);
    }

    private static int execute(ServerCommandSource source, String s) {
        ServerModName.setName(ChatColor.translateAlternateColorCodes('&',
                String.format(s + "&r <- Fabric/KiloEssentials (%s, %s)", MinecraftVersion.create().getName(), Mod.getVersion())));

        source.sendFeedback(new LiteralText("You have successfully changed the server custom brand name to:\n "
        + ServerModName.getName()), true);
        return 1;
    }
}
