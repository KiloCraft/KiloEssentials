package org.kilocraft.essentials.commands.debug;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.SharedConstants;
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
import org.kilocraft.essentials.util.Pager;
import org.kilocraft.essentials.util.Texter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugEssentialsCommand extends EssentialCommand {
    public DebugEssentialsCommand() {
        super("debugess", src -> src.hasPermissionLevel(4) && (SharedConstants.isDevelopment || KiloConfig.getMainNode().getNode("debug").getBoolean()));
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> cacheArgument = literal("cache")
                .executes(ctx -> Cache.showList(ctx, 1))
                .then(
                        argument("cached", StringArgumentType.string())
                                .suggests(Cache::cacheIds)
                                .executes(Cache::showInfo)
                )
                .then(
                        literal("-p")
                                .then(
                                        argument("page", IntegerArgumentType.integer(1))
                                                .executes(ctx -> Cache.showList(ctx, IntegerArgumentType.getInteger(ctx, "page")))
                                )
                );

        LiteralArgumentBuilder<ServerCommandSource> modeArgument = literal("mode")
                .then(
                        argument("set", BoolArgumentType.bool())
                                .executes(Debug::setMode)
                );

        this.commandNode.addChild(cacheArgument.build());
        this.commandNode.addChild(modeArgument.build());
    }

    private static class Cache {
        private static final String FIRST_LINE = "&e0.&f Cached<capture of ?> &bObject &8:&d true&7 -> [ &6&onull&r&7 ]";
        private static final String LINE_FORMAT = "&e%s.&f %s &b%s &8:&d %s&7 -> ";
        private static final String DATA_FORMAT = "&7[ &6%s &7]";

        public static int showList(final CommandContext<ServerCommandSource> ctx, int page) {
            TextInput input = new TextInput();
            input.append(FIRST_LINE);

            AtomicInteger atomicInteger = new AtomicInteger();
            CacheManager.getMap().forEach((id, cached) -> {
                atomicInteger.getAndIncrement();
                Object o = cached.get();
                String type = o == null ? "null" : o.getClass().getSimpleName();
                String data = o == null ? "null" : o.toString();
                String id1 = id.length() > 20 ? id.substring(0, Math.min(id.length(), 20)) : id;
                if (id.length() > 20) {
                    id1 = id1 + "...";
                }

                input.append(
                        Texter.toText(String.format(LINE_FORMAT, atomicInteger.get(), id1, type, cached.isValid())).styled((style) -> {
                            style.setHoverEvent(Texter.Events.onHover(id));
                        }).append(
                                Texter.toText(String.format(DATA_FORMAT, data.substring(0, Math.min(data.length(), 10)))).styled((style) -> {
                                    style.setHoverEvent(Texter.Events.onHover(data));
                                    style.setClickEvent(Texter.Events.onClickRun("/debugess cache " + id));
                                })
                        )
                );
            });

            Pager.Page paged = Pager.getPageFromText(Pager.Options.builder().setPageIndex(page - 1).build(), input.getTextLines());
            paged.send(ctx.getSource(), "CACHED DATA", "/debugess cache %page%");
            return SINGLE_SUCCESS;
        }

        private static final String FORMAT = "&6Cached&b<&7capture of &b%s> &9%s &b {\n";
        private static final String FORMAT1 = "\n&b}";
        public static int showInfo(final CommandContext<ServerCommandSource> ctx) {
            String id = StringArgumentType.getString(ctx, "cached");
            Cached<?> cached = CacheManager.get(id);

            if (cached == null) {
                ctx.getSource().sendError(Texter.toText("&cCache doesn't exist"));
                return -1;
            }

            Object o = cached.get();
            String type = o == null ? "null" : o.getClass().getSimpleName();
            String data = o == null ? "null" : o.toString();

            ctx.getSource().sendFeedback(
                    Texter.toText(String.format(FORMAT, type, id))
                            .append(new LiteralText(data).formatted(Formatting.WHITE).append(Texter.toText(FORMAT1)))
                    , false);

            return SINGLE_SUCCESS;
        }

        public static CompletableFuture<Suggestions> cacheIds(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            List<String> strings = new ArrayList<>();
            CacheManager.getMap().forEach((id, cached) -> {
                strings.add(id);
            });
            return CommandSource.suggestMatching(strings, builder);
        }

    }

    private static class Debug {
        public static int setMode(final CommandContext<ServerCommandSource> ctx) {
            boolean set = BoolArgumentType.getBool(ctx, "set");

            KiloDebugUtils.setDebugMode(set);
            KiloChat.sendMessageTo(ctx.getSource(), Texter.toText("&eSet debug mode to &6" + set));
            return 1;
        }
    }
}
