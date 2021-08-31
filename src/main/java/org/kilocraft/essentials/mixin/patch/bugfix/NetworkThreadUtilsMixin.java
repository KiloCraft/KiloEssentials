package org.kilocraft.essentials.mixin.patch.bugfix;

import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkThreadUtils.class)
public abstract class NetworkThreadUtilsMixin {

    /**
     * {@link NetworkThreadUtils#forceMainThread(Packet, PacketListener, ServerWorld)} is a method used by
     * {@link net.minecraft.server.network.ServerPlayNetworkHandler} to make sure all incoming client packets are
     * processed on the main server thread. It works by checking if the current thread is equal to the server thread,
     * see {@link ThreadExecutor#isOnThread}. The method, which determines whether code should be run on the server
     * or on the current thread is however overwritten by {@link MinecraftServer#shouldExecuteAsync()}. This overwrite
     * adds another check to {@link MinecraftServer#isStopped()}. Because of this {@link ThreadExecutor#execute} wont
     * redirect the packet to the main server thread. This causes a {@link StackOverflowError} on the Netty Thread.
     * A simple fix for this is to simply ignore all packets during server shutdown.
     */
    @Inject(
            method = "forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static <T extends PacketListener> void fixStackOverFlowOnShutdown(Packet<T> packet, T listener, ServerWorld world, CallbackInfo ci) {
        if (!world.getServer().isRunning()) ci.cancel();
    }

}
