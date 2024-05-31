package me.ikevoodoo.infusesmp.listeners;

import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final EffectManager manager;

    public PlayerConnectionListener(EffectManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.manager.load(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.manager.save(event.getPlayer());
    }
}
