package org.kilocraft.essentials.craft.mixin;

import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.network.packet.CommandTreeS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(CommandManager.class)
public abstract class MixinCommandManager {

    @Shadow @Final private CommandDispatcher<ServerCommandSource> dispatcher;

    @Shadow protected abstract void makeTreeForSource(CommandNode<ServerCommandSource> commandNode_1, CommandNode<CommandSource> commandNode_2, ServerCommandSource serverCommandSource_1, Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map_1);

//    @Inject(
//            method = "literal", cancellable = true,
//            at = @At(value = "RETURN", target = "Lnet/minecraft/server/command/CommandManager;literal(Ljava/lang/String;)Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;")
//    )

    @Inject(
            method = "sendCommandTree", cancellable = true,
            at = @At(value = "HEAD", target = "Lnet/minecraft/server/command/CommandManager;sendCommandTree(Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    )

    private void modify(ServerPlayerEntity serverPlayerEntity_1, CallbackInfo ci) {
        Map<CommandNode<ServerCommandSource>, CommandNode<CommandSource>> map_1 = Maps.newHashMap();
        RootCommandNode<CommandSource> rootCommandNode_1 = new RootCommandNode();
        map_1.put(this.dispatcher.getRoot(), rootCommandNode_1);
        this.makeTreeForSource(this.dispatcher.getRoot(), rootCommandNode_1, serverPlayerEntity_1.getCommandSource(), map_1);
        serverPlayerEntity_1.networkHandler.sendPacket(new CommandTreeS2CPacket(rootCommandNode_1));
    }
}
