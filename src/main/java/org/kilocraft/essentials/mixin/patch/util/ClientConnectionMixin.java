package org.kilocraft.essentials.mixin.patch.util;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.text.Text;
import org.kilocraft.essentials.api.KiloEssentials;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Queue;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {

    @Shadow
    protected abstract NetworkState getState();

    @Shadow
    public abstract NetworkSide getSide();

    @Shadow
    private SocketAddress address;
    @Shadow
    private Text disconnectReason;
    @Shadow
    private boolean disconnected;
    @Shadow
    private boolean errored;
    @Shadow
    @Final
    private Queue<?> packetQueue;
    @Shadow
    private int ticks;
    boolean hasSentClosedChannelException = false;

    @Inject(method = "exceptionCaught", at = @At("HEAD"))
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable ex, CallbackInfo cb) {
        if (ex instanceof ClosedChannelException) {
            if (!this.hasSentClosedChannelException) {
                this.hasSentClosedChannelException = true;
                this.printCurrentState(ex);
            }
        } else {
            this.printCurrentState(ex);
        }
    }

    private void printCurrentState(Throwable ex) {
        KiloEssentials.getLogger().warn("An error occurred while processing client connection:", ex);
        KiloEssentials.getLogger().warn(
                "Network state: " + this.getState().toString() +
                        "\nNetwork side: " + this.getSide().toString() +
                        "\nQueued packets: " + this.packetQueue.size() +
                        "\nSocket Address: " + this.address.toString() +
                        "\nTicks: " + this.ticks +
                        "\nDisconnected: " + this.disconnected +
                        "\nDisconnect Reason: " + (this.disconnectReason == null ? null : this.disconnectReason.asString()) +
                        "\nErrored: " + this.errored
        );
    }

}
