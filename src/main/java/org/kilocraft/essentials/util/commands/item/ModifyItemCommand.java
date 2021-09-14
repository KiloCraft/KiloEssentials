package org.kilocraft.essentials.util.commands.item;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.commands.KiloCommands;
import org.kilocraft.essentials.util.registry.RegistryUtils;

public class ModifyItemCommand extends EssentialCommand {
    public ModifyItemCommand() {
        super("modifyitem", src ->
                KiloCommands.hasPermission(src, CommandPermission.ITEM_NAME) ||
                        KiloCommands.hasPermission(src, CommandPermission.ITEM_LORE) ||
                        KiloCommands.hasPermission(src, CommandPermission.ITEM_COMMANDS)
        );
    }

    public static boolean validate(CommandSourceUser user, ItemStack item, String input) {
        if (item.isEmpty()) {
            user.sendLangMessage("command.item.no_item");
            return true;
        }

        if (ComponentText.clearFormatting(input).length() >= 90) {
            user.sendLangMessage("command.item.too_long");
            return true;
        }


        if (user.asPlayer().experienceLevel < 1 && !user.asPlayer().isCreative()) {
            user.sendLangMessage("command.item.no_exp");
            return true;
        }

        for (String disabledItem : KiloConfig.main().disabledItems) {
            Item toItem = RegistryUtils.toItem(disabledItem);
            if (item.getItem().equals(toItem)) {
                user.sendLangMessage("command.item.disabled");
                return true;
            }
        }
        return false;
    }

    public static boolean validate(CommandSourceUser user, ItemStack item) {
        return validate(user, item, "");
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        ItemNameCommand.registerChild(this.argumentBuilder);
        ItemLoreCommand.registerChild(this.argumentBuilder);
        PowerToolsCommand.registerChild(this.argumentBuilder);
    }
}
