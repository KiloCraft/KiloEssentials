package org.kilocraft.essentials.craft.provider;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.kilocraft.essentials.api.chat.LangText;

import java.util.Objects;

public class LocateBiomeProvider {

    public static String getBiomeName(Biome biome) {
        return Objects.requireNonNull(Registry.BIOME.getId(biome)).toString().replace("minecraft:", "");
    }

    public static int execute(ServerCommandSource source, Biome biome) {
        BlockPos executorPos = new BlockPos(source.getPosition());
        BlockPos biomePos = null;
        String biomeName = biome.getName().toString();
        try {
            biomePos = spiralOutwardsLookingForBiome(source, source.getWorld(), biome, executorPos.getX(), executorPos.getZ());
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        if (biomePos == null) {
            source.sendFeedback(new TranslatableText("optimizeWorld.stage.failed",
                    biomeName, timeout / 1000).formatted(Formatting.RED), true);
        }
        BlockPos finalBiomePos = biomePos;
        source.getMinecraftServer().execute(() -> {
            int distance = MathHelper.floor(getDistance(executorPos.getX(), executorPos.getZ(), finalBiomePos.getX(), finalBiomePos.getZ()));
            Text coordinates = Texts.bracketed(new TranslatableText("chat.coordinates", finalBiomePos.getX(), "~", finalBiomePos.getZ())
                .styled((style) -> {
                    style.setColor(Formatting.GREEN);
                    style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp " + source.getName() + " " + finalBiomePos.getX() + " ~ " + finalBiomePos.getZ()));
                    style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")));
                })
            );

            source.sendFeedback(new TranslatableText("commands.locate.success", biomeName, coordinates, distance), false);
        });

        return 0;
    }

    static int timeout = 120_000;
    private static boolean SuccessfullLocate = false;

    private static BlockPos spiralOutwardsLookingForBiome(ServerCommandSource source, World world, Biome biomeToFind, double startX, double startZ) throws CommandSyntaxException {
        double a = 16 / Math.sqrt(Math.PI);
        double b = 2 * Math.sqrt(Math.PI);
        double x, z;
        double dist = 0;
        long start = System.currentTimeMillis();
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
            if (source.getEntity() instanceof PlayerEntity && !(source.getMinecraftServer() instanceof DedicatedServer))
                source.sendFeedback(LangText.getFormatter(true, "command.locate.biome.scanning", dots), false);
            if (i == 9216) {
                previous++;
                i = 0;
            }
            i++;
            if (world.getBiome(pos).equals(biomeToFind)) {
                pos.close();
                if (source.getEntity() instanceof PlayerEntity && !(source.getMinecraftServer() instanceof DedicatedServer))
                    source.sendFeedback(LangText.getFormatter(true, "command.locate.biome.found", biomeToFind.getName(), (System.currentTimeMillis() - start) / 1000), false);
                return new BlockPos((int) x, 0, (int) z);
            }
        }
        return null;
    }


    private static double getDistance(int posX, int posZ, int biomeX, int biomeZ) {
        return MathHelper.sqrt(Math.pow(biomeX - posX, 2) + Math.pow(biomeZ - posZ, 2));
    }

}
