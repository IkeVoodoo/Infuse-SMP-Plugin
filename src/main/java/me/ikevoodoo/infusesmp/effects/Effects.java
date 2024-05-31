package me.ikevoodoo.infusesmp.effects;

import me.ikevoodoo.infusesmp.config.GeneralConfig;
import me.ikevoodoo.infusesmp.config.effects.EffectData;
import me.ikevoodoo.infusesmp.utils.StreamUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Effects {

    private final Map<PotionType, PotionEffect> positiveEffects = new HashMap<>();
    private final Map<PotionType, PotionEffect> negativeEffects = new HashMap<>();

    private final Map<PotionType, PotionEffect> positiveEffectsView = Collections.unmodifiableMap(this.positiveEffects);
    private final Map<PotionType, PotionEffect> negativeEffectsView = Collections.unmodifiableMap(this.negativeEffects);

    private final GeneralConfig generalConfig;
    private final NamespacedKey effectKey;

    public Effects(GeneralConfig generalConfig, NamespacedKey effectKey) {
        this.generalConfig = generalConfig;
        this.effectKey = effectKey;
    }

    public boolean hasNegative() {
        return !this.negativeEffects.isEmpty();
    }

    public boolean hasPositive() {
        return !this.positiveEffects.isEmpty();
    }

    public Map<PotionType, PotionEffect> getNegativeEffects() {
        return this.negativeEffectsView;
    }

    public Map<PotionType, PotionEffect> getPositiveEffects() {
        return this.positiveEffectsView;
    }

    public PotionEffect removeFirstNegative() {
        var iterator = this.negativeEffects.entrySet().iterator();

        if (iterator.hasNext()) {
            var next = iterator.next();
            iterator.remove();

            return next.getValue();
        }

        return null;
    }

    public PotionEffect removeFirstPositive() {
        var iterator = this.positiveEffects.entrySet().iterator();

        if (iterator.hasNext()) {
            var next = iterator.next();
            iterator.remove();

            return next.getValue();
        }

        return null;
    }

    public PotionEffect amplify(PotionType type, int amount, int duration) {
        boolean positive = this.isPositive(type);
        var effect = this.removeEffect(type);
        if (effect == null) return null;

        var amplified = this.amplifyEffect(effect, amount, duration);

        if (positive) {
            this.positiveEffects.put(type, amplified);
        } else {
            this.negativeEffects.put(type, amplified);
        }

        return amplified;
    }

    public void add(EffectData data, boolean positive) {
        PotionEffect effect = data.toEffect();

        if (positive) {
            this.positiveEffects.put(data.getType(), effect);
            return;
        }

        this.negativeEffects.put(data.getType(), effect);
    }

    public PotionEffect remove(PotionType type) {
        return this.removeEffect(type);
    }

    public void applyAndSave(LivingEntity entity) {
        this.applyInternal(entity);
        this.save(entity);
    }

    public void save(LivingEntity entity) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            this.save(entity, baos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        entity.getPersistentDataContainer().set(this.effectKey, PersistentDataType.BYTE_ARRAY, baos.toByteArray());
    }

    public void load(LivingEntity livingEntity) {
        for (var activePotionEffect : livingEntity.getActivePotionEffects()) {
            livingEntity.removePotionEffect(activePotionEffect.getType());
        }

        var persistentDataContainer = livingEntity.getPersistentDataContainer();
        if (!persistentDataContainer.has(this.effectKey, PersistentDataType.BYTE_ARRAY)) return;

        byte[] array = persistentDataContainer.get(this.effectKey, PersistentDataType.BYTE_ARRAY);
        assert array != null;
        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        try {
            this.load(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.applyInternal(livingEntity);
    }

    public boolean hasEffect(PotionType type) {
        return this.getEffect(type) != null;
    }

    private PotionEffect getEffect(PotionType type) {
        PotionEffect effect = this.positiveEffects.get(type);
        if (effect == null) {
            return this.negativeEffects.get(type);
        }

        return effect;
    }

    private PotionEffect removeEffect(PotionType type) {
        PotionEffect effect = this.positiveEffects.remove(type);
        if (effect == null) {
            return this.negativeEffects.remove(type);
        }

        return effect;
    }

    private boolean isPositive(PotionType type) {
        return this.positiveEffects.containsKey(type);
    }

    private void applyInternal(LivingEntity entity) {
        for (var effect : entity.getActivePotionEffects()) {
            var type = PotionType.fromBukkit(effect.getType());
            if (this.generalConfig.getEffectConfig().knowsAboutEffect(type)) {
                if (!this.hasEffect(type)) {
                    entity.removePotionEffect(effect.getType());
                }
            }
        }

        this.addAllEffects(this.positiveEffects, entity);
        this.addAllEffects(this.negativeEffects, entity);
    }

    private void addAllEffects(Map<PotionType, PotionEffect> effects, LivingEntity entity) {
        for (var entry : effects.entrySet()) {
            var effect = entry.getValue();

            var mcEffect = entity.getPotionEffect(effect.getType());
            if (mcEffect != null) {
                if (mcEffect.getAmplifier() == effect.getAmplifier()) {
                    continue;
                }

                entity.removePotionEffect(effect.getType()); // In case of a mismatch, let's remove the old effect
            }

            entity.addPotionEffect(effect);
        }
    }

    private void save(LivingEntity entity, OutputStream outputStream) throws IOException {
        this.writeMap(entity, this.positiveEffects, outputStream);
        this.writeMap(entity, this.negativeEffects, outputStream);
    }

    private void load(InputStream inputStream) throws IOException {
        this.readMap(this.positiveEffects, inputStream);
        this.readMap(this.negativeEffects, inputStream);
    }

    private PotionEffect amplifyEffect(PotionEffect effect, int amount, int duration) {
        return new PotionEffect(
                effect.getType(),
                duration,
                effect.getAmplifier() + amount,
                effect.isAmbient(),
                effect.hasParticles(),
                effect.hasIcon()
        );
    }

    private void writeMap(LivingEntity entity, Map<PotionType, PotionEffect> effects, OutputStream outputStream) throws IOException {
        StreamUtils.writeInt(effects.size(), outputStream);
        for (Map.Entry<PotionType, PotionEffect> pot : effects.entrySet()) {
            byte[] bytes = pot.getKey().name().getBytes(StandardCharsets.UTF_8);
            StreamUtils.writeInt(bytes.length, outputStream);
            outputStream.write(bytes);

            PotionEffect effect = pot.getValue();

            PotionEffect entityEffect = entity.getPotionEffect(pot.getKey().getBukkitType());

            int duration = entityEffect == null ? effect.getDuration() : entityEffect.getDuration();
            int amplifier = entityEffect == null ? effect.getAmplifier() : entityEffect.getAmplifier();

            StreamUtils.writeInt(duration, outputStream);
            StreamUtils.writeInt(amplifier, outputStream);

            outputStream.write(effect.isAmbient() ? 1 : 0);
            outputStream.write(effect.hasParticles() ? 1 : 0);
            outputStream.write(effect.hasIcon() ? 1 : 0);
        }
    }

    private void readMap(Map<PotionType, PotionEffect> effects, InputStream inputStream) throws IOException {
        effects.clear();
        for (int i = 0, max = StreamUtils.readInt(inputStream); i < max; i++) {
            int size = StreamUtils.readInt(inputStream);
            byte[] bytes = new byte[size];
            if(inputStream.read(bytes) == -1) {
                throw new IllegalStateException("Unexpected end of stream while reading effects!");
            }

            String name = new String(bytes, StandardCharsets.UTF_8);
            PotionType type = PotionType.valueOf(name);

            int duration = StreamUtils.readInt(inputStream);
            int amplifier = StreamUtils.readInt(inputStream);

            boolean ambient = inputStream.read() == 1;
            boolean particles = inputStream.read() == 1;
            boolean icon = inputStream.read() == 1;

            PotionEffect effect = type.toEffect(duration, amplifier, ambient, particles, icon);
            effects.put(type, effect);
        }
    }
}
