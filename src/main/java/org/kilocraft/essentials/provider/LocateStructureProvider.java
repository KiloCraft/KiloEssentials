package org.kilocraft.essentials.provider;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LocateStructureProvider {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.locate.failed", new Object[0]));

    public static String getStructureName(String structure) {
        String s = structure.replaceAll("_", " ");
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase());
    }

    public static int execute(ServerCommandSource source, String structure) throws CommandSyntaxException {
        BlockPos blockPos_1 = new BlockPos(source.getPosition());
        BlockPos blockPos_2 = source.getWorld().locateStructure(structure, blockPos_1, 100, false);
        if (blockPos_2 == null) {
            throw FAILED_EXCEPTION.create();
        } else {
            int int_1 = MathHelper.floor(getDistance(blockPos_1.getX(), blockPos_1.getZ(), blockPos_2.getX(), blockPos_2.getZ()));
            Text text_1 = Texts.bracketed(new TranslatableText("chat.coordinates", new Object[]{blockPos_2.getX(), "~", blockPos_2.getZ()})).styled((style_1) -> {
                style_1.setColor(Formatting.GREEN).setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + source.getName() + " " + blockPos_2.getX() + " ~ " + blockPos_2.getZ())).setHoverEvent(new HoverEvent(net.minecraft.text.HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip", new Object[0])));
            });
            source.sendFeedback(new TranslatableText("commands.locate.success", new Object[]{getStructureName(structure), text_1, int_1}), false);
            return int_1;
        }
    }

    private static float getDistance(int int_1, int int_2, int int_3, int int_4) {
        int int_5 = int_3 - int_1;
        int int_6 = int_4 - int_2;
        return MathHelper.sqrt((float)(int_5 * int_5 + int_6 * int_6));
    }
}
