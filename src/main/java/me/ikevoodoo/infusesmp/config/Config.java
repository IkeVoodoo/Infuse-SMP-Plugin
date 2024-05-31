package me.ikevoodoo.infusesmp.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class Config {

    protected final ConfigurationSection getSection(ConfigurationSection section, String key) {
        var sub = section.getConfigurationSection(key);
        if (sub == null) {
            return new YamlConfiguration();
        }

        return sub;
    }

}
