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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.bungeetablistplus.player.IPlayerProvider;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlayerManager {

    private final BungeeTabListPlus plugin;

    private final Collection<IPlayerProvider> playerProviders;

    public PlayerManager(BungeeTabListPlus plugin, Collection<IPlayerProvider> playerProviders) {
        this.plugin = plugin;
        this.playerProviders = playerProviders;
    }

    public boolean isServer(String s) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (s.equalsIgnoreCase(server.getName())) {
                return true;
            }
            int i = s.indexOf('#');
            if (i > 1) {
                if (s.substring(0, i).equalsIgnoreCase(server.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<IPlayer> getPlayers(Collection<String> filter,
                                    ProxiedPlayer who, boolean includeSuspectors) {
        List<IPlayer> list = new ArrayList<>();
        Iterable<IPlayer> players = Iterables.concat(Collections2.transform(playerProviders, new Function<IPlayerProvider, Iterable<IPlayer>>() {
            @Override
            public Iterable<IPlayer> apply(IPlayerProvider iPlayerProvider) {
                return iPlayerProvider.getPlayers();
            }
        }));
        for (IPlayer p : players) {
            boolean areGroupRules = false;
            boolean areServerRules = false;
            boolean fitGroupRules = false;
            boolean fitServerRules = false;
            String group = plugin.getPermissionManager().getMainGroup(p);
            for (String rule : filter) {
                if (rule.isEmpty()) {
                    // ignore
                } else {
                    Optional<ServerInfo> server = p.getServer();
                    if (rule.equalsIgnoreCase("currentserver")) {
                        areServerRules = true;
                        if (server.isPresent() && who.getServer() != null) {
                            if (server.get().getName().equalsIgnoreCase(
                                    who.getServer().getInfo().getName())) {
                                fitServerRules = true;
                            }
                        }
                    } else if (isServer(rule)) {
                        areServerRules = true;
                        if (server.isPresent()) {
                            if (server.get().getName().equalsIgnoreCase(rule)) {
                                fitServerRules = true;
                            }
                            String[] s = rule.split("#");
                            if (s.length == 2) {
                                if (server.get().getName().
                                        equalsIgnoreCase(s[0])) {
                                    String world = plugin.getBridge().
                                            getPlayerInformation(p, "world");
                                    if (world != null) {
                                        if (world.equalsIgnoreCase(s[1])) {
                                            fitServerRules = true;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        areGroupRules = true;
                        if (group != null) {
                            if (group.equalsIgnoreCase(rule)) {
                                fitGroupRules = true;
                            }
                        }
                    }
                }
            }
            if (((!areServerRules) || fitServerRules) && ((!areGroupRules) || fitGroupRules) && !BungeeTabListPlus.
                    isHidden(p, who) && (includeSuspectors || p.getGameMode() != 3)) {
                list.add(p);
            }
        }
        return list;
    }

    public int getServerPlayerCount(String server, ProxiedPlayer viewer, boolean includeSuspectors) {
        int num = 0;
        Iterable<IPlayer> players = Iterables.concat(Collections2.transform(playerProviders, new Function<IPlayerProvider, Iterable<IPlayer>>() {
            @Override
            public Iterable<IPlayer> apply(IPlayerProvider iPlayerProvider) {
                return iPlayerProvider.getPlayers();
            }
        }));
        for (IPlayer p : players) {
            Optional<ServerInfo> s = p.getServer();
            if (s.isPresent()) {
                if (s.get().getName().equalsIgnoreCase(server) && !BungeeTabListPlus.
                        isHidden(p, viewer) && (includeSuspectors || p.getGameMode() != 3)) {
                    num++;
                }
            }
        }
        return num;
    }

    public int getGlobalPlayerCount(ProxiedPlayer viewer, boolean includeSuspectors) {
        int num = 0;
        Iterable<IPlayer> players = Iterables.concat(Collections2.transform(playerProviders, new Function<IPlayerProvider, Iterable<IPlayer>>() {
            @Override
            public Iterable<IPlayer> apply(IPlayerProvider iPlayerProvider) {
                return iPlayerProvider.getPlayers();
            }
        }));
        for (IPlayer p : players) {
            if (!BungeeTabListPlus.isHidden(p, viewer) && (includeSuspectors || p.getGameMode() != 3)) {
                num++;
            }
        }
        return num;
    }

    public int getPlayerCount(String args, ProxiedPlayer player, boolean includeSuspectors) {
        String tmp = args.replaceAll(",", "+");
        String[] all = tmp.split("\\+");
        return this.getPlayers(Arrays.asList(all), player, includeSuspectors).size();
    }
}
