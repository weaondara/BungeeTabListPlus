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
package codecrafter47.bungeetablistplus.sorting;

import codecrafter47.bungeetablistplus.player.IPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;

/**
 * @author Florian Stober
 */
public class YouFirst implements ISortingRule {

    private final ProxiedPlayer player;

    public YouFirst(ProxiedPlayer p) {
        player = p;
    }

    @Override
    public int compare(IPlayer player1, IPlayer player2) {
        if (Objects.equals(player1.getName(), player.getName())) {
            return -1;
        }
        if (Objects.equals(player2.getName(), player.getName())) {
            return 1;
        }
        return 0;
    }

}
