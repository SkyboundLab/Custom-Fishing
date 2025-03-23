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

package net.momirealms.customfishing.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that is triggered before an item is collected into the fishing bag.
 * It can be cancelled to prevent the item from being collected.
 */
public class FishingBagPreCollectEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private final ItemStack itemStack;
    private boolean isCancelled;
    private final Inventory bag;

    /**
     * Constructs a new FishingBagPreCollectEvent.
     *
     * @param who The player who is collecting the item
     * @param itemStack The item that is being collected into the fishing bag
     * @param bag The inventory of the fishing bag
     */
    public FishingBagPreCollectEvent(@NotNull Player who, ItemStack itemStack, Inventory bag) {
        super(who);
        this.itemStack = itemStack;
        this.isCancelled = false;
        this.bag = bag;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    /**
     * Gets the {@link ItemStack} that is being collected into the fishing bag.
     *
     * @return The item being collected
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * Gets the {@link Inventory} of the fishing bag.
     *
     * @return The inventory of the fishing bag
     */
    @NotNull
    public Inventory getBagInventory() {
        return bag;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
