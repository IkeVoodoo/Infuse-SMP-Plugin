package me.ikevoodoo.infusesmp.config.effects;

import me.ikevoodoo.infusesmp.effects.PotionType;
import org.bukkit.potion.PotionEffect;

public class EffectData {
    private final PotionType type;
    private final int duration;
    private final int amplifier;
    private final boolean ambient;
    private final boolean particles;
    private final boolean icon;

    public EffectData(PotionType type, int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.ambient = ambient;
        this.particles = particles;
        this.icon = icon;
    }

    public EffectData(PotionType type) {
        this(type, 0, 0, false, false, false);
    }

    public PotionType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public boolean isAmbient() {
        return ambient;
    }

    public boolean isParticles() {
        return particles;
    }

    public boolean isIcon() {
        return icon;
    }

    public PotionEffect toEffect() {
        return this.type.toEffect(this.duration, this.amplifier, this.ambient, this.particles, this.icon);
    }
}
