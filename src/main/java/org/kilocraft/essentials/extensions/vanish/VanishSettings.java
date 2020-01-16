package org.kilocraft.essentials.extensions.vanish;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.kilocraft.essentials.user.ServerUser;

public class VanishSettings {
    private ServerUser user;
    private boolean enableEventMessages = false;
    private boolean nightVision = true;
    private boolean showBossbar = true;
    private boolean disablePrivateMessages = true;
    private boolean pickupItems = false;
    private boolean invulnerable = true;
    private boolean ignoreEvents = true;
    private boolean canDamageOthers = false;

    VanishSettings(ServerUser user) {
        this.user = user;
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
        tag.putBoolean("canDamageOthers", this.canDamageOthers);

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
        this.canDamageOthers = compoundTag.getBoolean("canDamageOthers");
    }

    public static String[] getKeys() {
        return new String[]{"enableEventMessages", "nightVision", "showBossbar", "disablePrivateMessage",
                "pickupItems", "invulnerable", "ignoreEvents", "canDamageOthers"};
    }

    public static boolean isValidKey(String key) {
        for (String s : getKeys()) {
            if (key.equals(s))
                return true;
        }

        return false;
    }

    public void setEnableEventMessages(boolean enableEventMessages) {
        this.enableEventMessages = enableEventMessages;
    }

    public void setNightVision(boolean nightVision) {
        this.nightVision = nightVision;
    }

    public void setShowBossbar(boolean showBossbar) {
        this.showBossbar = showBossbar;
    }

    public void setDisablePrivateMessages(boolean disablePrivateMessages) {
        this.disablePrivateMessages = disablePrivateMessages;
    }

    public void setPickupItems(boolean pickupItems) {
        this.pickupItems = pickupItems;
    }

    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
    }

    public void setIgnoreEvents(boolean ignoreEvents) {
        this.ignoreEvents = ignoreEvents;
    }

    public void setCanDamageOthers(boolean canDamageOthers) {
        this.canDamageOthers = canDamageOthers;
    }


    public boolean isEnableEventMessages() {
        return enableEventMessages;
    }

    public boolean isNightVision() {
        return nightVision;
    }

    public boolean isShowBossbar() {
        return showBossbar;
    }

    public boolean isDisablePrivateMessages() {
        return disablePrivateMessages;
    }

    public boolean isPickupItems() {
        return pickupItems;
    }

    public boolean isInvulnerable() {
        return invulnerable;
    }

    public boolean isIgnoreEvents() {
        return ignoreEvents;
    }

    public boolean isCanDamageOthers() {
        return canDamageOthers;
    }

}
