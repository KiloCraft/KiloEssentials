package org.kilocraft.essentials.api.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.EssentialPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.chat.LangText;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.user.User;
import org.kilocraft.essentials.chat.KiloChat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.string;

public abstract class EssentialCommand implements IEssentialCommand {
    private String label;
    protected transient String alias[];
    protected transient KiloEssentials essentials;
    protected transient LiteralArgumentBuilder<ServerCommandSource> argumentBuilder;
    protected transient LiteralCommandNode<ServerCommandSource> commandNode;
    protected transient Server server;
    protected transient static final Logger logger = LogManager.getLogger();
    protected transient Predicate<ServerCommandSource> PERMISSION_CHECK_ROOT;
    protected transient CommandPermission PERMISSION;
    protected transient int MIN_OP_LEVEL;

    public EssentialCommand() {
    }

    public EssentialCommand(final String label) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = literal(label).requires(PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = literal(label).requires(PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, Predicate<ServerCommandSource> predicate) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.argumentBuilder = literal(label).requires(PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, Predicate<ServerCommandSource> predicate, String[] alias) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.alias = alias;
        this.argumentBuilder = literal(label).requires(PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, CommandPermission PERMISSION) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, PERMISSION);
        this.argumentBuilder = literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
        this.PERMISSION = PERMISSION;
    }

    public EssentialCommand(final String label, CommandPermission PERMISSION, int minOpLevel) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, PERMISSION, minOpLevel);
        this.argumentBuilder = literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
        this.PERMISSION = PERMISSION;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    public EssentialCommand(final String label, CommandPermission PERMISSION, String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, PERMISSION);
        this.argumentBuilder = literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
        this.PERMISSION = PERMISSION;
    }

    public EssentialCommand(final String label, CommandPermission PERMISSION, int minOpLevel, String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> KiloCommands.hasPermission(src, PERMISSION, minOpLevel);
        this.argumentBuilder = literal(label).requires(this.PERMISSION_CHECK_ROOT);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
        this.PERMISSION = PERMISSION;
        this.MIN_OP_LEVEL = minOpLevel;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String[] getAlias() {
        return this.alias;
    }

    public LiteralCommandNode<ServerCommandSource> getCommandNode() {
        return this.commandNode;
    }

    public LiteralArgumentBuilder<ServerCommandSource> getArgumentBuilder() {
        return this.argumentBuilder;
    }

    public Predicate<ServerCommandSource> getRootPermissionPredicate() {
        return this.PERMISSION_CHECK_ROOT;
    }

    public boolean hasPermission(ServerCommandSource src, CommandPermission cmdPerm) {
        return KiloCommands.hasPermission(src, cmdPerm);
    }

    public boolean hasPermission(ServerCommandSource src, CommandPermission cmdPerm, int minOpLevel) {
        return KiloCommands.hasPermission(src, cmdPerm, minOpLevel);
    }

    public boolean hasPermission(ServerCommandSource src, EssentialPermission essPerm) {
        return KiloEssentials.hasPermissionNode(src, essPerm);
    }

    public boolean hasPermission(ServerCommandSource src, EssentialPermission essPerm, int minOpLevel) {
        return KiloEssentials.hasPermissionNode(src, essPerm, minOpLevel);
    }

    public void sendMessage(CommandContext<ServerCommandSource> ctx, String key, Object... objects) {
        KiloChat.sendLangMessageTo(ctx.getSource(), key, objects);
    }

    public void sendMessage(ServerCommandSource src, String key, Object... objects) {
        KiloChat.sendLangMessageTo(src, key, objects);
    }

    public Text getLang(String key, Object... objects) {
        return LangText.getFormatter(true, key, objects);
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> literal(String label) {
        return CommandManager.literal(label);
    }

    @Override
    public <T> RequiredArgumentBuilder<ServerCommandSource, T> argument(String string, ArgumentType<T> argumentType) {
        return CommandManager.argument(string, argumentType);
    }

    @Override
    public OnlineUser getOnlineUser(String name) {
        return server.getOnlineUser(name);
    }

    @Override
    public OnlineUser getOnlineUser(ServerCommandSource source) throws CommandSyntaxException {
        return server.getOnlineUser(source.getPlayer());
    }

    public OnlineUser getOnlineUser(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        return getOnlineUser(ctx.getSource());
    }

    public OnlineUser getOnlineUser(ServerPlayerEntity player) throws CommandSyntaxException {
        return server.getOnlineUser(player);
    }

    public CompletableFuture<User> getOfflineUser(GameProfile profile) {
        return server.getUserManager().getOffline(profile);
    }

    public CompletableFuture<User> getOfflineUser(String name) {
        return server.getUserManager().getOffline(name);
    }

    public boolean isOnline(User user) {
        return server.getUserManager().isOnline(user);
    }

    public RequiredArgumentBuilder<ServerCommandSource, String> getUserArgument(String label) {
        return argument(label, string()).suggests(TabCompletions::allPlayers);
    }

}
