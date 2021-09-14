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
import org.kilocraft.essentials.api.KiloEssentials;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LocateBiomeProvided implements Command {
    private static final DynamicCommandExceptionType NOT_FOUND_EXCEPTION = new DynamicCommandExceptionType((object) -> new TranslatableText("commands.locatebiome.notFound", object));
    private static final List<Thread> threads = new ArrayList<>();
    private final Biome biome;

    public LocateBiomeProvided(Biome biome) {
        this.biome = biome;
    }

    @Override
    public int run(CommandContext context) {
        BiomeLocatorThread locatorThread = new BiomeLocatorThread((ServerCommandSource) context.getSource(), this.biome);
        Thread thread = new Thread(locatorThread, "Biome locator thread");
        thread.start();

        threads.add(thread);
        return SINGLE_SUCCESS;
    }

    public static void stopAll() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    @SuppressWarnings("Do not run on main thread")
    public static int execute(ServerCommandSource source, Biome biome) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(source.getPosition());
        BlockPos biomePos = source.getWorld().locateBiome(biome, pos, 6440, 8);

        if (biomePos == null) {
            Thread.currentThread().interrupt();
            throw NOT_FOUND_EXCEPTION.create(LocateBiomeProvided.getBiomeId(biome));
        }

        Thread.currentThread().interrupt();
        return LocateCommand.sendCoordinates(source, null, pos, biomePos, "commands.locatebiome.success");
    }

    public static String getBiomeId(Biome biome) {
        return Objects.requireNonNull(KiloEssentials.getMinecraftServer().getRegistryManager().get(Registry.BIOME_KEY).getId(biome)).toString();
    }

    public static String getBiomeName(Biome biome) {
        String s = Objects.requireNonNull(KiloEssentials.getMinecraftServer().getRegistryManager().get(Registry.BIOME_KEY).getId(biome)).getPath();
        return s.replaceFirst(String.valueOf(s.charAt(0)), String.valueOf(s.charAt(0)).toUpperCase(Locale.ROOT));
    }


    static class BiomeLocatorThread implements Runnable {
        private final Logger logger = LogManager.getLogger();
        private final ServerCommandSource source;
        private final Biome biome;

        public BiomeLocatorThread(ServerCommandSource source, Biome biome) {
            this.source = source;
            this.biome = biome;
        }

        @Override
        public void run() {
            this.logger.info("Locating biome \"" + LocateBiomeProvided.getBiomeId(this.biome) + "\", executed by " + this.source.getName());
            try {
                LocateBiomeProvided.execute(this.source, this.biome);
            } catch (CommandSyntaxException e) {
                this.source.sendError(new LiteralText(e.getMessage()));
            }
        }
    }
}