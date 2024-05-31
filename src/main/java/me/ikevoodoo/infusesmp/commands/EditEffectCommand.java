package me.ikevoodoo.infusesmp.commands;

import me.ikevoodoo.infusesmp.config.GeneralConfig;
import me.ikevoodoo.infusesmp.effects.EffectResult;
import me.ikevoodoo.infusesmp.effects.PotionType;
import me.ikevoodoo.infusesmp.managers.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class EditEffectCommand implements TabExecutor {

    private final GeneralConfig generalConfig;
    private final EffectManager effectManager;

    public EditEffectCommand(GeneralConfig generalConfig, EffectManager effectManager) {
        this.generalConfig = generalConfig;
        this.effectManager = effectManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cMissing arguments."); // meh error message, too little time
            return true;
        }

        var addMode = args[0].equalsIgnoreCase("add");
        var boostMode = args[0].equalsIgnoreCase("boost");
        var effectMode = args[1].equalsIgnoreCase("effect");

        var index = 2;
        if (effectMode) {
            index++;
        }
        if (boostMode) {
            index--;
        }

        var target = args.length <= index ? null : Bukkit.getPlayer(args[index]);
        if (target == null) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage("§cPlease provide a player1");
                return true;
            }
        }

        if (effectMode || boostMode) {
            if (args.length < index) {
                sender.sendMessage("§cPlease provide an effect!");
                return true;
            }

            try {
                var type = PotionType.valueOf(args[effectMode ? 2 : 1].toUpperCase(Locale.ROOT));

                if (boostMode) {
                    if (!this.effectManager.hasEffect(target, type)) {
                        sender.sendMessage("§cThe target doesn't have the effect §e" + type.getDisplayName() + "§c!");
                        return true;
                    }

                    this.effectManager.amplifyEffectNow(target, type, 1);
                    sender.sendMessage("§aEffect §e" + type.getDisplayName() + "§a was amplified for §6" + target.getName());
                    return true;
                }

                if (addMode) {
                    this.effectManager.giveEffectNow(target, type);
                } else {
                    this.effectManager.removeEffectNow(target, type);
                }
                sender.sendMessage("§aSuccessfully " + (addMode ? "given" : "removed") + " potion effect §e" + type.getDisplayName() + " §a" + (addMode ? "to" : "from") +" §6" + target.getDisplayName());
            } catch (IllegalArgumentException ignored) {
                sender.sendMessage("§cUnknown potion type: " + args[effectMode ? 2 : 1]);
            }

            return true;
        }

        var positiveMode = args[1].equalsIgnoreCase("positive");

        var function = getApplyEffect(addMode, positiveMode);
        var effect = function.apply(target);
        sender.sendMessage("§aSuccessfully " + (effect.added() ? "added " : "removed ") + (positiveMode ? "positive " : "negative ") + "effect §e" + effect.effect().getDisplayName());

        return false;
    }

    private @NotNull Function<LivingEntity, EffectResult> getApplyEffect(boolean addMode, boolean positiveMode) {
        Function<LivingEntity, EffectResult> function;
        if (addMode) {
            if (positiveMode) {
                function = this.effectManager::addPositiveEffectNow;
            } else {
                function = this.effectManager::addNegativeEffectNow;
            }
        } else {
            if (positiveMode) {
                function = entity -> {
                    var res = this.effectManager.removePositiveEffectNow(entity);
                    return new EffectResult(PotionType.fromBukkit(res.getType()), res, false);
                };
            } else {
                function =  entity -> {
                    var res = this.effectManager.removeNegativeEffectNow(entity);
                    return new EffectResult(PotionType.fromBukkit(res.getType()), res, false);
                };
            }
        }
        return function;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length <= 1) {
            return List.of("boost", "add", "remove");
        }

        var boost = args[0].equalsIgnoreCase("boost");

        if (args.length == 2 && !boost) {
            return List.of("effect", "positive", "negative");
        }

        if (args[1].equalsIgnoreCase("effect") || boost) {
            var idx = 2;
            if (boost) {
                idx--;
            }

            if (args.length > idx + 2) {
                return List.of();
            }

            if (args.length == idx + 2) {
                return getPlayers(args[idx + 1].toLowerCase(Locale.ROOT));
            }

            return getEffects(args[idx].toLowerCase(Locale.ROOT));
        }

        if (args.length > 3) {
            return List.of();
        }

        return getPlayers(args[2].toLowerCase(Locale.ROOT));
    }

    private List<String> getEffects(String filter) {
        var arr = new ArrayList<String>();
        for (var type : this.generalConfig.getEffectConfig().getNegative().keySet()) {
            var name = type.name().toLowerCase(Locale.ROOT);
            if (!name.startsWith(filter)) continue;
            arr.add(name);
        }

        for (var type : this.generalConfig.getEffectConfig().getPositive().keySet()) {
            var name = type.name().toLowerCase(Locale.ROOT);
            if (!name.startsWith(filter)) continue;
            arr.add(name);
        }

        return arr;
    }

    private List<String> getPlayers(String filter) {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(s -> s.startsWith(filter)).toList();
    }
}
