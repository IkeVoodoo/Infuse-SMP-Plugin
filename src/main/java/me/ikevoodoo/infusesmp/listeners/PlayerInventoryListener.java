package me.ikevoodoo.infusesmp.listeners;

import me.ikevoodoo.infusesmp.config.MessageConfig;
import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.effects.PotionType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerInventoryListener implements Listener {

    private final MessageHandler messageHandler;
    private final NamespacedKey potionKey;

    public PlayerInventoryListener(MessageHandler messageHandler, NamespacedKey potionKey) {
        this.messageHandler = messageHandler;
        this.potionKey = potionKey;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        var stack = event.getItem().getItemStack();

        this.translatePotionStack(player, stack);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        var slot = event.getNewSlot();
        var player = event.getPlayer();
        var stack = player.getInventory().getItem(slot);
        if (stack == null) return;

        this.translatePotionStack(player, stack);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        var view = event.getView();
        if (view.getType() != InventoryType.CRAFTING) return;

        var conf = this.messageHandler.getConfig(player);

        try {
            var size = view.countSlots();
            for (int i = 0; i < size; i++) {
                var item = view.getItem(i);
                if (item == null) continue;

                this.translatePotionStack(conf, item);

                view.setItem(i, item);
            }
        } catch (IndexOutOfBoundsException ignored) {
            // Let's ignore that
        }
    }

    private void translatePotionStack(Player player, ItemStack stack) {
        this.translatePotionStack(this.messageHandler.getConfig(player), stack);
    }

    private void translatePotionStack(MessageConfig config, ItemStack stack) {
        if (!(stack.getItemMeta() instanceof PotionMeta potionMeta)) return;

        var pdc = potionMeta.getPersistentDataContainer();
        if (!pdc.has(this.potionKey, PersistentDataType.BYTE)) return;
;
        var mcType = potionMeta.getCustomEffects().get(0).getType();
        var type = PotionType.fromBukkit(mcType);

        potionMeta.setDisplayName(config.getDrainedPotionName(type, false));
        stack.setItemMeta(potionMeta);
    }
}
