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
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.packets.TabHeaderPacket;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

import java.util.UUID;

public class TabList18 extends CustomTabList18 implements PlayerTablistHandler {

    private static String getSlotID(int n) {
        String s = Integer.toString(n + 1000);
        return "0000tab#" + s;
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    private final String[] sendTextures = new String[ConfigManager.getTabSize()];

    public TabList18(ProxiedPlayer player) {
        super(player);
    }

    @Override
    public void sendTablist(ITabList tabList) {
        resize(tabList.getColumns() * tabList.getRows());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getColumns() * tabList.getRows(); i++) {
            Slot line = tabList.getSlot((i % tabList.getRows()) * tabList.
                    getColumns() + (i / tabList.getRows()));
            if (line == null) {
                line = new Slot(" ", tabList.getDefaultPing());
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(getPlayer(), line.text, BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(super.getPlayer()));
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(getPlayer(), text);
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (charLimit > 0) {
                text = ColorParser.substringIgnoreColors(text, charLimit);
                for (int j = charLimit - ChatColor.stripColor(text).length(); j > 0; j--) {
                    text += ' ';
                }
            }
            if (text.endsWith("" + ChatColor.COLOR_CHAR)) {
                text = text.substring(0, text.length() - 1);
            }

            if (line.getSkin() == SkinManager.defaultSkin) {
                line.setSkin(tabList.getDefaultSkin());
            }
            updateSlot(i, text, line.ping, line.getSkin());
        }

        // update header/footer
        String header = tabList.getHeader();
        if (header != null && header.endsWith("" + ChatColor.COLOR_CHAR)) {
            header = header.substring(0, header.length() - 1);
        }
        String footer = tabList.getFooter();
        if (footer != null && footer.endsWith("" + ChatColor.COLOR_CHAR)) {
            footer = footer.substring(0, footer.length() - 1);
        }
        if (header != null || footer != null) {
            if (BungeeTabListPlus.isAbove995()) {
                player.setTabHeader(TextComponent.fromLegacyText(header), TextComponent.fromLegacyText(footer));
            } else {
                TabHeaderPacket packet = new TabHeaderPacket();
                if (header != null) {
                    packet.setHeader(ComponentSerializer.toString(TextComponent.
                            fromLegacyText(header)));
                }
                if (footer != null) {
                    packet.setFooter(ComponentSerializer.toString(TextComponent.
                            fromLegacyText(footer)));
                }
                player.unsafe().sendPacket(packet);
            }
        }
    }

    private void resize(int size) {
        if (size == sendSlots) {
            return;
        }
        if (size > sendSlots) {
            for (int i = sendSlots; i < size; i++) {
                createSlot(i);
            }
            sendSlots = size;
        } else if (size < sendSlots) {
            for (int i = size; i < sendSlots; i++) {
                removeSlot(i);
            }
        }
        sendSlots = size;
    }

    private void removeSlot(int i) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(i)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setDisplayName(" ");
        item.setGamemode(0);
        item.setPing(0);
        item.setUsername(getSlotID(i));
        item.setProperties(new String[0][0]);
        pli.setItems(new Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
    }

    private void updateSlot(int row, String text, int ping, Skin skin) {
        boolean textureUpdate = false;
        String[] textures = skin.toProperty();
        if (textures != null) {
            textures = new String[]{textures[1], textures[2]};
        }
        if ((sendTextures[row] == null && textures != null) || (sendTextures[row] != null && textures == null) || (textures != null && sendTextures[row] != null && !textures[0].
                equals(
                        sendTextures[row]))) {
            // update texture
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
            Item item = new Item();
            UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + getSlotID(row)).getBytes(
                            Charsets.UTF_8));
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setDisplayName(ComponentSerializer.toString(
                    TextComponent.
                            fromLegacyText(text)));

            item.setUsername(getSlotID(row));
            item.setGamemode(0);
            if (textures != null) {
                item.setProperties(new String[][]{{"textures", textures[0],
                        textures[1]
                }});
                sendTextures[row] = item.getProperties()[0][1];
            } else {
                item.setProperties(new String[0][0]);
                sendTextures[row] = null;

            }
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
            textureUpdate = true;
        }

        // update ping
        if (ping != slots_ping[row]) {
            slots_ping[row] = ping;
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
            Item item = new Item();
            UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setUsername(getSlotID(row));
            item.setProperties(new String[0][0]);
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
        }

        // update name
        String old = send[row];
        if (old == null || !old.equals(text) || textureUpdate) {
            send[row] = text;
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
            Item item = new Item();
            UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setDisplayName(ComponentSerializer.toString(TextComponent.
                    fromLegacyText(text)));

            item.setUsername(getSlotID(row));
            item.setGamemode(0);
            item.setProperties(new String[0][0]);
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);

        }
    }

    private void createSlot(int row) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setDisplayName(" ");
        item.setGamemode(0);
        item.setPing(0);
        item.setUsername(getSlotID(row));
        item.setProperties(new String[0][0]);
        pli.setItems(new Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
        send[row] = null;
        slots_ping[row] = 0;
        sendTextures[row] = null;
    }

    @Override
    public void unload() {
        resize(0);
    }
}