package org.kilocraft.essentials.api.util;

import com.google.gson.JsonElement;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.*;
import org.kilocraft.essentials.api.Mod;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

public class MinecraftMod {
    private static ModContainer modContainer;
    private static ModMetadata modMetadata;

    public static ModContainer getModContainer() {
        return modContainer;
    }

    public MinecraftMod() {
        modContainer = new ModContainer() {
            @Override
            public ModMetadata getMetadata() {
                return modMetadata;
            }

            @Override
            public Path getRootPath() {
                return null;
            }
        };

        modMetadata = new ModMetadata() {
            @Override
            public String getType() {
                return "universal";
            }

            @Override
            public String getId() {
                return "fabirc-yarn-mappings";
            }

            @Override
            public Version getVersion() {
                return Mod::getMappingsVersion;
            }

            @Override
            public Collection<ModDependency> getDepends() {
                return null;
            }

            @Override
            public Collection<ModDependency> getRecommends() {
                return null;
            }

            @Override
            public Collection<ModDependency> getSuggests() {
                return null;
            }

            @Override
            public Collection<ModDependency> getConflicts() {
                return null;
            }

            @Override
            public Collection<ModDependency> getBreaks() {
                return null;
            }

            @Override
            public String getName() {
                return "Minecraft";
            }

            @Override
            public String getDescription() {
                return "Minecraft source deobfuscation mappings, provided by the fabric-yarn project";
            }

            @Override
            public Collection<Person> getAuthors() {
                return null;
            }

            @Override
            public Collection<Person> getContributors() {
                return null;
            }

            @Override
            public ContactInformation getContact() {
                return null;
            }

            @Override
            public Collection<String> getLicense() {
                return null;
            }

            @Override
            public Optional<String> getIconPath(int i) {
                return Optional.empty();
            }

            @Override
            public boolean containsCustomValue(String s) {
                return false;
            }

            @Override
            public CustomValue getCustomValue(String s) {
                return null;
            }

            @Override
            public boolean containsCustomElement(String s) {
                return false;
            }

            @Override
            public JsonElement getCustomElement(String s) {
                return null;
            }
        };
    }

}
