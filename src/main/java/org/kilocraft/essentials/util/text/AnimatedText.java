package org.kilocraft.essentials.util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;

public class AnimatedText {
    private List<Component> frames;
    private int frame;
    private Runnable runnable;
    private ScheduledExecutorService executorService;
    private TimeUnit timeUnit;
    private int initialDelay;
    private int nextFrameTime;
    private ServerPlayer player;

    public AnimatedText(int initialDelay, int delay, TimeUnit timeUnit, ServerPlayer player) {
        this.frames = new ArrayList<>();
        this.timeUnit = timeUnit;
        this.initialDelay = initialDelay;
        this.nextFrameTime = delay;
        this.player = player;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public AnimatedText append(Component text) {
        this.frames.add(text);
        return this;
    }

    public List<Component> getFrames() {
        return this.frames;
    }

    public AnimatedText setStyle(Style style) {
        for (Component frame : this.frames) {
            ((MutableComponent) frame).setStyle(style);
        }

        return this;
    }

    public AnimatedText build() {
        this.runnable = () -> {
            if (this.player == null) {
                this.remove();
                Thread.currentThread().interrupt();
            }

            if (this.frame >= this.frames.size()) {
                this.frame = 0;
            }

            if (this.player.connection != null) {
                this.player.connection.send(new ClientboundSetTitlesAnimationPacket(1, this.nextFrameTime, -1));
                this.player.connection.send(new ClientboundSetActionBarTextPacket(this.frames.get(this.frame)));
            }
            this.frame++;
        };

        return this;
    }

    public AnimatedText start() {
        if (this.runnable == null)
            throw new IllegalStateException("You have to build the AnimatedText before starting it!");

        this.executorService.scheduleAtFixedRate(this.runnable, this.initialDelay, this.nextFrameTime, this.timeUnit);
        return this;
    }

    public void stop() {
        this.executorService.shutdown();

        if (this.player != null)
            this.player.connection.send(new ClientboundSetActionBarTextPacket(new TextComponent("")));
    }

    public void remove() {
        this.stop();
        this.frames = null;
        this.executorService = null;
        this.frame = 0;
        this.nextFrameTime = 0;
        this.initialDelay = 0;
        this.player = null;
        this.runnable = null;
        this.timeUnit = null;
    }


}
