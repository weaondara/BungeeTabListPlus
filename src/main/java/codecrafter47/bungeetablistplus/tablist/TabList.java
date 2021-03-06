/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.skin.Skin;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Florian Stober
 */
public class TabList implements ITabList {

    private final int rows;
    private final int collums;
    private int usedSlots;
    private final Slot[] slots;
    private String header;
    private String footer;
    @Getter
    @Setter
    private Skin defaultSkin;
    private int defaultPing;
    private int size;

    @Override
    public String getHeader() {
        return header;
    }

    @Override
    public void setHeader(String header) {
        this.header = header;
    }

    @Override
    public String getFooter() {
        return footer;
    }

    @Override
    public void setFooter(String footer) {
        this.footer = footer;
    }

    public TabList() {
        this.rows = ConfigManager.getRows();
        this.collums = ConfigManager.getCols();
        this.usedSlots = 0;
        this.slots = new Slot[rows * collums];
        header = null;
        footer = null;
        size = rows * collums;
    }

    @Override
    public int getRows() {
        return this.rows;
    }

    @Override
    public int getColumns() {
        return this.collums;
    }

    @Override
    public int getUsedSlots() {
        return this.usedSlots;
    }

    @Override
    public Slot getSlot(int n) {
        return this.slots[n];
    }

    @Override
    public Slot getSlot(int row, int column) {
        return getSlot(row * collums + column);
    }

    @Override
    public void setSlot(int n, Slot s) {
        if (n >= slots.length) {
            return;
        }
        this.slots[n] = s;
        if (n + 1 > usedSlots) {
            usedSlots = n + 1;
        }
    }

    @Override
    public void setSlot(int row, int column, Slot s) {
        setSlot(row * collums + column, s);
    }

    @Override
    public int getDefaultPing() {
        return defaultPing;
    }

    @Override
    public void setDefaultPing(int defaultPing) {
        this.defaultPing = defaultPing;
    }

    @Override
    public int getSize() {
        return size;
    }
}
