package me.ikevoodoo.infusesmp;

import me.ikevoodoo.infusesmp.commands.DrainCommand;
import me.ikevoodoo.infusesmp.commands.EditEffectCommand;
import me.ikevoodoo.infusesmp.commands.ReloadCommand;
import me.ikevoodoo.infusesmp.config.GeneralConfig;
import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.items.GoodPotion;
import me.ikevoodoo.infusesmp.items.SparkItem;
import me.ikevoodoo.infusesmp.listeners.PlayerConnectionListener;
import me.ikevoodoo.infusesmp.listeners.PlayerDeathListener;
import me.ikevoodoo.infusesmp.listeners.PlayerEffectExpireListener;
import me.ikevoodoo.infusesmp.listeners.PlayerInventoryListener;
import me.ikevoodoo.infusesmp.listeners.PlayerPotionListener;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import me.ikevoodoo.infusesmp.menu.SparkMenu;
import me.ikevoodoo.spigotcore.items.ItemRegistry;
import me.ikevoodoo.spigotcore.items.listeners.ItemListeners;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.File;

public final class InfuseSmp extends JavaPlugin {

    private final GeneralConfig generalConfig;
    private final MessageHandler messageHandler;
    private final EffectManager effectManager;
    private final NamespacedKey potionKey = new NamespacedKey(this, "drained_potion");
    private SparkMenu sparkMenu;

    public InfuseSmp() {
        super();

        this.generalConfig = new GeneralConfig();
        this.messageHandler = new MessageHandler();
        this.effectManager = new EffectManager(this.generalConfig, new NamespacedKey(this, "infuse_smp_effects"));
    }

    @Override
    public void onEnable() {
        this.sparkMenu = new SparkMenu(this, this.effectManager, this.messageHandler);
        ItemListeners.registerAllListeners(this);

        ItemRegistry.register("spark_item", SparkItem.class, () -> new SparkItem(this.generalConfig.getSparkConfig(), this.sparkMenu));
        ItemRegistry.register("good_potion", GoodPotion.class, () -> new GoodPotion(this.generalConfig.getGoodPotionConfig(), this.effectManager, this.messageHandler));

        this.reloadConfig();

        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerPotionListener(this.generalConfig, this.effectManager, this.messageHandler, this.potionKey), this);
        getServer().getPluginManager().registerEvents(new PlayerEffectExpireListener(this.effectManager, this.messageHandler), this);
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this.effectManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this.messageHandler, this.potionKey), this);

        // Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> Bukkit.getOnlinePlayers().forEach(this.effectManager::apply), 0,5);

        getCommand("reload-infuse").setExecutor(new ReloadCommand(this::reloadConfig));
        getCommand("drain").setExecutor(new DrainCommand(this.effectManager, this.messageHandler, this.generalConfig.getDrainCommandConfig(), this.potionKey));
        getCommand("edit-effects").setExecutor(new EditEffectCommand(this.generalConfig, this.effectManager));
    }

    @Override
    public void reloadConfig() {
        saveDefaultConfig();

        super.reloadConfig();

        this.generalConfig.loadFrom(getConfig());
        this.messageHandler.reload(new File(getDataFolder(), "messages"));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public EffectManager getEffectManager() {
        return this.effectManager;
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
