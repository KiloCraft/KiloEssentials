package org.kilocraft.essentials.api.feature;

public class FeatureType<F extends ConfigurableFeature> {
    private FeatureType(Class<F> type) {
        this.type = type;
    }
    private Class<F> type;
    private String id;

    public Class<F> getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public static <F extends ConfigurableFeature> FeatureType<F> create(Class<F> type, String id) {
        return new FeatureType<>(type);
    }
}
