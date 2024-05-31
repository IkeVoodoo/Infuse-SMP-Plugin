package me.ikevoodoo.infusesmp.listeners;

import me.ikevoodoo.infusesmp.config.GeneralConfig;
import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.util.Arrays;
import java.util.ListIterator;

public class PlayerPotionListener implements Listener {

    private final GeneralConfig config;
    private final EffectManager effectManager;
    private final MessageHandler messageHandler;
    private final NamespacedKey potionKey;

    public PlayerPotionListener(GeneralConfig config, EffectManager effectManager, MessageHandler messageHandler, NamespacedKey potionKey) {
        this.config = config;
        this.effectManager = effectManager;
        this.messageHandler = messageHandler;
        this.potionKey = potionKey;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void on(BrewEvent event) {
        for (ListIterator<ItemStack> iterator = event.getResults().listIterator(); iterator.hasNext(); ) {
            ItemStack result = iterator.next();
            if (result.getType() != Material.POTION) continue;

            PotionMeta meta = (PotionMeta) result.getItemMeta();
            assert meta != null;
            for (PotionEffect effect : meta.getCustomEffects()) {
                if (this.config.getEffectConfig().knowsAboutEffect(PotionType.fromBukkit(effect.getType()))) {
                    if (this.config.shouldUseSass()) {
                        iterator.set(this.createBush());
                    } else {
                        event.setCancelled(true);
                    }
                    break;
                }
            }

            if (meta.getCustomEffects().isEmpty()) {
                org.bukkit.potion.PotionType type = meta.getBasePotionData().getType();
                if (this.config.getEffectConfig().knowsAboutEffect(PotionType.fromBukkit(type.getEffectType()))) {
                    if (this.config.shouldUseSass()) {
                        iterator.set(this.createBush());
                    } else {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        var stack = event.getItem();
        if (stack.getType() != Material.POTION) return;

        var player = event.getPlayer();

        var conf = this.messageHandler.getConfig(player);

        var meta = (PotionMeta) stack.getItemMeta();
        assert meta != null;
        if (meta.getPersistentDataContainer().has(this.potionKey, PersistentDataType.BYTE)) {
            stack.setAmount(stack.getAmount() - 1);
            event.setItem(stack);

            for (var effect : meta.getCustomEffects()) {
                var removed = this.effectManager.removeNegativeEffect(player);

                if (removed == null) {
                    var type = PotionType.fromBukkit(effect.getType());
                    this.effectManager.giveEffect(player, type);
                    player.sendMessage(conf.getReceiveMessage(type, false));
                } else {
                    player.sendMessage(conf.getLoseMessage(PotionType.fromBukkit(removed.getType()), true));
                }
            }

            this.effectManager.apply(player);

            player.updateInventory();
            return;
        }

        var effectList = meta.getCustomEffects();
        if (effectList.size() == 1 && effectList.get(0).getDuration() == -1) {
            player.sendMessage("§cYou tried to glitch out the plugin, that's not a good thing to do!");
            player.setHealth(0.1);
            event.setCancelled(true);
        }
    }

    private ItemStack createBush() {
        ItemStack stack = new ItemStack(Material.DEAD_BUSH);
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§cYou can't craft this!");
        meta.setLore(Arrays.asList("§7Womp womp, you can't craft infuse effects!", "§7Sorry, not sorry!"));
        stack.setItemMeta(meta);
        return stack;
    }
}
