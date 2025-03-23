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

package net.momirealms.customfishing.common.config.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Node<T> {

    private final T value;
    private final HashMap<String, Node<T>> childTree = new HashMap<>();

    public Node(T value) {
        this.value = value;
    }

    public Node() {
        this(null);
    }

    @Nullable
    public T nodeValue() {
        return value;
    }

    @NotNull
    public HashMap<String, Node<T>> getChildTree() {
        return childTree;
    }

    @Nullable
    public Node<T> getChild(String node) {
        return childTree.get(node);
    }

    @Nullable
    public Node<T> removeChild(String node) {
        return childTree.remove(node);
    }
}
