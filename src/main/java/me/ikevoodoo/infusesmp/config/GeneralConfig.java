package me.ikevoodoo.infusesmp.config;

import me.ikevoodoo.infusesmp.config.commands.DrainCommandConfig;
import me.ikevoodoo.infusesmp.config.effects.EffectConfig;
import me.ikevoodoo.infusesmp.config.items.ItemConfig;
import org.bukkit.configuration.ConfigurationSection;

public final class GeneralConfig extends Config {

    private final EffectConfig effectConfig = new EffectConfig();
    private final ItemConfig sparkConfig = new ItemConfig("spark_item");
    private final ItemConfig goodPotionConfig = new ItemConfig("good_potion");
    private final DrainCommandConfig drainCommandConfig = new DrainCommandConfig();
    private double effectBoostSeconds;
    private boolean useSass;

    public EffectConfig getEffectConfig() {
        return this.effectConfig;
    }

    public ItemConfig getSparkConfig() {
        return this.sparkConfig;
    }

    public ItemConfig getGoodPotionConfig() {
        return this.goodPotionConfig;
    }

    public DrainCommandConfig getDrainCommandConfig() {
        return drainCommandConfig;
    }

    public double getEffectBoostSeconds() {
        return this.effectBoostSeconds;
    }

    public boolean shouldUseSass() {
        return this.useSass;
    }

    public void loadFrom(ConfigurationSection section) {
        this.effectConfig.loadFrom(getSection(section, "effects"));

        var items = getSection(section, "items");

        this.sparkConfig.loadFrom(getSection(items, "spark"));
        this.goodPotionConfig.loadFrom(getSection(items, "goodPotion"));

        var general = section.getConfigurationSection("general");

        this.effectBoostSeconds = general.getDouble("effectBoostSeconds", 60 * 5);
        this.useSass = general.getBoolean("useSass", false);

        this.drainCommandConfig.loadFrom(getSection(getSection(section, "commands"), "drain"));
    }

}
