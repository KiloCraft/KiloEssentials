package org.kilocraft.essentials.craft.provider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.craft.utils.LangText;

public class LocateBiomeProvider {

    static int timeout = 120_000;
    private static boolean SuccessfullLocate = false;

    public static int executeByCommand(ServerCommandSource source, Biome biome) {
        provide(source, biome, true);
        return 1;
    }

    private static Text sendSuccessFeedback(ServerCommandSource source, String biomeName , BlockPos finalBiomePos, BlockPos sourcePos) {
        LiteralText literalText;
        int dist = MathHelper.floor(getDistance(sourcePos.getX(), sourcePos.getZ(), finalBiomePos.getX(), finalBiomePos.getZ()));
        Text coords = Texts.bracketed(new TranslatableText("chat.coordinates", finalBiomePos.getX(), "~", finalBiomePos.getZ()))
                .setStyle(new Style()
                        .setColor(Formatting.GREEN)
                        .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + finalBiomePos.getX() + " ~ " + finalBiomePos.getZ()))
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip"))));

        source.sendFeedback(new TranslatableText("commands.locate.success", biomeName, coords, dist), false);
        return coords;
    }

    private static void provide(ServerCommandSource source, Biome biome, boolean sendFeedback) {
        new Thread(() -> {
            BlockPos sourcePos = new BlockPos(source.getPosition());
            BlockPos biomePos = null;
            TranslatableText biomeName = new TranslatableText(biome.getTranslationKey());

            biomePos = spiralOutwardsLookingForBiome(source, source.getWorld(), biome, sourcePos.getX(), sourcePos.getZ(), sendFeedback);

            if (biomePos == null) {
                SuccessfullLocate = false;
                if (sendFeedback)
                    source.sendFeedback(
                            LangText.getFormatter(false, "command.locate.biome.scanning"
                                    , biome.getName().setStyle(new Style().setColor(Formatting.GOLD)))
                                    .setStyle(new Style().setColor(Formatting.YELLOW)),
                            false);
                else SuccessfullLocate = true;
                BlockPos finalBiomePose = biomePos;

                if (sendFeedback) {
                    assert finalBiomePose != null;
                    source.sendFeedback(sendSuccessFeedback(source, biomeName.asString(), finalBiomePose, sourcePos), false);
                }

            }
        });
    }

    private static BlockPos spiralOutwardsLookingForBiome(ServerCommandSource source, World world, Biome biomeToFind, double sourceX, double sourceZ, boolean sendFeedback) {
        double a = 16 / Math.sqrt(Math.PI);
        double b = 2 * Math.sqrt(Math.PI);
        double x, z;
        double distance = 0;
        long start = System.currentTimeMillis();

        BlockPos.PooledMutable pos = BlockPos.PooledMutable.get();
        int previous = 0;
        int i = 0;

        for (int n = 0; distance < Integer.MAX_VALUE; ++n) {
            if ((System.currentTimeMillis() - start) >  timeout) return null;

            double rootN = Math.sqrt(n);
            distance = a * rootN;
            x = sourceX + (distance * Math.sin(b * rootN));
            z = sourceZ + (distance * Math.cos(b * rootN));

            pos.set(x, 0, z);

            if (previous == 3) previous = 0;
            String dots = (previous == 0 ? "." : previous == 1 ? ".." : "...");

            /**
             @Start Scanning..
             */
            if (sendFeedback)
                source.sendFeedback(LangText.getFormatter(false, "command.locate.biome.failed", biomeToFind.getName()
                                .setStyle(new Style().setColor(Formatting.GOLD)))
                        .setStyle(new Style().setColor(Formatting.YELLOW))
                        , false);
            if (i == 9216) {
                previous++;
                i = 0;
            }
            i++;
            if (world.getBiome(pos).equals(biomeToFind)) {
                pos.close();
                /**
                 * @Found The Biome
                 */
            }


        }
        return null;
    }

    private static double getDistance(int PosX, int PosZ, int biomeX, int BiomeZ) {
        return MathHelper.sqrt(Math.pow(biomeX - PosX, 2) + Math.pow(BiomeZ - PosZ, 2));
    }

}
