package me.ikevoodoo.infusesmp.config.effects;

import me.ikevoodoo.infusesmp.effects.PotionEffectMode;
import me.ikevoodoo.infusesmp.effects.PotionType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public final class EffectConfig {

    private final Map<PotionType, EffectData> positive = new HashMap<>();
    private final Map<PotionType, EffectData> negative = new HashMap<>();

    private final Map<PotionType, EffectData> positiveView = Collections.unmodifiableMap(this.positive);
    private final Map<PotionType, EffectData> negativeView = Collections.unmodifiableMap(this.negative);

    public Map<PotionType, EffectData> getPositive() {
        return this.positiveView;
    }

    public Map<PotionType, EffectData> getNegative() {
        return this.negativeView;
    }


    public boolean isPositive(PotionType type) {
        return this.positive.containsKey(type);
    }

    public boolean isNegative(PotionType type) {
        return this.negative.containsKey(type);
    }

    public boolean knowsAboutEffect(PotionType type) {
        return this.isPositive(type) || this.isNegative(type);
    }

    public void loadFrom(ConfigurationSection section) {
        boolean warnings = section.getBoolean("printWarnings", false);

        this.loadMap(this.positive, section.getConfigurationSection("positive"), warnings, PotionEffectMode.POSITIVE);
        this.loadMap(this.negative, section.getConfigurationSection("negative"), warnings, PotionEffectMode.NEGATIVE);
    }

    private void loadMap(Map<PotionType, EffectData> list, ConfigurationSection entries, boolean warnings, PotionEffectMode targetMode) {
        list.clear();

        if (entries == null) {
            return;
        }

        for (String name : entries.getKeys(false)) {
            ConfigurationSection entry = entries.getConfigurationSection(name);
            if (entry == null) {
                Bukkit.getLogger().log(Level.SEVERE,
                        String.format("Potion effect type '%s' is not a config section!", name)
                );
                continue;
            }

            if (!entry.contains("effectLevel")) {
                Bukkit.getLogger().log(Level.SEVERE,
                        String.format("Potion effect type '%s' does not have a level!", name)
                );
                continue;
            }

            PotionType type = PotionType.valueOf(name.toUpperCase(Locale.ROOT));
            if (type.getEffectMode() != targetMode && warnings) {
                Bukkit.getLogger().log(Level.WARNING,
                        String.format("Potion effect type '%s' is being loaded as %s, when it is actually %s!", type.name(), targetMode, type.getEffectMode())
                );
            }

            if (list.containsKey(type)) {
                Bukkit.getLogger().log(Level.SEVERE,
                        String.format("Potion effect type '%s' is already registered!", name)
                );
                continue;
            }

            list.put(type, new EffectData(
                    type,
                    entry.getInt("duration", -1),
                    Math.max(entries.getInt("effectLevel"), 1) - 1,
                    entries.getBoolean("ambient", true),
                    entries.getBoolean("particles", true),
                    entries.getBoolean("icon", true)
            ));
        }
    }

}
