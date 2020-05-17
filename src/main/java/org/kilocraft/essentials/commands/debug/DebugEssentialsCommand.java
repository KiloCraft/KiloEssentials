package org.kilocraft.essentials.commands.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.command.arguments.NbtCompoundTagArgumentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.kilocraft.essentials.KiloDebugUtils;
import org.kilocraft.essentials.api.command.EssentialCommand;
import org.kilocraft.essentials.api.text.TextInput;
import org.kilocraft.essentials.api.util.Cached;
import org.kilocraft.essentials.chat.KiloChat;
import org.kilocraft.essentials.config.KiloConfig;
import org.kilocraft.essentials.util.CacheManager;
import org.kilocraft.essentials.util.text.Pager;
import org.kilocraft.essentials.util.text.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugEssentialsCommand extends EssentialCommand {
    public DebugEssentialsCommand() {
        super("debug", src -> src.hasPermissionLevel(3));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> modeOn = literal("on")
                .executes(ctx -> setDebugMode(ctx, true));
        LiteralArgumentBuilder<ServerCommandSource> modeOff = literal("off")
                .executes(ctx -> setDebugMode(ctx, false));

        LiteralArgumentBuilder<ServerCommandSource> bar = literal("bar");
        {
            LiteralArgumentBuilder<ServerCommandSource> barOn = literal("on")
                    .executes(ctx -> setDebugBar(ctx, true));
            LiteralArgumentBuilder<ServerCommandSource> barOff = literal("off")
                    .executes(ctx -> setDebugBar(ctx, false));

            bar.then(barOn);
            bar.then(barOff);
        }

        this.argumentBuilder.then(modeOn);
        this.argumentBuilder.then(modeOff);
        this.argumentBuilder.then(bar);
    }

    private int setDebugMode(final CommandContext<ServerCommandSource> ctx, boolean set) {
        KiloDebugUtils.setDebugMode(set);
        sendFeedback(ctx, "command.debug.mode", set);
        return set ? 1 : 0;
    }

    private int setDebugBar(final CommandContext<ServerCommandSource> ctx, boolean set) {
        KiloDebugUtils.setDebugBarVisible(set);
        sendFeedback(ctx, set ? "command.debug.bar.visible" : "command.debug.bar.invisible");
        return set ? 1 : 0;
    }

    private void sendFeedback(final CommandContext<ServerCommandSource> ctx, String key, Object... objects) {
        this.getServerUser(ctx).sendLangMessage(key, objects);
    }
}
