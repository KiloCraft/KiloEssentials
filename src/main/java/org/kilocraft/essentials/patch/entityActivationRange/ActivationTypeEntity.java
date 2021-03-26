package org.kilocraft.essentials.patch.entityActivationRange;

public interface ActivationTypeEntity {

    public ActivationRange.ActivationType getActivationType();
    public boolean getDefaultActivationState();
    public int getActivatedTick();
    public void setActivatedTick(int activatedTick);

}
