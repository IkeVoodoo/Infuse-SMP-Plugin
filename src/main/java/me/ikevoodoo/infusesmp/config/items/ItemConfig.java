package me.ikevoodoo.infusesmp.config.items;

import me.ikevoodoo.infusesmp.config.Config;
import me.ikevoodoo.infusesmp.utils.StringUtils;
import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.ItemRegistry;
import me.ikevoodoo.spigotcore.items.data.DisplayItemData;
import me.ikevoodoo.spigotcore.items.data.ItemData;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class ItemConfig extends Config {

    private final List<String> lore = new ArrayList<>();
    private final List<String> loreView = Collections.unmodifiableList(this.lore);
    private final ItemRecipeConfig recipeConfig;
    private final String id;
    private Item item;
    private Material type;
    private String name;
    private int customModelData;
    private boolean enabled;

    public ItemConfig(String id) {
        this.id = id;
        this.recipeConfig = new ItemRecipeConfig(id);
    }

    public ItemRecipeConfig getRecipeConfig() {
        return recipeConfig;
    }

    public List<String> getLore() {
        return this.loreView;
    }

    public Material getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public ItemStack getStack() {
        return this.item.createItemStack(
                new ItemData.Builder()
                        .displayData(
                                new DisplayItemData.Builder()
                                        .displayName(this.name)
                                        .customModelData(this.customModelData)
                                        .lore(this.loreView)
                                        .build()
                        )
                        .build()
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void loadFrom(ConfigurationSection section) {
        this.lore.clear();
        this.lore.addAll(section.getStringList("lore").stream().map(StringUtils::color).toList());

        this.type = Material.valueOf(section.getString("type", "stone").toUpperCase(Locale.ROOT));
        this.name = StringUtils.color(section.getString("name", "§rCustom Infuse Item"));
        this.customModelData = section.getInt("customModelData", 0);
        this.enabled = section.getBoolean("itemEnabled", true);

        this.item = ItemRegistry.getInstance(this.id);
        this.recipeConfig.loadFrom(getSection(section, "recipe"), this.getStack());
    }

}
