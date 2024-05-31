package me.ikevoodoo.infusesmp.effects;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum PotionType {

    SPEED(PotionEffectMode.POSITIVE, PotionEffectType.SPEED),
    SLOWNESS(PotionEffectMode.NEGATIVE, PotionEffectType.SLOW),
    HASTE(PotionEffectMode.POSITIVE, PotionEffectType.FAST_DIGGING),
    MINING_FATIGUE(PotionEffectMode.NEGATIVE, PotionEffectType.SLOW_DIGGING),
    STRENGTH(PotionEffectMode.POSITIVE, PotionEffectType.INCREASE_DAMAGE),
    INSTANT_HEALTH(PotionEffectMode.POSITIVE, PotionEffectType.HEAL),
    INSTANT_DAMAGE(PotionEffectMode.NEGATIVE, PotionEffectType.HARM),
    JUMP_BOOST(PotionEffectMode.POSITIVE, PotionEffectType.JUMP),
    NAUSEA(PotionEffectMode.NEGATIVE, PotionEffectType.CONFUSION),
    REGENERATION(PotionEffectMode.POSITIVE, PotionEffectType.REGENERATION),
    RESISTANCE(PotionEffectMode.POSITIVE, PotionEffectType.DAMAGE_RESISTANCE),
    FIRE_RESISTANCE(PotionEffectMode.POSITIVE, PotionEffectType.FIRE_RESISTANCE),
    WATER_BREATHING(PotionEffectMode.POSITIVE, PotionEffectType.WATER_BREATHING),
    INVISIBILITY(PotionEffectMode.POSITIVE, PotionEffectType.INVISIBILITY),
    BLINDNESS(PotionEffectMode.NEGATIVE, PotionEffectType.BLINDNESS),
    NIGHT_VISION(PotionEffectMode.POSITIVE, PotionEffectType.NIGHT_VISION),
    HUNGER(PotionEffectMode.NEGATIVE, PotionEffectType.HUNGER),
    WEAKNESS(PotionEffectMode.NEGATIVE, PotionEffectType.WEAKNESS),
    HEALTH_BOOST(PotionEffectMode.POSITIVE, PotionEffectType.HEALTH_BOOST),
    ABSORPTION(PotionEffectMode.POSITIVE, PotionEffectType.ABSORPTION),
    SATURATION(PotionEffectMode.POSITIVE, PotionEffectType.SATURATION),
    GLOWING(PotionEffectMode.NEGATIVE, PotionEffectType.GLOWING),
    LEVITATION(PotionEffectMode.NEGATIVE, PotionEffectType.LEVITATION),
    LUCK(PotionEffectMode.POSITIVE, PotionEffectType.LUCK),
    BAD_LUCK(PotionEffectMode.NEGATIVE, PotionEffectType.UNLUCK),
    SLOW_FALLING(PotionEffectMode.POSITIVE, PotionEffectType.SLOW_FALLING),
    CONDUIT_POWER(PotionEffectMode.POSITIVE, PotionEffectType.CONDUIT_POWER),
    DOLPHINS_GRACE(PotionEffectMode.POSITIVE, PotionEffectType.DOLPHINS_GRACE) {
        @Override
        public String getDisplayName() {
            return "Dolphin's Grace";
        }
    },
    BAD_OMEN(PotionEffectMode.NEGATIVE, PotionEffectType.BAD_OMEN),
    HERO_OF_THE_VILLAGE(PotionEffectMode.POSITIVE, PotionEffectType.HERO_OF_THE_VILLAGE);

    private static final Map<PotionEffectType, PotionType> TYPE_CONVERSION = new HashMap<>();

    private final PotionEffectMode effectMode;
    private final PotionEffectType bukkitType;
    private final org.bukkit.potion.PotionType bukkitPotionType;

    static {
        for(PotionType type : PotionType.values()) {
            TYPE_CONVERSION.put(type.getBukkitType(), type);
        }
    }

    PotionType(PotionEffectMode effectMode, PotionEffectType bukkitType, org.bukkit.potion.PotionType bukkitPotionType) {
        this.effectMode = effectMode;
        this.bukkitType = bukkitType;
        this.bukkitPotionType = bukkitPotionType;
    }

    PotionType(PotionEffectMode effectMode, PotionEffectType bukkitType) {
        this(effectMode, bukkitType, org.bukkit.potion.PotionType.getByEffect(bukkitType));
    }

    public PotionEffectMode getEffectMode() {
        return effectMode;
    }

    public PotionEffectType getBukkitType() {
        return bukkitType;
    }

    public org.bukkit.potion.PotionType getBukkitPotionType() {
        return bukkitPotionType;
    }

    public PotionEffect toEffect(int duration, int amplifier, boolean ambient, boolean particles, boolean icon) {
        return new PotionEffect(this.bukkitType, duration, amplifier, ambient, particles, icon);
    }

    public static PotionType fromBukkit(PotionEffectType effectType) {
        return TYPE_CONVERSION.get(effectType);
    }

    public String getDisplayName() {
        return Arrays.stream(this.name().toLowerCase(Locale.ROOT).split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1)).collect(Collectors.joining(" "));
    }
}
