package me.ikevoodoo.infusesmp.items;

import me.ikevoodoo.infusesmp.config.items.ItemConfig;
import me.ikevoodoo.infusesmp.menu.SparkMenu;
import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.context.ItemClickContext;
import me.ikevoodoo.spigotcore.items.context.ItemSetupContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SparkItem extends Item {
    private final ItemConfig config;
    private final SparkMenu sparkMenu;

    public SparkItem(ItemConfig config, SparkMenu sparkMenu) {
        this.config = config;
        this.sparkMenu = sparkMenu;
    }

    @Override
    public void onRightClick(ItemClickContext context) {
        this.sparkMenu.open((Player) context.clicker());
        context.tryConsumeItem();
    }

    @Override
    protected Material getMaterial() {
        return this.config.getType();
    }

    @Override
    protected void setupItemStack(ItemSetupContext setupContext) {

    }

    @Override
    protected boolean hasState() {
        return false;
    }
}
