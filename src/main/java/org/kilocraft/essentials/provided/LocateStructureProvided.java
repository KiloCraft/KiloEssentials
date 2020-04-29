package org.kilocraft.essentials.provided;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class LocateStructureProvided {
    public static String getStructureName(String structure) {
        String s = structure.replaceAll("_", " ");
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase());
    }

    @SuppressWarnings("Do not run on main thread")
    public static int execute(ServerCommandSource source, String structure) {
        BlockPos blockPos_1 = new BlockPos(source.getPosition());
        BlockPos blockPos_2 = source.getWorld().locateStructure(structure, blockPos_1, 100, false);
        if (blockPos_2 == null) {
            source.sendError(new TranslatableText("commands.locate.failed"));
            return 0;
        }

        int int_1 = MathHelper.floor(getDistance(blockPos_1.getX(), blockPos_1.getZ(), blockPos_2.getX(), blockPos_2.getZ()));
        Text coordinates = Texts.bracketed(new TranslatableText("chat.coordinates", blockPos_2.getX(), "~", blockPos_2.getZ())
                .styled((style) -> {
                    return style.withFormatting(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + source.getName() + " " + blockPos_2.getX() + " ~ " + blockPos_2.getZ())).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")));
                })
        );

        source.sendFeedback(new TranslatableText("commands.locate.success", getStructureName(structure), coordinates, int_1), false);

        Thread.currentThread().interrupt();
        return int_1;
    }

    private static float getDistance(int int_1, int int_2, int int_3, int int_4) {
        int int_5 = int_3 - int_1;
        int int_6 = int_4 - int_2;
        return MathHelper.sqrt((float)(int_5 * int_5 + int_6 * int_6));
    }
}
