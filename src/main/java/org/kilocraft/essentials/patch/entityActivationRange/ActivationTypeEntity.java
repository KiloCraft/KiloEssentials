package org.kilocraft.essentials.patch.entityActivationRange;

public interface ActivationTypeEntity {

    ActivationRange.ActivationType getActivationType();

    boolean getDefaultActivationState();

    int getActivatedTick();

    void setActivatedTick(int activatedTick);

}
