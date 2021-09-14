package org.kilocraft.essentials.api;

import org.kilocraft.essentials.provided.KiloFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

public class ModConstants {
    private static final Properties properties = new Properties();
    private static final Properties lang = new Properties();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

    private ModConstants() {}

    public static void loadConstants() {
        try {
            properties.load(ModConstants.class.getClassLoader().getResourceAsStream("mod.properties"));
            loadLanguage();
        } catch (final IOException e) {
            KiloEssentials.getLogger().error("There was an error loading the mod properties file", e);
        }
    }

    public static void loadLanguage() {
        // TODO: Make languages configurable
        String langFile = "en_us.lang";
        KiloFile LANG_FILE = new KiloFile(langFile, KiloEssentials.getLangDirPath());
        if (!LANG_FILE.exists()) {
            LANG_FILE.createFile();
            LANG_FILE.pasteFromResources("assets/lang/" + langFile);
        }
        try {
            lang.load(new FileInputStream(LANG_FILE.getFile()));
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            KiloEssentials.getLogger().error("There was an error loading the language file", e);
        }
    }

    public static String translation(final String key) {
        return ModConstants.lang.getProperty(key);
    }

    public static String translation(final String key, final Object... objects) {
        return String.format(ModConstants.lang.getProperty(key), objects);
    }

    public static InputStream getResourceAsStream(final String path) {
        return ModConstants.class.getClassLoader().getResourceAsStream(path);
    }

    public static Properties getProperties() {
        return ModConstants.properties;
    }

    public static Properties getStrings() {
        return ModConstants.lang;
    }

    public static String getVersion() {
        return ModConstants.properties.getProperty("version");
    }

    public static String getVersionNick() {
        return ModConstants.properties.getProperty("version_nick");
    }

    public static String getVersionType() {
        return ModConstants.properties.getProperty("version_type");
    }

    public static String getVersionInt() {
        return ModConstants.properties.getProperty("version_int");
    }

    public static String getBuildTime() {
        return ModConstants.properties.getProperty("build_time");
    }

    public static String getMappingsVersion() {
        return ModConstants.properties.getProperty("fabric_yarn_mappings");
    }

    public static String getLoaderVersion() {
        return ModConstants.properties.getProperty("fabric_loader_version");
    }

    public static String getMinecraftVersion() {
        return ModConstants.getProperties().getProperty("minecraft_version");
    }

    public static String getGitHash() {
        return ModConstants.properties.getProperty("git_hash");
    }

    public static String getGitHashFull() {
        return ModConstants.properties.getProperty("git_hash_full");
    }

    public static String getGitBranch() {
        return ModConstants.properties.getProperty("git_branch");
    }

    public static String getBuildType() {
        return ModConstants.properties.getProperty("build_type");
    }

    public static int dataFixerSchema() {
        return Integer.parseInt(ModConstants.properties.getProperty("datafixer_schema"));
    }
}
