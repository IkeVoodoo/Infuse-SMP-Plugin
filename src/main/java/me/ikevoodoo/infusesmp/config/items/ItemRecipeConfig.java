package me.ikevoodoo.infusesmp.config.items;

import me.ikevoodoo.infusesmp.InfuseSmp;
import me.ikevoodoo.infusesmp.config.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

public final class ItemRecipeConfig extends Config {

    private final String id;
    private Recipe recipe;
    private boolean enabled;

    public ItemRecipeConfig(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void loadFrom(ConfigurationSection section, ItemStack result) {
        this.enabled = section.getBoolean("recipeEnabled", true);

        String recipeOption = section.getString("type", "shapeless").toLowerCase(Locale.ROOT);
        var plugin = JavaPlugin.getPlugin(InfuseSmp.class);
        NamespacedKey key = new NamespacedKey(plugin, getId());
        Bukkit.removeRecipe(key);

        switch (recipeOption) {
            case "shaped":
                var shaped = new ShapedRecipe(key, result);

                var choices = this.getChoices(section, "items");
                var shape = this.createRecipeStrings(choices);
                shaped.shape(shape);

                for (int i = 0; i < choices.length; i++) {
                    var choice = choices[i];
                    if (choice == null) continue;

                    shaped.setIngredient((i + "").charAt(0), choice);
                }

                this.recipe = shaped;
                break;

            case "shapeless":
                ShapelessRecipe shapeless = new ShapelessRecipe(key, result);

                for (String element : section.getStringList("items")) {
                    String materialName = element.toUpperCase(Locale.ROOT);

                    try {
                        shapeless.addIngredient(Material.valueOf(materialName));
                    } catch (IllegalArgumentException ignored) {
                        plugin.getLogger().log(Level.SEVERE, String.format("Recipe material doesn't exist! (got %s)", materialName));
                    }
                }

                this.recipe = shapeless;
                break;

            default:
                plugin.getLogger().log(Level.SEVERE, String.format("Unknown recipe option: \"%s\". Must be either \"shaped\" or \"shapeless\"", recipeOption));
                break;
        }

        Bukkit.addRecipe(this.recipe);
    }

    private String[] createRecipeStrings(RecipeChoice.MaterialChoice[] choices) {
        var str = new String[choices.length / 3];

        for(int i = 0; i < choices.length; i++) {
            var choice = choices[i];
            var idx = (int) Math.floor(i / 3D);

            if (str[idx] == null) {
                str[idx] = "";
            }

            if (choice == null) {
                str[idx] += " ";
            } else {
                str[idx] += i + "";
            }
        }

        return str;
    }


    private RecipeChoice.MaterialChoice[] getChoices(ConfigurationSection configurationSection, String path) {
        var choices = new RecipeChoice.MaterialChoice[9];

        if(!configurationSection.isConfigurationSection(path)) {
            return choices;
        }

        var sub = configurationSection.getConfigurationSection(path);
        if (sub == null) return choices;

        var plugin = JavaPlugin.getPlugin(InfuseSmp.class);
        for (var key : sub.getKeys(false)) {
            var matName = sub.getString(key);
            if(matName == null) {
                continue;
            }

            var mat = Material.AIR;
            try {
                mat = Material.valueOf(matName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                plugin.getLogger().log(Level.SEVERE, String.format("Recipe material for slot %s doesn't exist! (got %s)", key, matName));
            }

            if (mat.isAir()) {
                continue;
            }

            try {
                int index = Integer.parseInt(key);
                var idx = Math.max(0, Math.min(index - 1, choices.length - 1));
                choices[idx] = new RecipeChoice.MaterialChoice(mat);
            } catch (NumberFormatException ignored) {
                plugin.getLogger().log(Level.SEVERE, String.format("Recipe slot out of bounds (Must be 1-9): %s", key));
            }
        }

        return choices;
    }

}
