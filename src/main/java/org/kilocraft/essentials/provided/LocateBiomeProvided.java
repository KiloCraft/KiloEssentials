package org.kilocraft.essentials.provided;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.api.chat.LangText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocateBiomeProvided implements Command {
    private static List<Thread> threads = new ArrayList<>();
    private Biome biome;

    public LocateBiomeProvided(Biome biome) {
        this.biome = biome;
    }

    @Override
    public int run(CommandContext context) {
        BiomeLocatorThread locatorThread = new BiomeLocatorThread((ServerCommandSource) context.getSource(), biome);
        Thread thread = new Thread(locatorThread, "Biome locator thread");
        thread.start();

        threads.add(thread);
        return SINGLE_SUCCESS;
    }

    public static void stopAll() {
        for (Thread thread : threads) {
            thread.stop();
        }
    }

    @SuppressWarnings("Do not run on main thread")
    public static int execute(ServerCommandSource source, Biome biome) {
        BlockPos executorPos = new BlockPos(source.getPosition());
        BlockPos biomePos = null;
        String biomeName = getBiomeName(biome);
        try {
            biomePos = spiralOutwardsLookingForBiome(source, source.getWorld(), biome, executorPos.getX(), executorPos.getZ());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }

        if (biomePos == null) {
            source.sendFeedback(LangText.getFormatter(true, "command.locate.biome.failed",  getBiomeName(biome), (System.currentTimeMillis() - start) / 1000), false);
            return 0;
        }

        BlockPos finalBiomePos = biomePos;

        int distance = MathHelper.floor(getDistance(executorPos.getX(), executorPos.getZ(), finalBiomePos.getX(), finalBiomePos.getZ()));
        Text coordinates = Texts.bracketed(new TranslatableText("chat.coordinates", finalBiomePos.getX(), "~", finalBiomePos.getZ())
                .styled((style) -> {
                    style.setColor(Formatting.GREEN);
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + source.getName() + " " + finalBiomePos.getX() + " ~ " + finalBiomePos.getZ()));
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")));
                })
        );

        source.sendFeedback(new TranslatableText("commands.locate.success", biomeName, coordinates, distance), false);

        Thread.currentThread().interrupt();
        return 1;
    }

    static long start;
    static int timeout = 123_000;
    private static boolean SuccessfullLocate = false;

    private static BlockPos spiralOutwardsLookingForBiome(ServerCommandSource source, World world, Biome biomeToFind, double startX, double startZ) throws CommandSyntaxException {
        double a = 16 / Math.sqrt(Math.PI);
        double b = 2 * Math.sqrt(Math.PI);
        double x, z;
        double dist = 0;
        start = System.currentTimeMillis();

        BlockPos.PooledMutable pos = BlockPos.PooledMutable.get();
        int previous = 0;
        int i = 0;
        for (int n = 0; dist < Integer.MAX_VALUE; ++n) {
            if ((System.currentTimeMillis() - start) > timeout)
                return null;


            double rootN = Math.sqrt(n);
            dist = a * rootN;
            x = startX + (dist * Math.sin(b * rootN));
            z = startZ + (dist * Math.cos(b * rootN));
            pos.set(x, 0, z);
            if (previous == 3)
                previous = 0;
            String dots = (previous == 0 ? "." : previous == 1 ? ".." : "...");
            if (i == 9216) {
                previous++;
                i = 0;
            }
            i++;
            if (world.getBiomeAccess().getBiome(pos).equals(biomeToFind)) {
                SuccessfullLocate = true;
                pos.close();
                //Feedback: Success
                source.sendFeedback(LangText.getFormatter(true, "command.locate.found", getBiomeName(biomeToFind).toLowerCase(), (System.currentTimeMillis() - start) / 1000), false);
                return new BlockPos((int) x, 0, (int) z);

            }
        }
        source.sendFeedback(LangText.getFormatter(true, "command.locate.biome.failed", getBiomeName(biomeToFind), (System.currentTimeMillis() - start) / 1000), false);
        return null;
    }

    private static double getDistance(int posX, int posZ, int biomeX, int biomeZ) {
        return MathHelper.sqrt(Math.pow(biomeX - posX, 2) + Math.pow(biomeZ - posZ, 2));
    }

    public static String getBiomeId(Biome biome) {
        return Objects.requireNonNull(Registry.BIOME.getId(biome)).getPath();
    }

    public static String getBiomeName(Biome biome) {
        String s = getBiomeId(biome).replaceAll("_", " ");
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase());
    }

}

class BiomeLocatorThread implements Runnable {
    private Logger logger = LogManager.getLogger();
    private ServerCommandSource source;
    private Biome biome;

    public BiomeLocatorThread(ServerCommandSource source, Biome biome) {
        this.source = source;
        this.biome = biome;
    }

    @Override
    public void run() {
        logger.info("Locating biome \"" + LocateBiomeProvided.getBiomeId(biome) + "\", executed by " + source.getName());
        LocateBiomeProvided.execute(this.source, this.biome);
    }
}