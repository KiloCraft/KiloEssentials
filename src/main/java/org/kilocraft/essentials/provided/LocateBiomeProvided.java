package org.kilocraft.essentials.provided;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.server.command.LocateCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocateBiomeProvided implements Command {
    private static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((object) -> new TranslatableText("commands.locatebiome.notFound", new Object[]{object}));
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
    public static int execute(ServerCommandSource source, Biome biome) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(source.getPosition());
        BlockPos biomePos = source.getWorld().locateBiome(biome, pos, 6440, 8);

        if (biomePos == null) {
            Thread.currentThread().interrupt();
            throw NOT_FOUND_EXCEPTION.create(biome.getName().getString());
        }

        Thread.currentThread().interrupt();
        return LocateCommand.sendCoordinates(source, biome.getName().getString(), pos, biomePos, "commands.locatebiome.success");
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
        try {
            LocateBiomeProvided.execute(this.source, this.biome);
        } catch (CommandSyntaxException e) {
            source.sendError(new LiteralText(e.getMessage()));
        }
    }
}