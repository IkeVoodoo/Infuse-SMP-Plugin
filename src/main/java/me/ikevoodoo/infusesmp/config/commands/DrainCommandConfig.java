package me.ikevoodoo.infusesmp.config.commands;

import me.ikevoodoo.infusesmp.config.Config;
import org.bukkit.configuration.ConfigurationSection;

public class DrainCommandConfig extends Config {

    private boolean allowPickingEffects;
    private boolean drainIntoNegatives;

    public boolean shouldAllowPickingEffects() {
        return allowPickingEffects;
    }

    public boolean shouldDrainIntoNegatives() {
        return drainIntoNegatives;
    }

    public void loadFrom(ConfigurationSection section) {
        this.allowPickingEffects = section.getBoolean("allowPickingEffects", false);
        this.drainIntoNegatives = section.getBoolean("drainIntoNegatives", true);
    }

}
