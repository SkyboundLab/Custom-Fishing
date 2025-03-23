/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.integration.item;

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class OraxenItemProvider implements ItemProvider {

    @Override
    public String identifier() {
        return "Oraxen";
    }

    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Context<Player> player, @NotNull String id) {
        ItemBuilder itemBuilder = OraxenItems.getItemById(id);
        return itemBuilder == null ? new ItemStack(Material.AIR) : itemBuilder.build();
    }

    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return OraxenItems.getIdByItem(itemStack);
    }
}
