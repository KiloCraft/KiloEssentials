package org.kilocraft.essentials.api.event.context;

public interface ReturnableContext<R> extends Contextual {
    void setReturnValue(R r);

    R getReturnValue();
}
