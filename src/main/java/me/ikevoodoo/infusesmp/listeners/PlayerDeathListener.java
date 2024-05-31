package me.ikevoodoo.infusesmp.listeners;

import me.ikevoodoo.infusesmp.InfuseSmp;
import me.ikevoodoo.infusesmp.config.MessageConfig;
import me.ikevoodoo.infusesmp.effects.EffectResult;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class PlayerDeathListener implements Listener {

    private final InfuseSmp plugin;

    public PlayerDeathListener(InfuseSmp plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void on(PlayerDeathEvent event) {
        var killed = event.getEntity();
        var killer = killed.getKiller();
        if (killer == null) return;

        var effectManager = this.plugin.getEffectManager();
        var killedConfig = this.plugin.getMessageHandler().getConfig(killed);
        var killerConfig = this.plugin.getMessageHandler().getConfig(killer);

        var negative = effectManager.addNegativeEffectNow(killed);
        if (negative.effect() == null) {
            return;
        }

        this.sendMessage(killed, killedConfig, negative, false);

        var positive = effectManager.addPositiveEffectNow(killer);
        this.sendMessage(killer, killerConfig, positive, true);
    }

    private void sendMessage(Player player, MessageConfig config, EffectResult result, boolean positive) {
        if (result.effect() == null) return;

        if (result.added()) {
            player.sendMessage(config.getReceiveMessage(result.effect(), !positive));
            return;
        }

        player.sendMessage(config.getLoseMessage(result.effect(), positive));
    }


}
