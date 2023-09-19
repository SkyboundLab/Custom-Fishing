/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.mechanic.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.ActionManager;
import net.momirealms.customfishing.api.manager.LootManager;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionExpansion;
import net.momirealms.customfishing.api.mechanic.action.ActionFactory;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.VaultHook;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.util.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ActionManagerImpl implements ActionManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, ActionFactory> actionBuilderMap;
    private final String EXPANSION_FOLDER = "expansions/action";

    public ActionManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.actionBuilderMap = new HashMap<>();
        this.registerInbuiltActions();
    }

    private void registerInbuiltActions() {
        this.registerMessageAction();
        this.registerCommandAction();
        this.registerMendingAction();
        this.registerExpAction();
        this.registerChainAction();
        this.registerPotionAction();
        this.registerSoundAction();
        this.registerPluginExpAction();
        this.registerTitleAction();
        this.registerActionBarAction();
        this.registerCloseInvAction();
        this.registerDelayedAction();
        this.registerConditionalAction();
        this.registerPriorityAction();
        this.registerLevelAction();
        this.registerHologramAction();
        this.registerFakeItemAction();
        this.registerFishFindAction();
        this.registerFoodAction();
        this.registerItemAmountAction();
        this.registerItemDurabilityAction();
        this.registerGiveItemAction();
        this.registerMoneyAction();
    }

    public void load() {
        this.loadExpansions();
        this.loadGlobalEventActions();
    }

    public void unload() {
        GlobalSettings.unload();
    }

    public void disable() {
        unload();
        this.actionBuilderMap.clear();
    }

    private void loadGlobalEventActions() {
        YamlConfiguration config = plugin.getConfig("config.yml");
        GlobalSettings.load(config.getConfigurationSection("mechanics.global-events"));
    }

    @Override
    public boolean registerAction(String type, ActionFactory actionFactory) {
        if (this.actionBuilderMap.containsKey(type)) return false;
        this.actionBuilderMap.put(type, actionFactory);
        return true;
    }

    @Override
    public boolean unregisterAction(String type) {
        return this.actionBuilderMap.remove(type) != null;
    }

    @Override
    public Action getAction(ConfigurationSection section) {
        return getActionBuilder(section.getString("type")).build(section.get("value"), section.getDouble("chance", 1d));
    }

    @Override
    public HashMap<ActionTrigger, Action[]> getActionMap(ConfigurationSection section) {
        HashMap<ActionTrigger, Action[]> actionMap = new HashMap<>();
        if (section == null) return actionMap;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionMap.put(
                        ActionTrigger.valueOf(entry.getKey().toUpperCase(Locale.ENGLISH)),
                        getActions(innerSection)
                );
            }
        }
        return actionMap;
    }

    @Nullable
    @Override
    public Action[] getActions(ConfigurationSection section) {
        if (section == null) return null;
        ArrayList<Action> actionList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                actionList.add(getAction(innerSection));
            }
        }
        return actionList.toArray(new Action[0]);
    }

    @Override
    public ActionFactory getActionBuilder(String type) {
        return actionBuilderMap.get(type);
    }

    private void registerMessageAction() {
        registerAction("message", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        msg,
                        condition.getArgs()
                );
                for (String text : replaced) {
                    AdventureManagerImpl.getInstance().sendPlayerMessage(condition.getPlayer(), text);
                }
            };
        });
        registerAction("broadcast", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        msg,
                        condition.getArgs()
                );
                for (Player player : Bukkit.getOnlinePlayers()) {
                    for (String text : replaced) {
                        AdventureManagerImpl.getInstance().sendPlayerMessage(player, text);
                    }
                }
            };
        });
        registerAction("message-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> msg = section.getStringList("message");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        msg,
                                        condition.getArgs()
                                );
                                for (String text : replaced) {
                                    AdventureManagerImpl.getInstance().sendPlayerMessage((Player) player, text);
                                }
                                condition.delArg("{near}");
                            }
                        }
                    }, condition.getLocation());
                };
            } else {
                LogUtils.warn("Illegal value format found at action: message-nearby");
                return null;
            }
        });
        registerAction("random-message", (args, chance) -> {
            ArrayList<String> msg = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = msg.get(ThreadLocalRandom.current().nextInt(msg.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                AdventureManagerImpl.getInstance().sendPlayerMessage(condition.getPlayer(), random);
            };
        });
    }

    private void registerCommandAction() {
        registerAction("command", (args, chance) -> {
            ArrayList<String> cmd = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                        condition.getPlayer(),
                        cmd,
                        condition.getArgs()
                );
                plugin.getScheduler().runTaskSync(() -> {
                    for (String text : replaced) {
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                    }
                }, condition.getLocation());
            };
        });
        registerAction("random-command", (args, chance) -> {
            ArrayList<String> cmd = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = cmd.get(ThreadLocalRandom.current().nextInt(cmd.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                String finalRandom = random;
                plugin.getScheduler().runTaskSync(() -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), finalRandom);
                }, condition.getLocation());
            };
        });
        registerAction("command-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> cmd = section.getStringList("command");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                List<String> replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        cmd,
                                        condition.getArgs()
                                );
                                for (String text : replaced) {
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), text);
                                }
                                condition.delArg("{near}");
                            }
                        }
                    }, condition.getLocation());
                };
            } else {
                LogUtils.warn("Illegal value format found at action: command-nearby");
                return null;
            }
        });
    }

    private void registerCloseInvAction() {
        registerAction("close-inv", (args, chance) -> condition -> {
            if (Math.random() > chance) return;
            condition.getPlayer().closeInventory();
        });
    }

    private void registerActionBarAction() {
        registerAction("actionbar", (args, chance) -> {
            String text = (String) args;
            return condition -> {
                if (Math.random() > chance) return;
                String parsed = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), text, condition.getArgs());
                AdventureManagerImpl.getInstance().sendActionbar(condition.getPlayer(), parsed);
            };
        });
        registerAction("random-actionbar", (args, chance) -> {
            ArrayList<String> texts = ConfigUtils.stringListArgs(args);
            return condition -> {
                if (Math.random() > chance) return;
                String random = texts.get(ThreadLocalRandom.current().nextInt(texts.size()));
                random = PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), random, condition.getArgs());
                AdventureManagerImpl.getInstance().sendActionbar(condition.getPlayer(), random);
            };
        });
        registerAction("actionbar-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String actionbar = section.getString("actionbar");
                int range = section.getInt("range");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    plugin.getScheduler().runTaskSync(() -> {
                        for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                            double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                            if (distance <= range) {
                                condition.insertArg("{near}", player.getName());
                                String replaced = PlaceholderManagerImpl.getInstance().parse(
                                        owner,
                                        actionbar,
                                        condition.getArgs()
                                );
                                AdventureManagerImpl.getInstance().sendActionbar((Player) player, replaced);
                                condition.delArg("{near}");
                            }
                        }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: command-nearby");
                return null;
            }
        });
    }

    private void registerMendingAction() {
        registerAction("mending", (args, chance) -> {
            int xp = (int) args;
            return condition -> {
                if (Math.random() > chance) return;
                if (CustomFishingPlugin.get().getVersionManager().isSpigot()) {
                    condition.getPlayer().getLocation().getWorld().spawn(condition.getPlayer().getLocation(), ExperienceOrb.class, e -> e.setExperience(xp));
                } else {
                    condition.getPlayer().giveExp(xp, true);
                    AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
                }
            };
        });
    }

    private void registerFoodAction() {
        registerAction("food", (args, chance) -> {
            int food = (int) (ConfigUtils.getDoubleValue(args) * 2);
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setFoodLevel(player.getFoodLevel() + food);
            };
        });
        registerAction("saturation", (args, chance) -> {
            double saturation = ConfigUtils.getDoubleValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setSaturation((float) (player.getSaturation() + saturation));
            };
        });
    }

    private void registerExpAction() {
        registerAction("exp", (args, chance) -> {
            int xp = (int) args;
            return condition -> {
                if (Math.random() > chance) return;
                condition.getPlayer().giveExp(xp);
            };
        });
    }

    private void registerHologramAction() {
        registerAction("hologram", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String text = section.getString("text", "");
                int duration = section.getInt("duration", 20);
                boolean position = section.getString("position", "other").equals("other");
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                int range = section.getInt("range", 16);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player owner = condition.getPlayer();
                    Location location = position ? condition.getLocation() : owner.getLocation();
                    plugin.getScheduler().runTaskSync(() -> {
                            for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                                double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                                if (distance <= range) {
                                    ArmorStandUtils.sendHologram(
                                            (Player) player,
                                            location.clone().add(x, y, z),
                                            AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                                    PlaceholderManagerImpl.getInstance().parse(owner, text, condition.getArgs())
                                            ),
                                            duration
                                    );
                                }
                            }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: hologram");
                return null;
            }
        });
    }

    private void registerItemAmountAction() {
        registerAction("item-amount", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                boolean mainOrOff = section.getString("hand", "main").equalsIgnoreCase("main");
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemStack itemStack = mainOrOff ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
                    itemStack.setAmount(Math.max(0, itemStack.getAmount() + amount));
                };
            } else {
                LogUtils.warn("Illegal value format found at action: item-amount");
                return null;
            }
        });
    }

    private void registerItemDurabilityAction() {
        registerAction("durability", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                EquipmentSlot slot = EquipmentSlot.valueOf(section.getString("slot", "hand").toUpperCase(Locale.ENGLISH));
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemStack itemStack = player.getInventory().getItem(slot);
                    if (amount > 0) {
                        ItemUtils.addDurability(itemStack, amount, true);
                    } else {
                        ItemUtils.loseDurability(itemStack, -amount, true);
                    }
                };
            } else {
                LogUtils.warn("Illegal value format found at action: item-durability");
                return null;
            }
        });
    }

    private void registerGiveItemAction() {
        registerAction("give-item", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String id = section.getString("item");
                int amount = section.getInt("amount", 1);
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    ItemUtils.giveCertainAmountOfItem(player, CustomFishingPlugin.get().getItemManager().buildAnyItemByID(player, id), amount);
                };
            } else {
                LogUtils.warn("Illegal value format found at action: give-item");
                return null;
            }
        });
    }

    private void registerFakeItemAction() {
        registerAction("fake-item", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String[] itemSplit = section.getString("item", "").split(":", 2);
                int duration = section.getInt("duration", 20);
                boolean position = section.getString("position", "hook").equals("hook");
                double x = section.getDouble("x");
                double y = section.getDouble("y");
                double z = section.getDouble("z");
                return condition -> {
                    if (Math.random() > chance) return;
                    Player player = condition.getPlayer();
                    Location location = position ? condition.getLocation() : player.getLocation();
                    ArmorStandUtils.sendFakeItem(
                            condition.getPlayer(),
                            location.clone().add(x, y, z),
                            plugin.getItemManager().build(player, itemSplit[0], itemSplit[1], condition.getArgs()),
                            duration
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: hologram");
                return null;
            }
        });
    }

    private void registerChainAction() {
        registerAction("chain", (args, chance) -> {
            List<Action> actions = new ArrayList<>();
            if (args instanceof ConfigurationSection section) {
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection innerSection) {
                        actions.add(getAction(innerSection));
                    }
                }
            }
            return condition -> {
                if (Math.random() > chance) return;
                for (Action action : actions) {
                    action.trigger(condition);
                }
            };
        });
    }

    private void registerMoneyAction() {
        registerAction("give-money", (args, chance) -> {
            double money = ConfigUtils.getDoubleValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                VaultHook.getEconomy().depositPlayer(condition.getPlayer(), money);
            };
        });
        registerAction("take-money", (args, chance) -> {
            double money = ConfigUtils.getDoubleValue(args);
            return condition -> {
                if (Math.random() > chance) return;
                VaultHook.getEconomy().withdrawPlayer(condition.getPlayer(), money);
            };
        });
    }

    private void registerDelayedAction() {
        registerAction("delay", (args, chance) -> {
            List<Action> actions = new ArrayList<>();
            int delay;
            if (args instanceof ConfigurationSection section) {
                delay = section.getInt("delay", 1);
                ConfigurationSection actionSection = section.getConfigurationSection("actions");
                if (actionSection != null) {
                    for (Map.Entry<String, Object> entry : actionSection.getValues(false).entrySet()) {
                        if (entry.getValue() instanceof ConfigurationSection innerSection) {
                            actions.add(getAction(innerSection));
                        }
                    }
                }
            } else {
                delay = 1;
            }
            return condition -> {
                if (Math.random() > chance) return;
                plugin.getScheduler().runTaskSyncLater(() -> {
                    for (Action action : actions) {
                        action.trigger(condition);
                    }
                }, condition.getLocation(), delay * 50L, TimeUnit.MILLISECONDS);
            };
        });
    }

    private void registerTitleAction() {
        registerAction("title", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String title = section.getString("title");
                String subtitle = section.getString("subtitle");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendTitle(
                            condition.getPlayer(),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), title, condition.getArgs()),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitle, condition.getArgs()),
                            fadeIn * 50,
                            stay * 50,
                            fadeOut * 50
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: title");
                return null;
            }
        });
        registerAction("title-nearby", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String title = section.getString("title");
                String subtitle = section.getString("subtitle");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                int range = section.getInt("range", 32);
                return condition -> {
                    if (Math.random() > chance) return;
                    plugin.getScheduler().runTaskSync(() -> {
                            for (Entity player : condition.getLocation().getWorld().getNearbyEntities(condition.getLocation(), range, range, range, entity -> entity instanceof Player)) {
                                double distance = LocationUtils.getDistance(player.getLocation(), condition.getLocation());
                                if (distance <= range) {
                                    condition.insertArg("{near}", player.getName());
                                    AdventureManagerImpl.getInstance().sendTitle(
                                            condition.getPlayer(),
                                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), title, condition.getArgs()),
                                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitle, condition.getArgs()),
                                            fadeIn * 50,
                                            stay * 50,
                                            fadeOut * 50
                                    );
                                    condition.delArg("{near}");
                                }
                            }
                        }, condition.getLocation()
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: title-nearby");
                return null;
            }
        });
        registerAction("random-title", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<String> titles = section.getStringList("titles");
                List<String> subtitles = section.getStringList("subtitles");
                int fadeIn = section.getInt("fade-in", 20);
                int stay = section.getInt("stay", 30);
                int fadeOut = section.getInt("fade-out", 10);
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendTitle(
                            condition.getPlayer(),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), titles.get(ThreadLocalRandom.current().nextInt(titles.size())), condition.getArgs()),
                            PlaceholderManagerImpl.getInstance().parse(condition.getPlayer(), subtitles.get(ThreadLocalRandom.current().nextInt(subtitles.size())), condition.getArgs()),
                            fadeIn * 50,
                            stay * 50,
                            fadeOut * 50
                    );
                };
            } else {
                LogUtils.warn("Illegal value format found at action: random-title");
                return null;
            }
        });
    }

    private void registerPotionAction() {
        registerAction("potion-effect", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                PotionEffect potionEffect = new PotionEffect(
                        Objects.requireNonNull(PotionEffectType.getByName(section.getString("type", "BLINDNESS").toUpperCase(Locale.ENGLISH))),
                        section.getInt("duration", 20),
                        section.getInt("amplifier", 0)
                );
                return condition -> {
                    if (Math.random() > chance) return;
                    condition.getPlayer().addPotionEffect(potionEffect);
                };
            }
            LogUtils.warn("Illegal value format found at action: potion-effect");
            return null;
        });
    }

    private void registerLevelAction() {
        registerAction("level", (args, chance) -> {
            int level = (int) args;
            return condition -> {
                if (Math.random() > chance) return;
                Player player = condition.getPlayer();
                player.setLevel(Math.max(0, player.getLevel() + level));
            };
        });
    }

    @SuppressWarnings("all")
    private void registerSoundAction() {
        registerAction("sound", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                Sound sound = Sound.sound(
                        Key.key(section.getString("key")),
                        Sound.Source.valueOf(section.getString("source", "PLAYER").toUpperCase(Locale.ENGLISH)),
                        (float) section.getDouble("volume", 1),
                        (float) section.getDouble("pitch", 1)
                );
                return condition -> {
                    if (Math.random() > chance) return;
                    AdventureManagerImpl.getInstance().sendSound(condition.getPlayer(), sound);
                };
            }
            LogUtils.warn("Illegal value format found at action: sound");
            return null;
        });
    }

    private void registerConditionalAction() {
        registerAction("conditional", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                Action[] actions = getActions(section.getConfigurationSection("actions"));
                Requirement[] requirements = plugin.getRequirementManager().getRequirements(section.getConfigurationSection("conditions"), true);
                return condition -> {
                    if (Math.random() > chance) return;
                    if (requirements != null)
                        for (Requirement requirement : requirements) {
                            if (!requirement.isConditionMet(condition)) {
                                return;
                            }
                        }
                    if (actions != null)
                        for (Action action : actions) {
                            action.trigger(condition);
                        }
                };
            }
            LogUtils.warn("Illegal value format found at action: conditional");
            return null;
        });
    }

    private void registerPriorityAction() {
        registerAction("priority", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                List<Pair<Requirement[], Action[]>> conditionActionPairList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection inner) {
                        Action[] actions = getActions(inner.getConfigurationSection("actions"));
                        Requirement[] requirements = plugin.getRequirementManager().getRequirements(inner.getConfigurationSection("conditions"), false);
                        conditionActionPairList.add(Pair.of(requirements, actions));
                    }
                }
                return condition -> {
                    if (Math.random() > chance) return;
                    outer:
                        for (Pair<Requirement[], Action[]> pair : conditionActionPairList) {
                            if (pair.left() != null)
                                for (Requirement requirement : pair.left()) {
                                    if (!requirement.isConditionMet(condition)) {
                                        continue outer;
                                    }
                                }
                            if (pair.right() != null)
                                for (Action action : pair.right()) {
                                    action.trigger(condition);
                                }
                            return;
                        }
                };
            }
            LogUtils.warn("Illegal value format found at action: conditional");
            return null;
        });
    }

    private void registerPluginExpAction() {
        registerAction("plugin-exp", (args, chance) -> {
            if (args instanceof ConfigurationSection section) {
                String pluginName = section.getString("plugin");
                double exp = section.getDouble("exp", 1);
                String target = section.getString("target");
                return condition -> {
                    if (Math.random() > chance) return;
                    Optional.ofNullable(plugin.getIntegrationManager().getLevelHook(pluginName)).ifPresentOrElse(it -> {
                        it.addXp(condition.getPlayer(), target, exp);
                    }, () -> LogUtils.warn("Plugin (" + pluginName + "'s) level is not compatible. Please double check if it's a problem caused by pronunciation."));
                };
            }
            return null;
        });
    }

    private void registerFishFindAction() {
        registerAction("fish-finder", (args, chance) -> {
            boolean arg = (boolean) args;
            return condition -> {
                if (Math.random() > chance) return;
                condition.insertArg("{lava}", String.valueOf(arg));
                LootManager lootManager = plugin.getLootManager();
                List<String> loots = plugin.getFishingManager().getPossibleLootKeys(condition).stream().map(lootManager::getLoot).filter(Objects::nonNull).filter(Loot::showInFinder).map(Loot::getNick).toList();
                StringJoiner stringJoiner = new StringJoiner(CFLocale.MSG_Split_Char);
                for (String loot : loots) {
                    stringJoiner.add(loot);
                }
                condition.delArg("{lava}");
                AdventureManagerImpl.getInstance().sendMessageWithPrefix(condition.getPlayer(), CFLocale.MSG_Possible_Loots + stringJoiner);
            };
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadExpansions() {
        File expansionFolder = new File(plugin.getDataFolder(), EXPANSION_FOLDER);
        if (!expansionFolder.exists())
            expansionFolder.mkdirs();

        List<Class<? extends ActionExpansion>> classes = new ArrayList<>();
        File[] expansionJars = expansionFolder.listFiles();
        if (expansionJars == null) return;
        for (File expansionJar : expansionJars) {
            if (expansionJar.getName().endsWith(".jar")) {
                try {
                    Class<? extends ActionExpansion> expansionClass = ClassUtils.findClass(expansionJar, ActionExpansion.class);
                    classes.add(expansionClass);
                } catch (IOException | ClassNotFoundException e) {
                    LogUtils.warn("Failed to load expansion: " + expansionJar.getName(), e);
                }
            }
        }
        try {
            for (Class<? extends ActionExpansion> expansionClass : classes) {
                ActionExpansion expansion = expansionClass.getDeclaredConstructor().newInstance();
                unregisterAction(expansion.getActionType());
                registerAction(expansion.getActionType(), expansion.getActionFactory());
                LogUtils.info("Loaded action expansion: " + expansion.getActionType() + "[" + expansion.getVersion() + "]" + " by " + expansion.getAuthor() );
            }
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            LogUtils.warn("Error occurred when creating expansion instance.", e);
        }
    }
}
