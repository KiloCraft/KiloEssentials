package org.kilocraft.essentials.extensions.vanish;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class VanishSettings {
    private boolean enableEventMessages;
    private boolean nightVision;
    private boolean showBossbar;
    private boolean disablePrivateMessages;
    private boolean pickupItems;
    private boolean invulnerable;
    private boolean ignoreEvents;

    public VanishSettings(boolean enableEventMessages, boolean nightVision, boolean showBossbar,
                          boolean disablePrivateMessages, boolean pickupItems, boolean invulnerable,
                          boolean ignoreEvents) {
        this.enableEventMessages = enableEventMessages;
        this.nightVision = nightVision;
        this.showBossbar = showBossbar;
        this.disablePrivateMessages = disablePrivateMessages;
        this.pickupItems = pickupItems;
        this.invulnerable = invulnerable;
        this.ignoreEvents = ignoreEvents;
    }

    protected CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("enableEventMessages", this.enableEventMessages);
        tag.putBoolean("nightVision", this.nightVision);
        tag.putBoolean("bossbar", this.showBossbar);
        tag.putBoolean("disablePrivateMessages", this.disablePrivateMessages);
        tag.putBoolean("pickupItems", this.pickupItems);
        tag.putBoolean("invulnerable", this.invulnerable);
        tag.putBoolean("ignoreEvents", this.ignoreEvents);

        return tag;
    }

    protected void deserialize(@NotNull CompoundTag compoundTag) {
        this.enableEventMessages = compoundTag.getBoolean("enableEventMessages");
        this.nightVision = compoundTag.getBoolean("nightVision");
        this.showBossbar = compoundTag.getBoolean("bossbar");
        this.disablePrivateMessages = compoundTag.getBoolean("disablePrivateMessages");
        this.pickupItems = compoundTag.getBoolean("pickupItems");
        this.invulnerable = compoundTag.getBoolean("invulnerable");
        this.ignoreEvents = compoundTag.getBoolean("ignoreEvents");
    }

    public void setEnableEventMessages(boolean enableEventMessages) {
        this.enableEventMessages = enableEventMessages;
    }

    public void setNightVisionEnabled(boolean nightVision) {
        this.nightVision = nightVision;
    }

    public void setShowBossbar(boolean showBossbar) {
        this.showBossbar = showBossbar;
    }

    public void setDisablePrivateMessages(boolean disablePrivateMessages) {
        this.disablePrivateMessages = disablePrivateMessages;
    }

    public void setPickupItemsEnabled(boolean pickupItems) {
        this.pickupItems = pickupItems;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void setIgnoreEvents(boolean ignoreEvents) {
        this.ignoreEvents = ignoreEvents;
    }

    public boolean enableEventMessages() {
        return enableEventMessages;
    }

    public boolean isNightVisionEnabled() {
        return nightVision;
    }

    public boolean showBossbar() {
        return showBossbar;
    }

    public boolean isDisabledPrivateMessages() {
        return disablePrivateMessages;
    }

    public boolean pickupItems() {
        return pickupItems;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean ignoreEvents() {
        return ignoreEvents;
    }

}
