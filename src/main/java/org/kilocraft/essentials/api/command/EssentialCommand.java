package org.kilocraft.essentials.api.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kilocraft.essentials.CommandPermission;
import org.kilocraft.essentials.KiloCommands;
import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.server.Server;
import org.kilocraft.essentials.api.user.OnlineUser;

import java.util.function.Predicate;

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
        this.argumentBuilder = literal(label);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, String[] alias) {
        this.label = label;
        this.alias = alias;
        this.PERMISSION_CHECK_ROOT = src -> true;
        this.argumentBuilder = literal(label);
        this.commandNode = this.argumentBuilder.build();
        this.server = KiloEssentials.getServer();
    }

    public EssentialCommand(final String label, Predicate<ServerCommandSource> predicate) {
        this.label = label;
        this.PERMISSION_CHECK_ROOT = predicate;
        this.argumentBuilder = literal(label);
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

    public CommandPermission getPERMISSION() {
        return this.PERMISSION;
    }

    public int getMinOpLevelRequired() {
        return this.MIN_OP_LEVEL;
    }

    public Predicate<ServerCommandSource> getRootPermissionPredicate() {
        return this.PERMISSION_CHECK_ROOT;
    }

    public void setPermissionPredicate(Predicate<ServerCommandSource> predicate) {
        this.PERMISSION_CHECK_ROOT = predicate;
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

}
