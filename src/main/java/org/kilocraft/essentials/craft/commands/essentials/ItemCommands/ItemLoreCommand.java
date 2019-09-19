package org.kilocraft.essentials.craft.commands.essentials.ItemCommands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.github.indicode.fabric.permissions.Thimble;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.kilocraft.essentials.api.chat.ChatColor;
import org.kilocraft.essentials.api.chat.LangText;
import org.yaml.snakeyaml.nodes.Tag;

public class ItemLoreCommand {
    public static void registerChild(LiteralArgumentBuilder<ServerCommandSource> argumentBuilder) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("name")
                .requires(source -> Thimble.hasPermissionChildOrOp(source, "kiloessentials.command.item.lore", 3));

        LiteralArgumentBuilder<ServerCommandSource> resetArgument = CommandManager.literal("reset");
        LiteralArgumentBuilder<ServerCommandSource> setArgument = CommandManager.literal("set");
        RequiredArgumentBuilder<ServerCommandSource, Integer> lineArgument = CommandManager.argument("line", IntegerArgumentType.integer(0, 10));

        builder.then(setArgument);
        builder.then(resetArgument);
		resetArgument.then(lineArgument);
        argumentBuilder.then(builder);

        resetArgument.executes(context -> {
            ItemStack item = context.getSource().getPlayer().getMainHandStack();
            CompoundTag itemTag = item.getTag();
            System.out.println(itemTag.getCompound("display").getType("Lore"));
            //item.gett(new TranslatableText(item.getItem().getTranslationKey()));

            if (item == null) {
				context.getSource().sendFeedback(LangText.get(true, "command.rename.noitem"), false);
			} else {
				item.setCustomName(new TranslatableText(item.getItem().getTranslationKey()));
			}
            
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
