package org.kilocraft.essentials.util.commands.server;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.world.SaveProperties;
import org.apache.commons.lang3.time.StopWatch;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.ModConstants;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.ComponentText;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.util.Action;
import org.kilocraft.essentials.util.CommandPermission;
import org.kilocraft.essentials.util.text.Texter;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReloadCommand extends EssentialCommand {
    public ReloadCommand() {
        super("reload", CommandPermission.RELOAD, new String[]{"rl"});
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> kiloessentials = literal("kiloessentials").executes(this::reloadKE);
        LiteralArgumentBuilder<ServerCommandSource> vanilla = literal("vanilla").executes(this::reloadVanilla);
        argumentBuilder.then(vanilla);
        argumentBuilder.then(kiloessentials);
        argumentBuilder.executes(this::reload);
    }

    private int reload(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        StopWatch watch = new StopWatch();
        src.sendLangMessage("command.reload.start");

        watch.start();
        reload(ctx, (throwable) -> {
            watch.stop();
            String str = tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
            logger.error(str);
            src.sendMessage(str);
        });

        watch.stop();
        src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        return AWAIT;
    }

    private int reloadVanilla(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        StopWatch watch = new StopWatch();
        src.sendLangMessage("command.reload.vanilla");

        watch.start();
        AtomicBoolean success = new AtomicBoolean(true);
        reloadMinecraftServer(ctx, (throwable) -> {
            watch.stop();
            String str = tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
            logger.error(str);
            src.sendMessage(ComponentText.toText(tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)))).styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.newText(throwable.getMessage())))));
            success.set(false);
        });
        if (success.get()) {
            watch.stop();
            src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        }
        return AWAIT;
    }

    public void reload(CommandContext<ServerCommandSource> ctx, Action<Throwable> fallback) {
        reloadKE(ctx);
        reloadMinecraftServer(ctx, fallback);
    }

    public void reloadMinecraftServer(CommandContext<ServerCommandSource> ctx, Action<Throwable> fallback) {
        MinecraftServer server = ctx.getSource().getServer();
        ResourcePackManager resourcePackManager = server.getDataPackManager();
        SaveProperties saveProperties = server.getSaveProperties();
        Collection<String> collection = resourcePackManager.getEnabledNames();

        Collection<String> modifiedCollection = Lists.newArrayList(collection);
        resourcePackManager.scanPacks();
        for (String string : resourcePackManager.getNames()) {
            if (!saveProperties.getDataPackSettings().getDisabled().contains(string) && !modifiedCollection.contains(string)) {
                modifiedCollection.add(string);
            }
        }

        server.reloadResources(collection).exceptionally((throwable) -> {
            fallback.perform(throwable);
            return null;
        });
    }

    private int reloadKE(CommandContext<ServerCommandSource> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        StopWatch watch = new StopWatch();
        watch.start();
        src.sendLangMessage("command.reload.ke");
        KiloEssentials.getInstance().reload();
        watch.stop();
        src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        return AWAIT;
    }
}
