package me.ikevoodoo.infusesmp.config;

import me.ikevoodoo.infusesmp.effects.PotionType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageConfig {

    private final String positiveColor;
    private final String negativeColor;

    private final String receiveMessage;
    private final String loseMessage;


    // private final String sparkMenuTitle;
    private final String sparkUpgradeMessage;
    private final String sparkPotionName;
    private final String sparkNoEffect;
    private final String sparkAlreadyUpgraded;

    private final String sparkNavigationFormat;
    private final String sparkMoveLeftFormat;
    private final String sparkMoveRightFormat;


    private final String goodPotionSuccess;
    private final String goodPotionFailure;

    private final String amplifyEnded;

    private final String drainTooManyNegatives;
    private final String drainNoDebts;
    private final String drainNegativeGiven;
    private final String drainSuccess;
    private final String drainedPotionName;


    private final Map<PotionType, String> potionNames = new HashMap<>();

    protected MessageConfig(YamlConfiguration configuration) {
        this.positiveColor = configuration.getString("colors.positiveEffect", "§e");
        this.negativeColor = configuration.getString("colors.negativeEffect", "§e");

        this.receiveMessage = configuration.getString("combat.receiveEffect", "§aYou received {{effect}}");
        this.loseMessage = configuration.getString("combat.loseEffect", "§aYou lost {{effect}}");

        this.sparkUpgradeMessage = configuration.getString("spark.upgrade.successful", "§aYou have upgraded {{effect}}§a to level {{level}}!");
        this.sparkPotionName = configuration.getString("spark.upgrade.prompt", "§aUpgrade {{effect}}");
        this.sparkNoEffect = configuration.getString("spark.upgrade.none", "§4NO EFFECT");
        this.sparkAlreadyUpgraded = configuration.getString("spark.upgrade.alreadyUpgraded", "§cAlready upgraded {{effect}}§c!");

        this.sparkNavigationFormat = configuration.getString("spark.navigation.format", "§fPage {{page}} / {{totalPages}}");
        this.sparkMoveLeftFormat = configuration.getString("spark.navigation.left", "§7Press §6LEFT CLICK §7to go to the previous page.");
        this.sparkMoveRightFormat = configuration.getString("spark.navigation.right", "§7Press §6RIGHT CLICK §7to go to the next page.");

        this.goodPotionSuccess = configuration.getString("goodPotion.success", "§aRemoved negative effect {{effect}}§c!");
        this.goodPotionFailure = configuration.getString("goodPotion.failure", "§cYou do not have any negative effects!");

        this.amplifyEnded = configuration.getString("effectBonusEnded", "§cThe upgrade you made to {{effect}}§c has ran out, it is now level {{level}}!");

        this.drainTooManyNegatives = configuration.getString("drain.drainedTooMany", "§cYou have too many negative effects to be able to drain a new one!");
        this.drainNoDebts = configuration.getString("drain.noNegativeEffects", "§cYou have no positive effects to drain!");
        this.drainNegativeGiven = configuration.getString("drain.negativeAdded", "§cYou were given {{effect}}§c as you do not have any positive effects!");
        this.drainSuccess = configuration.getString("drain.success", "§aYou drained {{effect}}");
        this.drainedPotionName = configuration.getString("drain.potionName", "§aObtain {{effect}}");

        ConfigurationSection potionNames = configuration.getConfigurationSection("potionNames");
        if (potionNames == null) {
            potionNames = new YamlConfiguration();
        }

        for (var type : PotionType.values()) {
            String value = potionNames.getString(type.name().toLowerCase());
            if (value == null) {
                potionNames.getString(type.name().toLowerCase(), type.getDisplayName());
            }

            this.potionNames.put(type, value);
        }
    }

    public String getReceiveMessage(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.receiveMessage, type, isNegative);
    }
    public String getLoseMessage(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.loseMessage, type, isNegative);
    }

    public String getSparkUpgradeMessage(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.sparkUpgradeMessage, type, isNegative);
    }
    public String getSparkPotionName(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.sparkPotionName, type, isNegative);
    }
    public String getSparkNoEffect() {
        return sparkNoEffect;
    }
    public String getSparkAlreadyUpgraded(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.sparkAlreadyUpgraded, type, isNegative);
    }

    public String getSparkNavigationFormat(int page, int totalPages) {
        var maxPageLength = String.valueOf(totalPages).length();
        var pageString = String.format("%,@@d".replace("@@", String.valueOf(maxPageLength)), page);
        return this.sparkNavigationFormat.replace("{{page}}", pageString).replace("{{totalPages}}", String.valueOf(totalPages));
    }
    public String getSparkMoveLeftFormat() {
        return this.sparkMoveLeftFormat; }
    public String getSparkMoveRightFormat() {
        return this.sparkMoveRightFormat;
    }

    public String getGoodPotionSuccess(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.goodPotionSuccess, type, isNegative);
    }
    public String getGoodPotionFailure() {
        return this.goodPotionFailure;
    }

    public String getAmplifyEnded(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.amplifyEnded, type, isNegative);
    }

    public String getDrainNoDebts() {
        return drainNoDebts;
    }
    public String getDrainTooManyNegatives() {
        return this.drainTooManyNegatives;
    }
    public String getDrainNegativeGiven(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.drainNegativeGiven, type, isNegative);
    }
    public String getDrainSuccess(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.drainSuccess, type, isNegative);
    }

    public String getDrainedPotionName(PotionType type, boolean isNegative) {
        return this.colorEffectName(this.drainedPotionName, type, isNegative);
    }

    private String colorEffectName(String message, PotionType type, boolean isNegative) {
        var name = this.potionNames.getOrDefault(type, type.getDisplayName());
        var color = isNegative ? this.negativeColor : this.positiveColor;
        var replacement = color + name;
        return message.replace("{{effect}}", replacement);
    }
}
