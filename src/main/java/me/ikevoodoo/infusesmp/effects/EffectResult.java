package me.ikevoodoo.infusesmp.effects;

import org.bukkit.potion.PotionEffect;

public record EffectResult(PotionType effect, PotionEffect potionEffect, boolean added) {
}
