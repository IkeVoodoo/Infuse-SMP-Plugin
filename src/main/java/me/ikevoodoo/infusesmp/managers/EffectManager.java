package me.ikevoodoo.infusesmp.managers;

import me.ikevoodoo.infusesmp.config.GeneralConfig;
import me.ikevoodoo.infusesmp.config.effects.EffectConfig;
import me.ikevoodoo.infusesmp.config.effects.EffectData;
import me.ikevoodoo.infusesmp.effects.EffectResult;
import me.ikevoodoo.infusesmp.effects.Effects;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.utils.CollectionUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EffectManager {

    private final HashMap<UUID, Effects> effectsList = new HashMap<>();
    private final GeneralConfig generalConfig;
    private final NamespacedKey effectKey;

    public EffectManager(GeneralConfig generalConfig, NamespacedKey effectKey) {
        this.generalConfig = generalConfig;
        this.effectKey = effectKey;
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public boolean hasNegativeEffect(LivingEntity entity) {
        return this.createEffects(entity).hasNegative();
    }

    public boolean hasPositiveEffect(LivingEntity entity) {
        return this.createEffects(entity).hasPositive();
    }

    public void giveEffect(LivingEntity entity, PotionType type) {
        EffectConfig cfg = this.generalConfig.getEffectConfig();
        var effectData = cfg.getPositive().get(type);
        if (effectData == null) {
            effectData = cfg.getNegative().get(type);
        }

        if (effectData == null) return;

        createEffects(entity).add(effectData, this.generalConfig.getEffectConfig().getPositive().containsKey(type));
    }

    public void giveEffectNow(LivingEntity entity, PotionType type) {
        this.giveEffect(entity, type);
        this.apply(entity);
    }

    public PotionEffect removeEffect(LivingEntity entity, PotionType type) {
        var effects = this.createEffects(entity);
        return effects.remove(type);
    }

    public PotionEffect removeEffectNow(LivingEntity entity, PotionType type) {
        var res = this.removeEffect(entity, type);
        if (res == null) return null;
        this.apply(entity);

        return res;
    }

    public void load(LivingEntity entity) {
        this.createEffects(entity).load(entity);
    }

    public void save(LivingEntity entity) {
        this.createEffects(entity).save(entity);
    }

    public boolean hasEffect(LivingEntity entity, PotionType type) {
        return this.createEffects(entity).hasEffect(type);
    }

    public EffectResult addPositiveEffect(LivingEntity entity) {
        var effects = this.createEffects(entity);
        if (effects.hasNegative()) {
            var effect = effects.removeFirstNegative();
            return new EffectResult(PotionType.fromBukkit(effect.getType()), effect, false);
        }

        var effectData = randomEffectData(this.generalConfig.getEffectConfig().getPositive(), effects);
        if (effectData == null || effects.hasEffect(effectData.getType())) {
            return new EffectResult(null, null, false);
        }

        effects.add(effectData, true);
        return new EffectResult(effectData.getType(), effectData.toEffect(), true);
    }

    public EffectResult addNegativeEffect(LivingEntity entity) {
        var effects = this.createEffects(entity);
        if (effects.hasPositive()) {
            var effect = effects.removeFirstPositive();
            return new EffectResult(PotionType.fromBukkit(effect.getType()), effect, false);
        }

        var effectData = randomEffectData(this.generalConfig.getEffectConfig().getNegative(), effects);
        if (effectData == null || effects.hasEffect(effectData.getType())) {
            return new EffectResult(null, null, false);
        }

        effects.add(effectData, false);
        return new EffectResult(effectData.getType(), effectData.toEffect(), true);
    }

    public EffectResult addPositiveEffectNow(LivingEntity entity) {
        var result = this.addPositiveEffect(entity);
        if (result == null) return null;

        this.apply(entity);
        return result;
    }

    public EffectResult addNegativeEffectNow(LivingEntity entity) {
        var result = this.addNegativeEffect(entity);
        if (result == null) return null;

        this.apply(entity);
        return result;
    }

    public PotionEffect amplifyEffect(LivingEntity entity, PotionType type, int amount) {
        return this.createEffects(entity).amplify(type, amount, (int) (this.generalConfig.getEffectBoostSeconds() * 20L));
    }

    public PotionEffect deAmplify(LivingEntity entity, PotionType type) {
        return this.createEffects(entity).amplify(type, -1, -1);
    }

    public PotionEffect amplifyEffectNow(LivingEntity entity, PotionType type, int amount) {
        PotionEffect amplified = this.amplifyEffect(entity, type, amount);
        entity.removePotionEffect(amplified.getType());
        this.createEffects(entity).applyAndSave(entity);
        return amplified;
    }

    public PotionEffect deAmplifyNow(LivingEntity entity, PotionType type) {
        PotionEffect amplified = this.deAmplify(entity, type);
        entity.removePotionEffect(amplified.getType());
        this.createEffects(entity).applyAndSave(entity);
        return amplified;
    }

    public Map<PotionType, PotionEffect> getPositive(LivingEntity entity) {
        return this.createEffects(entity).getPositiveEffects();
    }

    public Map<PotionType, PotionEffect> getNegative(LivingEntity entity) {
        return this.createEffects(entity).getNegativeEffects();
    }

    public PotionEffect removePositiveEffect(LivingEntity entity) {
        return this.createEffects(entity).removeFirstPositive();
    }

    public PotionEffect removeNegativeEffect(LivingEntity entity) {
        return this.createEffects(entity).removeFirstNegative();
    }

    public PotionEffect removePositiveEffectNow(LivingEntity entity) {
        var effect = this.removePositiveEffect(entity);
        if (effect == null) return null;

        entity.removePotionEffect(effect.getType());
        return effect;
    }

    public PotionEffect removeNegativeEffectNow(LivingEntity entity) {
        var effect = this.removeNegativeEffect(entity);
        if (effect == null) return null;

        entity.removePotionEffect(effect.getType());
        return effect;
    }

    public EffectData randomPositiveEffect(LivingEntity entity) {
        return randomEffectData(this.generalConfig.getEffectConfig().getPositive(), this.createEffects(entity));
    }

    public EffectData randomNegativeEffect(LivingEntity entity) {
        return randomEffectData(this.generalConfig.getEffectConfig().getNegative(), this.createEffects(entity));
    }

    public void apply(LivingEntity entity) {
        this.createEffects(entity).applyAndSave(entity);
    }

    private EffectData randomEffectData(Map<PotionType, EffectData> dataList, Effects effects) {
        EffectData effectData;

        int tries = -dataList.size();

        do {
            tries++;
            Map.Entry<PotionType, EffectData> entry = CollectionUtils.randomInMap(dataList);

            if (entry == null) return null;

            effectData = entry.getValue();
            if (effectData == null) return null;
        } while (tries < dataList.size() && effects.hasEffect(effectData.getType()));

        if (tries >= dataList.size()) {
            return null;
        }

        return effectData;
    }

    private Effects createEffects(LivingEntity entity) {
        return this.effectsList.computeIfAbsent(entity.getUniqueId(), id -> new Effects(this.generalConfig, this.effectKey));
    }

}
