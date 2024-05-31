package me.ikevoodoo.infusesmp.menu;

import me.ikevoodoo.infusesmp.config.MessageConfig;
import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import me.ikevoodoo.spigotcore.gui.Screen;
import me.ikevoodoo.spigotcore.gui.pages.PagePosition;
import me.ikevoodoo.spigotcore.gui.pages.PageType;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPage;
import me.ikevoodoo.spigotcore.gui.pages.ScreenPageView;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class SparkMenu {

    private final int itemsPerPage = 8;

    private final Screen screen;
    private final ScreenPage page;
    private final NamespacedKey pageId;
    private final EffectManager effectManager;
    private final MessageHandler messageHandler;

    public SparkMenu(Plugin plugin, EffectManager effectManager, MessageHandler messageHandler) {
        this.screen = new Screen(plugin, "Upgrade an effect.");
        this.pageId = new NamespacedKey(plugin, "infuse_smp_spark_page");
        this.effectManager = effectManager;
        this.messageHandler = messageHandler;

        this.page = this.screen.createPage(new PageType(InventoryType.DROPPER, 9, 3, 3));
        this.page.getPageButtonHolder().addButton(new PagePosition(1, 1), (event, clicked, type) -> {
            var player = (Player) event.player();

            int max = this.getMaxPages(player);
            int page = this.getPage(player);
            switch (type) {
                case LEFT:
                    page--;
                    break;
                case RIGHT:
                    page++;
                    break;
            }

            page = Math.max(0, Math.min(page, max - 1));
            this.setPage(player, page);

            this.updateView(player, event.view(), page);
            event.setCancelled(true);
        });
        this.page.getPageButtonHolder().addClickHandler((event, clicked, type) -> {
            event.setCancelled(true);
            if (clicked.getType() != Material.POTION) return;

            PotionMeta meta = (PotionMeta) clicked.getItemMeta();
            assert meta != null;

            PotionEffectType potionEffectType = meta.getCustomEffects().get(0).getType();

            var player = (Player) event.player();
            var potionType = PotionType.fromBukkit(potionEffectType);


            event.setCancelled(true);
            player.closeInventory();
            var level = String.valueOf(this.effectManager.amplifyEffectNow(player, PotionType.fromBukkit(potionEffectType), 1).getAmplifier() + 1);
            player.sendMessage(this.messageHandler.getConfig(player).getSparkUpgradeMessage(potionType, true).replace("{{level}}", level));
        });
    }

    public void open(Player player) {
        ScreenPageView view = this.screen.open(player);

        this.setPage(player, 0);
        this.updateView(player, view, 0);
    }

    private void updateView(Player player, ScreenPageView view, int page) {
        view.setItem(new PagePosition(1, 1), this.createNavigationItemStack(this.messageHandler.getConfig(player), player, page));

        this.displayItems(player, this.page, view, page);
    }

    private void displayItems(Player player, ScreenPage page, ScreenPageView view, int currentPage) {
        int skip = currentPage * this.itemsPerPage;
        var positive = this.effectManager.getPositive(player);
        var config = this.messageHandler.getConfig(player);

        int i = 0;
        for (Map.Entry<PotionType, PotionEffect> entry : positive.entrySet()) {
            int index = i - skip;
            if (index >= 4) {
                index++;
            }

            PagePosition pos = page.slotPosition(index);

            if (entry.getValue().getAmplifier() > 0) {
                i++;

                ItemStack stack = new ItemStack(Material.BARRIER);
                ItemMeta meta = stack.getItemMeta();
                assert meta != null;
                meta.setDisplayName(config.getSparkAlreadyUpgraded(entry.getKey(), false));
                stack.setItemMeta(meta);

                view.setItem(pos, stack);
                continue;
            }

            if (i < skip) {
                i++;
                continue;
            }

            if (i >= skip + this.itemsPerPage) {
                break;
            }

            view.setItem(pos, this.createPotionStack(config, entry.getKey(), entry.getValue()));

            i++;
        }

        int maxIndex = i - skip;
        if (maxIndex >= 4) {
            maxIndex++;
        }

        for (int j = 0; j < this.itemsPerPage; j++) {
            int index = j;
            if (index >= 4) index++;

            if (index < maxIndex) continue;

            PagePosition pos = page.slotPosition(index);

            ItemStack curr = view.getItem(pos);
            if (curr != null && curr.getType() == Material.BARRIER) continue;

            ItemStack stack = new ItemStack(Material.BARRIER);
            ItemMeta meta = stack.getItemMeta();
            assert meta != null;
            meta.setDisplayName(config.getSparkNoEffect());
            stack.setItemMeta(meta);

            view.setItem(pos, stack);
        }
    }

    private ItemStack createPotionStack(MessageConfig config, PotionType type, PotionEffect effect) {
        ItemStack stack = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) stack.getItemMeta();
        assert meta != null;

        meta.addCustomEffect(effect, true);
        meta.setDisplayName(config.getSparkPotionName(type, true));

        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createNavigationItemStack(MessageConfig config, HumanEntity player, int currentPage) {
        var stack = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
        var meta = stack.getItemMeta();
        assert meta != null;

        int maxPages = this.getMaxPages(player);
        meta.setDisplayName(config.getSparkNavigationFormat(currentPage + 1, maxPages));
        meta.addItemFlags(ItemFlag.values());

        List<String> lore = new ArrayList<>();
        if (currentPage > 0) {
            lore.add(config.getSparkMoveLeftFormat());
        }

        if (currentPage < maxPages - 1) {
            lore.add(config.getSparkMoveRightFormat());
        }

        meta.setLore(lore);

        stack.setItemMeta(meta);
        return stack;
    }

    private int getMaxPages(HumanEntity player) {
        int effects = this.effectManager.getPositive(player).size();
        float result = effects / (float) this.itemsPerPage;
        if (result < 1) {
            return 1;
        }
        return (int) Math.ceil(result);
    }

    private int getPage(HumanEntity entity) {
        return entity.getPersistentDataContainer().getOrDefault(this.pageId, PersistentDataType.INTEGER, 0);
    }

    private void setPage(HumanEntity entity, int page) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(this.pageId, PersistentDataType.INTEGER, page);
    }

}
