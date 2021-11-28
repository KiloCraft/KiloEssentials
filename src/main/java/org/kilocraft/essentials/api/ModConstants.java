package org.kilocraft.essentials.api;

import org.apache.commons.lang3.StringEscapeUtils;
import org.kilocraft.essentials.provided.KiloFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class ModConstants {
    private static final Properties properties = new Properties();
    private static final Properties lang = new Properties();

    public static List<String> getCensored() {
        return censored;
    }

    private static final List<String> censored = new ArrayList<>();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    private static final String censoredFile = "censored.txt";
    private static final String langFile = "en_us.properties";
    private static final InputStream langInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("assets/lang/" + langFile);

    private ModConstants() {}

    public static void loadConstants() {
        try {
            properties.load(ModConstants.class.getClassLoader().getResourceAsStream("mod.properties"));
            loadFiles();
        } catch (final IOException e) {
            KiloEssentials.getLogger().error("There was an error loading the mod properties file", e);
        }
    }

    public static void loadFiles() {
        loadCensoredList();
        loadLanguage();
    }

    private static void loadCensoredList() {
        KiloFile CENSORED_FILE = new KiloFile(censoredFile, KiloEssentials.getEssentialsPath());
        if (!CENSORED_FILE.exists()) {
            CENSORED_FILE.createFile();
            CENSORED_FILE.pasteFromResources("assets/config/" + censoredFile);
        }
        censored.clear();
        try {
            censored.addAll(Files.readAllLines(CENSORED_FILE.getFile().toPath()));
        } catch (IOException e) {
            KiloEssentials.getLogger().error("There was an error loading the censored words file", e);
        }
    }

    private static void loadLanguage() {
        // TODO: Make languages configurable
        KiloFile LANG_FILE = new KiloFile(langFile, KiloEssentials.getLangDirPath());
        if (!LANG_FILE.exists()) {
            LANG_FILE.createFile();
        }
        try {
            // Make sure new values get added
            assert langInputStream != null;
            lang.load(new InputStreamReader(langInputStream, StandardCharsets.UTF_8));
            // Load properties from language file
            lang.load(new InputStreamReader(new FileInputStream(LANG_FILE.getFile()), StandardCharsets.UTF_8));
            // Save changes to language file
            saveLanguageFile(LANG_FILE.getFile());
        } catch (FileNotFoundException ignored) {
        } catch (IOException e) {
            KiloEssentials.getLogger().error("There was an error loading the language file", e);
        }
    }

    private static void saveLanguageFile(File file) {
        try {
            FileOutputStream os = new FileOutputStream(file);
            // Add comment
            os.write(("#KiloEssentials language file" + System.lineSeparator()).getBytes());
            List<Object> sorted = new ArrayList<>(lang.keySet());
            sorted.sort(null);
            for (Object key : sorted) {
                // Save properties
                os.write((
                            key +
                            "=" +
                            StringEscapeUtils.escapeJava(lang.getProperty((String) key)) +
                            System.lineSeparator()
                        ).getBytes()
                );
            }
            os.flush();
        } catch (IOException e) {
            KiloEssentials.getLogger().error("There was an error saving the language file", e);
        }
    }

    public static String translation(final String key) {
        String property = ModConstants.lang.getProperty(key);
        return property != null ? property : key;
    }

    public static String translation(final String key, final Object... objects) {
        return String.format(translation(key), objects);
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
        return ModConstants.properties.getProperty("fabric_mappings");
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
