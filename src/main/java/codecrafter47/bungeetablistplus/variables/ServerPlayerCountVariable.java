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
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Variable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerPlayerCountVariable implements Variable {

    @Override
    public String getReplacement(ProxiedPlayer viewer, String args) {
        // TODO this shouldn't be a player variable
        if (args == null) {
            return Integer.toString(BungeeTabListPlus.getInstance().
                    getPlayerManager().getGlobalPlayerCount(viewer, BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3));
        }
        return Integer.toString(BungeeTabListPlus.getInstance().
                getPlayerManager().getPlayerCount(args, viewer, BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3));
    }

}
