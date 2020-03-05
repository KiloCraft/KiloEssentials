package org.kilocraft.essentials.api.text;

import org.kilocraft.essentials.api.KiloEssentials;
import org.kilocraft.essentials.api.user.CommandSourceUser;
import org.kilocraft.essentials.api.user.OnlineUser;
import org.kilocraft.essentials.api.util.StringUtils;

import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TextInput implements IText {
    private static final HashMap<String, SoftReference<TextInput>> cache = new HashMap<String, SoftReference<TextInput>>();
    private final transient List<String> lines;
    private final transient List<String> chapters;
    private final transient Map<String, Integer> bookmarks;
    private final transient long lastChange;

    public TextInput(final OnlineUser user, final String filename, final boolean createFile) throws IOException {
        File file = null;
        final CommandSourceUser sender = (CommandSourceUser) user;

        if (!sender.isConsole()) {
            file = new File(KiloEssentials.getDataDirPath().toFile(), filename + "_" + StringUtils.sanitizeFileName(user.getUsername()) + ".txt");
        }

        if (file == null || !file.exists()) {
            file = new File(KiloEssentials.getDataDirPath().toFile(), filename + ".txt");
        }

        if (file.exists()) {
            this.lastChange = file.lastModified();
            final boolean readFromfile;
            synchronized (TextInput.cache) {
                final SoftReference<TextInput> inputRef = TextInput.cache.get(file.getName());
                final TextInput input;
                if (inputRef == null || (input = inputRef.get()) == null || input.lastChange < this.lastChange) {
                    this.lines = new ArrayList<>();
                    this.chapters = new ArrayList<>();
                    this.bookmarks = new HashMap<>();
                    TextInput.cache.put(file.getName(), new SoftReference<>(this));
                    readFromfile = true;
                } else {
                    this.lines = Collections.unmodifiableList(input.getLines());
                    this.chapters = Collections.unmodifiableList(input.getChapters());
                    this.bookmarks = Collections.unmodifiableMap(input.getBookmarks());
                    readFromfile = false;
                }
            }
            if (readFromfile) {
                final Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                final BufferedReader bufferedReader = new BufferedReader(reader);
                try {
                    int lineNumber = 0;
                    while (bufferedReader.ready()) {
                        final String line = bufferedReader.readLine();
                        if (line == null) {
                            break;
                        }
                        if (line.length() > 1 && line.charAt(0) == '#') {
                            final String[] titles = line.substring(1).trim().replace(" ", "_").split(",");
                            this.chapters.add(TextFormat.translate(titles[0]));
                            for (final String title : titles) {
                                this.bookmarks.put(TextFormat.translate(title.toLowerCase(Locale.ENGLISH)), lineNumber);
                            }
                        }
                        this.lines.add(TextFormat.translate(line));
                        lineNumber++;
                    }
                } finally {
                    reader.close();
                    bufferedReader.close();
                }
            }
        } else {
            this.lastChange = 0;
            this.lines = Collections.emptyList();
            this.chapters = Collections.emptyList();
            this.bookmarks = Collections.emptyMap();
            if (createFile) {
                final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename + ".txt");
                final OutputStream output = new FileOutputStream(file);
                try {
                    final byte[] buffer = new byte[1024];
                    int length = input.read(buffer);
                    while (length > 0) {
                        output.write(buffer, 0, length);
                        length = input.read(buffer);
                    }
                } finally {
                    output.close();
                    input.close();
                }

                throw new FileNotFoundException("File " + filename + ".txt does not exist. Creating one for you.");
            }
        }

    }


    @Override
    public IText add(final String add) {
        return null;
    }

    @Override
    public List<String> getLines() {
        return null;
    }

    @Override
    public List<String> getChapters() {
        return null;
    }

    @Override
    public Map<String, Integer> getBookmarks() {
        return null;
    }
}
