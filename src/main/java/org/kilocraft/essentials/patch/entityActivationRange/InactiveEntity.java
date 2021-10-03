package org.kilocraft.essentials.patch.entityActivationRange;

public interface InactiveEntity {

    void inactiveTick();

    boolean isTemporarilyActive();

    void setTemporarilyActive(boolean temporarilyActive);

}
