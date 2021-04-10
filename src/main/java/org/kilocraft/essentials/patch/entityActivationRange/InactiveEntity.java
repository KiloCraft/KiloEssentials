package org.kilocraft.essentials.patch.entityActivationRange;

public interface InactiveEntity {

    public void inactiveTick();

    public boolean isTemporarilyActive();

    public void setTemporarilyActive(boolean temporarilyActive);

}
