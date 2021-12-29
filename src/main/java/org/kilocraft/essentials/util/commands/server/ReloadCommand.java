package org.kilocraft.essentials.util.commands.server;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
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

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> kiloessentials = this.literal("kiloessentials").executes(this::reloadKE);
        LiteralArgumentBuilder<CommandSourceStack> vanilla = this.literal("vanilla").executes(this::reloadVanilla);
        this.argumentBuilder.then(vanilla);
        this.argumentBuilder.then(kiloessentials);
        this.argumentBuilder.executes(this::reload);
    }

    private int reload(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        StopWatch watch = new StopWatch();
        src.sendLangMessage("command.reload.start");

        watch.start();
        this.reload(ctx, (throwable) -> {
            watch.stop();
            String str = this.tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
            logger.error(str);
            src.sendMessage(str);
        });

        watch.stop();
        src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        return AWAIT;
    }

    private int reloadVanilla(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceUser src = this.getCommandSource(ctx);
        StopWatch watch = new StopWatch();
        src.sendLangMessage("command.reload.vanilla");

        watch.start();
        AtomicBoolean success = new AtomicBoolean(true);
        this.reloadMinecraftServer(ctx, (throwable) -> {
            watch.stop();
            String str = this.tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
            logger.error(str);
            src.sendMessage(ComponentText.toText(this.tl("command.reload.failed", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)))).withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Texter.newText(throwable.getMessage())))));
            success.set(false);
        });
        if (success.get()) {
            watch.stop();
            src.sendLangMessage("command.reload.end", ModConstants.DECIMAL_FORMAT.format(watch.getTime(TimeUnit.MILLISECONDS)));
        }
        return AWAIT;
    }

    public void reload(CommandContext<CommandSourceStack> ctx, Action<Throwable> fallback) {
        this.reloadKE(ctx);
        this.reloadMinecraftServer(ctx, fallback);
    }

    public void reloadMinecraftServer(CommandContext<CommandSourceStack> ctx, Action<Throwable> fallback) {
        MinecraftServer server = ctx.getSource().getServer();
        PackRepository resourcePackManager = server.getPackRepository();
        WorldData saveProperties = server.getWorldData();
        Collection<String> collection = resourcePackManager.getSelectedIds();

        Collection<String> modifiedCollection = Lists.newArrayList(collection);
        resourcePackManager.reload();
        for (String string : resourcePackManager.getAvailableIds()) {
            if (!saveProperties.getDataPackConfig().getDisabled().contains(string) && !modifiedCollection.contains(string)) {
                modifiedCollection.add(string);
            }
        }

        server.reloadResources(collection).exceptionally((throwable) -> {
            fallback.perform(throwable);
            return null;
        });
    }

    private int reloadKE(CommandContext<CommandSourceStack> ctx) {
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
