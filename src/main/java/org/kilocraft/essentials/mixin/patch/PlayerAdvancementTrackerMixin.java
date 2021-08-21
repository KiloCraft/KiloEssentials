package org.kilocraft.essentials.mixin.patch;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import org.kilocraft.essentials.patch.IterationEntryPoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

/**
 * From: PaperMC
 * https://github.com/PaperMC/Paper/blob/master/patches/server/0491-Optimize-the-advancement-data-player-iteration-to-be.patch
 * Copied from:
 * https://github.com/Wesley1808/ServerCore-Fabric/blob/1.17.1/src/main/java/org/provim/servercore/mixin/performance/PlayerAdvancementTrackerMixin.java
 */

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    @Final
    private Set<Advancement> progressUpdates;

    @Shadow
    @Final
    private Map<Advancement, AdvancementProgress> advancementToProgress;

    @Shadow
    @Final
    private Set<Advancement> visibilityUpdates;

    @Shadow
    @Final
    private Set<Advancement> visibleAdvancements;

    @Shadow
    protected abstract boolean canSee(Advancement advancement);

    // Redirect method calls to optimized method
    @Inject(method = "updateDisplay", at = @At("HEAD"), cancellable = true)
    private void updateDisplay(Advancement advancement, CallbackInfo ci) {
        this.updateDisplay(advancement, IterationEntryPoint.ROOT);
        ci.cancel();
    }

    // Optimized updateDisplay method
    private void updateDisplay(Advancement advancement, IterationEntryPoint entryPoint) {
        boolean bl = this.canSee(advancement);
        boolean bl2 = this.visibleAdvancements.contains(advancement);
        if (bl && !bl2) {
            this.visibleAdvancements.add(advancement);
            this.visibilityUpdates.add(advancement);
            if (this.advancementToProgress.containsKey(advancement)) {
                this.progressUpdates.add(advancement);
            }
        } else if (!bl && bl2) {
            this.visibleAdvancements.remove(advancement);
            this.visibilityUpdates.add(advancement);
        }

        if (bl != bl2 && advancement.getParent() != null) {
            // If we're not coming from an iterator consider this to be a root entry, otherwise
            // market that we're entering from the parent of an iterator.
            this.updateDisplay(advancement.getParent(), entryPoint == IterationEntryPoint.ITERATOR ? IterationEntryPoint.PARENT_OF_ITERATOR : IterationEntryPoint.ROOT);
        }

        // If this is true, we've went through a child iteration, entered the parent, processed the parent
        // and are about to reprocess the children. Stop processing here to prevent O(N^2) processing.
        if (entryPoint == IterationEntryPoint.PARENT_OF_ITERATOR) {
            return;
        }

        for (Advancement child : advancement.getChildren()) {
            this.updateDisplay(child, IterationEntryPoint.ITERATOR);
        }
    }
}
