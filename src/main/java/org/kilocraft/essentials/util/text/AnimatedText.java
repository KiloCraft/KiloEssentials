package org.kilocraft.essentials.util.text;

import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnimatedText {
    private List<Text> frames;
    private int frame;
    private Runnable runnable;
    private ScheduledExecutorService executorService;
    private TimeUnit timeUnit;
    private int initialDelay;
    private int nextFrameTime;
    private ServerPlayerEntity player;
    private TitleS2CPacket.Action action;

    public AnimatedText(int initialDelay, int delay, TimeUnit timeUnit, ServerPlayerEntity player, TitleS2CPacket.Action titleAction) {
        this.frames = new ArrayList<>();
        this.timeUnit = timeUnit;
        this.initialDelay = initialDelay;
        this.nextFrameTime = delay;
        this.player = player;
        this.action = titleAction;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public AnimatedText append(Text text) {
        this.frames.add(text);
        return this;
    }

    public List<Text> getFrames() {
        return this.frames;
    }

    public AnimatedText setStyle(Style style) {
        for (Text frame : this.frames) {
            ((MutableText)frame).setStyle(style);
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

            if (this.player.networkHandler != null) {
                this.player.networkHandler.sendPacket(new TitleS2CPacket(this.action, this.frames.get(this.frame),1, this.nextFrameTime, -1));
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
            this.player.networkHandler.sendPacket(new TitleS2CPacket(this.action, new LiteralText("")));
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
        this.action = null;
        this.timeUnit = null;
    }

}
