package org.kilocraft.essentials.listeners;

import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.kilocraft.essentials.api.event.EventHandler;
import org.kilocraft.essentials.api.event.player.PlayerOnHandSwingEvent;
import org.kilocraft.essentials.config.KiloConfig;

public class OnHandSwing implements EventHandler<PlayerOnHandSwingEvent> {
    @Override
    public void handle(PlayerOnHandSwingEvent event) {
        if (KiloConfig.main().misc().handSwingSound && event.getHand() == Hand.MAIN_HAND) {
            event.getPlayer().networkHandler.sendPacket(
                    new PlaySoundIdS2CPacket(new Identifier("minecraft", "entity.player.attack.nodamage"),
                            SoundCategory.MASTER, event.getPlayer().getPos(), 3.0F, 1.0F));
        }
    }
}
