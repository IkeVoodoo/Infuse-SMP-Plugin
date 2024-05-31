package me.ikevoodoo.infusesmp.items;

import me.ikevoodoo.infusesmp.config.MessageHandler;
import me.ikevoodoo.infusesmp.config.items.ItemConfig;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import me.ikevoodoo.spigotcore.items.Item;
import me.ikevoodoo.spigotcore.items.context.ItemClickContext;
import me.ikevoodoo.spigotcore.items.context.ItemSetupContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class GoodPotion extends Item {

    private final ItemConfig config;
    private final EffectManager effectManager;
    private final MessageHandler messageHandler;

    public GoodPotion(ItemConfig config, EffectManager effectManager, MessageHandler messageHandler) {
        this.config = config;
        this.effectManager = effectManager;
        this.messageHandler = messageHandler;
    }

    @Override
    public void onRightClick(ItemClickContext context) {
        context.cancelInteraction();

        if (!(context.clicker() instanceof Player player)) return;

        var type = this.effectManager.removeNegativeEffectNow(player);
        var config = this.messageHandler.getConfig(player);

        if (type == null) {
            player.sendMessage(config.getGoodPotionFailure());
            return;
        }

        player.sendMessage(config.getGoodPotionSuccess(PotionType.fromBukkit(type.getType()), true));
        context.tryConsumeItem();
    }

    @Override
    protected Material getMaterial() {
        return this.config.getType();
    }

    @Override
    protected void setupItemStack(ItemSetupContext context) {

    }

    @Override
    protected boolean hasState() {
        return false;
    }
}
