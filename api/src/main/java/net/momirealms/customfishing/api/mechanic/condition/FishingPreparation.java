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

package net.momirealms.customfishing.api.mechanic.condition;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FishingPreparation extends Condition {

    private boolean hasBait = false;
    private @Nullable ItemStack baitItemStack;
    private final @NotNull ItemStack rodItemStack;
    private final List<EffectCarrier> effects;
    private boolean canFish = true;

    public FishingPreparation(Player player, CustomFishingPlugin plugin) {
        super(player);

        PlayerInventory playerInventory = player.getInventory();
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        ItemStack offHandItem = playerInventory.getItemInOffHand();

        this.effects = new ArrayList<>();
        boolean rodOnMainHand = mainHandItem.getType() == Material.FISHING_ROD;
        this.rodItemStack = rodOnMainHand ? mainHandItem : offHandItem;
        String rodItemID = plugin.getItemManager().getAnyItemID(this.rodItemStack);
        EffectCarrier rodEffect = plugin.getEffectManager().getEffect("rod", rodItemID);
        if (rodEffect != null) effects.add(rodEffect);
        super.insertArg("{rod}", rodItemID);

        NBTItem nbtItem = new NBTItem(rodItemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.hasTag("hook_id")) {
            String hookID = cfCompound.getString("hook_id");
            super.insertArg("{hook}", rodItemID);
            EffectCarrier carrier = plugin.getEffectManager().getEffect("hook", hookID);
            if (carrier != null) {
                this.effects.add(carrier);
            }
        }

        String baitItemID = plugin.getItemManager().getAnyItemID(rodOnMainHand ? offHandItem : mainHandItem);
        EffectCarrier baitEffect = plugin.getEffectManager().getEffect("bait", baitItemID);

        if (baitEffect != null) {
            this.baitItemStack = rodOnMainHand ? offHandItem : mainHandItem;
            this.effects.add(baitEffect);
            this.hasBait = true;
            super.insertArg("{bait}", baitItemID);
        }

        if (plugin.getBagManager().isEnabled()) {
            Inventory fishingBag = plugin.getBagManager().getOnlineBagInventory(player.getUniqueId());
            HashSet<String> uniqueUtils = new HashSet<>(4);
            if (fishingBag != null) {
                this.insertArg("{in-bag}", "true");
                for (int i = 0; i < fishingBag.getSize(); i++) {
                    ItemStack itemInBag = fishingBag.getItem(i);
                    String bagItemID = plugin.getItemManager().getItemID(itemInBag);
                    if (bagItemID == null) continue;
                    if (!hasBait) {
                        EffectCarrier effect = plugin.getEffectManager().getEffect("bait", bagItemID);
                        if (effect != null) {
                            this.baitItemStack = itemInBag;
                            this.effects.add(effect);
                            super.insertArg("{bait}", bagItemID);
                            continue;
                        }
                    }
                    EffectCarrier utilEffect = plugin.getEffectManager().getEffect("util", bagItemID);
                    if (utilEffect != null && !uniqueUtils.contains(bagItemID)) {
                        effects.add(utilEffect);
                        uniqueUtils.add(bagItemID);
                    }
                }
                this.delArg("{in-bag}");
            }
        }

        for (String enchant : plugin.getIntegrationManager().getEnchantments(rodItemStack)) {
            EffectCarrier enchantEffect = plugin.getEffectManager().getEffect("enchant", enchant);
            if (enchantEffect != null) {
                this.effects.add(enchantEffect);
            }
        }

        for (EffectCarrier effectCarrier : effects) {
            if (!effectCarrier.isConditionMet(this)) {
                this.canFish = false;
                return;
            }
        }
    }

    @NotNull
    public ItemStack getRodItemStack() {
        return rodItemStack;
    }

    @Nullable
    public ItemStack getBaitItemStack() {
        return baitItemStack;
    }

    public boolean canFish() {
        return this.canFish;
    }

    public void mergeEffect(FishingEffect effect) {
        for (EffectCarrier effectCarrier : effects) {
            for (EffectModifier modifier : effectCarrier.getEffectModifiers()) {
                modifier.modify(effect, this);
            }
        }
    }

    public void triggerActions(ActionTrigger actionTrigger) {
        GlobalSettings.triggerRodActions(actionTrigger, this);
        for (EffectCarrier effectCarrier : effects) {
            Action[] actions = effectCarrier.getActions(actionTrigger);
            if (actions != null)
                for (Action action : actions) {
                    action.trigger(this);
                }
        }
    }
}
