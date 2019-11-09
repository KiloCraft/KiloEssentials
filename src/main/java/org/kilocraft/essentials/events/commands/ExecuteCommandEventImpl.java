package org.kilocraft.essentials.events.commands;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import org.kilocraft.essentials.api.event.commands.ExecuteCommandEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class ExecuteCommandEventImpl implements ExecuteCommandEvent {

    private ServerCommandSource source;
    private String command;
    private boolean isCanceled;
    private CallbackInfoReturnable<Integer> cir;

    public ExecuteCommandEventImpl(ServerCommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
        this.command = command;
        this.source = source;
        this.cir = cir;
    }

    @Override
    public String getCommand() {
        return this.command;
    }

    @Override
    public ServerCommandSource getSource() {
        return source;
    }

    @Override
    public MinecraftServer getServer() {
        return source.getMinecraftServer();
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean isCancelled) {
        this.cir.cancel();
        this.isCanceled = isCancelled;
    }
}
