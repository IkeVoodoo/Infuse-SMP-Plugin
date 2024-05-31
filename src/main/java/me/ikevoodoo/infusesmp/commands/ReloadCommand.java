package me.ikevoodoo.infusesmp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final Runnable reloadRunnable;

    public ReloadCommand(Runnable reloadRunnable) {
        this.reloadRunnable = reloadRunnable;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long start = System.nanoTime();
        this.reloadRunnable.run();
        long end = System.nanoTime();

        sender.sendMessage(String.format("§6Reloaded the InfuseSMP plugin, took %.2fms!", (end - start) / 1_000_000D));

        return true;
    }
}
