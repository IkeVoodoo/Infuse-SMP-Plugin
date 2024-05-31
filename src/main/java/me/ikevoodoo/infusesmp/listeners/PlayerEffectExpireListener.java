package me.ikevoodoo.infusesmp.listeners;

import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.potion.PotionEffect;

public class PlayerEffectExpireListener implements Listener {

    private final EffectManager effectManager;
    private final MessageHandler messageHandler;

    public PlayerEffectExpireListener(EffectManager effectManager, MessageHandler messageHandler) {
        this.effectManager = effectManager;
        this.messageHandler = messageHandler;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPotionEffect(EntityPotionEffectEvent event) {
        if (this.tryExpiry(event)) return;

        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED && event.getCause() != EntityPotionEffectEvent.Cause.PLUGIN) {
            if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

            var effect = event.getOldEffect();
            if (effect == null) return;

            if (!this.effectManager.hasEffect(livingEntity, PotionType.fromBukkit(effect.getType()))) {
                return;
            }

            event.setCancelled(true);
        }
    }

    private boolean tryExpiry(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return false;

        if(event.getCause() != EntityPotionEffectEvent.Cause.EXPIRATION) return false;

        var old = event.getOldEffect();
        if (old == null || old.getAmplifier() - 1 < 0) return false;

        var potType = PotionType.fromBukkit(old.getType());
        if (!this.effectManager.getGeneralConfig().getEffectConfig().knowsAboutEffect(potType)) {
            return true;
        }

        event.setCancelled(true);

        if (!this.effectManager.hasEffect(livingEntity, potType)) {
            return true;
        }

        var type = PotionType.fromBukkit(old.getType());
        var level = String.valueOf(this.effectManager.deAmplifyNow(livingEntity, type).getAmplifier() + 1);

        if (event.getEntity() instanceof Player player) {
            var config = this.messageHandler.getConfig(player);
            player.sendMessage(
                    config.getAmplifyEnded(type, this.effectManager.getGeneralConfig().getEffectConfig().isNegative(type)).replace("{{level}}", level)
            );
        }
        return true;
    }
}
