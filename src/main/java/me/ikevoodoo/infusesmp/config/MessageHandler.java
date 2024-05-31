package me.ikevoodoo.infusesmp.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {

    private final Map<String, MessageConfig> configs = new HashMap<>();

    public MessageConfig getConfig(@Nullable Player player) {
        var locale = player == null ? "en_us" : player.getLocale();
        var config = this.configs.get(locale);
        if (config == null) {
            return this.configs.getOrDefault("en_us", new MessageConfig(new YamlConfiguration()));
        }

        return config;
    }

    public void reload(File folder) {
        this.configs.clear();

        if (!folder.exists() && !folder.mkdirs()) {
            return;
        }

        if (!folder.isDirectory()) {
            throw new IllegalStateException("Unable to load messages, file is not a folder: " + folder.getAbsolutePath());
        }

        this.ensureDefaults(folder);

        var files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) continue;

            var name = file.getName();
            if (!name.endsWith(".yml")) continue;

            try {
                var slash = name.lastIndexOf('/');
                var slashIndex = slash == -1 ? 0 : slash;
                var withoutExtension = name.substring(slashIndex, name.length() - 4);

                var configuration = YamlConfiguration.loadConfiguration(file);
                this.configs.put(withoutExtension, new MessageConfig(configuration));
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void ensureDefaults(File folder) {
        try {
            this.ensureDefault(folder, "en_us");
            this.ensureDefault(folder, "it_it");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ensureDefault(File folder, String lang) throws IOException {
        if (this.configs.containsKey(lang)) return;

        var output = new File(folder, lang + ".yml").getAbsoluteFile();
        if (output.isFile()) return;

        try (var resource = getClass().getResourceAsStream("/languages/" + lang + ".yml");
             var fc = FileChannel.open(output.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            if (resource == null) return;

            fc.transferFrom(Channels.newChannel(resource), 0, Long.MAX_VALUE);
        }
    }

}
