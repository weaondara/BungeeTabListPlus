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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.skin.Skin;
import com.google.common.base.Optional;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class RedisPlayer implements IPlayer {
    private String name;
    private final UUID uuid;
    private ServerInfo server;
    private long lastServerLookup = 0;

    public RedisPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = RedisBungee.getApi().getNameFromUuid(uuid);
        }
        return name;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public Optional<ServerInfo> getServer() {
        if (server == null || System.currentTimeMillis() - lastServerLookup > 1000) {
            server = RedisBungee.getApi().getServerFor(uuid);
            lastServerLookup = System.currentTimeMillis();
        }
        return Optional.fromNullable(server);
    }

    @Override
    public int getPing() {
        // no way to know the real ping, so we just assume the best
        return 0;
    }

    @Override
    public Skin getSkin() {
        return BungeeTabListPlus.getInstance().getSkinManager().getSkin(uuid.toString());
    }

    @Override
    public int getGameMode() {
        // we don't know -> survival mode
        return 0;
    }
}
