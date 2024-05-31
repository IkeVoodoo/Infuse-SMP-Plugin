package me.ikevoodoo.infusesmp.commands;

import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.config.commands.DrainCommandConfig;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class DrainCommand implements TabExecutor {

    private final EffectManager effectManager;
    private final MessageHandler messageHandler;
    private final DrainCommandConfig config;
    private final NamespacedKey potionKey;

    public DrainCommand(EffectManager effectManager, MessageHandler messageHandler, DrainCommandConfig config, NamespacedKey potionKey) {
        this.effectManager = effectManager;
        this.messageHandler = messageHandler;
        this.config = config;
        this.potionKey = potionKey;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be executed by a player");
            return true;
        }


        var config = this.messageHandler.getConfig(player);

        PotionEffect effect;
        if (args.length > 0 && this.config.shouldAllowPickingEffects()) {
            try {
                var potionType = PotionType.valueOf(args[0].toUpperCase(Locale.ROOT));

                effect = this.effectManager.removeEffectNow(player, potionType);
                if (effect == null) {
                    sender.sendMessage("§cYou do not have the effect of " + potionType.getDisplayName());
                    return true;
                }
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage("§cUnknown potion type: " + args[0]);
                return true;
            }
        } else {
            effect = this.effectManager.removePositiveEffectNow(player);
        }

        if (effect == null) {
            if (!this.config.shouldDrainIntoNegatives()) {
                sender.sendMessage(config.getDrainNoDebts());
                return true;
            }

            var addedNegative = this.effectManager.addNegativeEffectNow(player);
            if (addedNegative.effect() == null) {
                sender.sendMessage(config.getDrainTooManyNegatives());
                return true;
            }

            var data = this.effectManager.randomPositiveEffect(player);

            effect = data.toEffect();
            sender.sendMessage(config.getDrainNegativeGiven(addedNegative.effect(), true));
        }


        var drainedType = PotionType.fromBukkit(effect.getType());
        var item = new ItemStack(Material.POTION);
        var meta = (PotionMeta) item.getItemMeta();
        assert meta != null;
        meta.addCustomEffect(effect, true);
        meta.setDisplayName(config.getDrainedPotionName(drainedType, false));
        meta.getPersistentDataContainer().set(this.potionKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);

        var world = player.getWorld();
        var loc = player.getLocation();
        for(var remaining : player.getInventory().addItem(item).values()) {
            world.dropItemNaturally(loc, remaining);
        }

        sender.sendMessage(config.getDrainSuccess(drainedType, false));
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (!this.config.shouldAllowPickingEffects()) return null;

        return this.effectManager.getPositive(player).keySet().stream()
                .map(PotionType::name)
                .map(String::toLowerCase)
                .toList();
    }
}