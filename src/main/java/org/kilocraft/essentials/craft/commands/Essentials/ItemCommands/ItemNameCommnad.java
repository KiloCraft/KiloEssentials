package org.kilocraft.essentials.craft.commands.Essentials.ItemCommands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.api.chat.ChatColor;
import org.kilocraft.essentials.api.chat.LangText;

public class ItemNameCommnad {
    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("name")
                .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item.name", 3));

        LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
        LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");

        builder.then(setArgument);
        builder.then(resetArgument);
        argumentBuilder.then(builder);

        resetArgument.executes(context -> {
            ItemStack item = context.getSource().getPlayer().getMainHandStack();
            item.setCustomName(new TranslatableText(item.getItem().getTranslationKey()));

            return 1;
        });

        setArgument.then(CommandManager.argument("name...", StringArgumentType.greedyString()).executes(context -> {
            PlayerEntity player = context.getSource().getPlayer();
            ItemStack item = player.getMainHandStack();

            if (item == null) {
                context.getSource().sendFeedback(LangText.get(true, "command.rename.noitem"), false);
            } else {
                if (player.experienceLevel < 1 && !player.isCreative()) {
                    context.getSource().sendFeedback(LangText.get(true, "command.rename.noxp"), false);
                }

                if (player.isCreative() == false) {
                    player.addExperienceLevels(-1);
                }

                item.setCustomName(new LiteralText(
                        ChatColor.translateAlternateColorCodes('&', StringArgumentType.getString(context, "name..."))
                ));

                player.sendMessage(LangText.getFormatter(true, "command.rename.success", StringArgumentType.getString(context, "name...")));
            }

            return 1;
        }));

    }
}
