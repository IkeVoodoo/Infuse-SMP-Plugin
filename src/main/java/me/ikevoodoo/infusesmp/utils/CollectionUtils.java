package me.ikevoodoo.infusesmp.utils;

import me.ikevoodoo.infusesmp.config.effects.EffectData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class CollectionUtils {

    private CollectionUtils() {

    }

    public static <K, V> Map.Entry<K, V> randomInMap(Map<K, V> map) {
        if (map.isEmpty()) {
            return null;
        }

        int skip = ThreadLocalRandom.current().nextInt(map.size());
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (i < skip) {
                i++;
                continue;
            }

            return entry;
        }

        return null;
    }

}
