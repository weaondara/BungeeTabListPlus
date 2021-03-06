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
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.packets.TabHeaderPacket;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

import java.text.Collator;
import java.util.*;
import java.util.logging.Level;

public class TabList18v3 extends CustomTabList18 implements PlayerTablistHandler {

    private static String getSlotID(int n) {
        return getSlotPrefix(n) + " ?tab";
    }

    private static String getSlotPrefix(int n) {
        return " §" + (char) (970 + n);
    }

    private final Map<UUID, Integer> sendPing = new HashMap<>();

    private int sendSlots = 0;

    private final Map<UUID, String> send = new HashMap<>();

    private final Map<UUID, String> sendTextures = new HashMap<>();

    final Map<UUID, String> sendUsernames = new HashMap<>();

    public TabList18v3(ProxiedPlayer player) {
        super(player);
    }

    @Override
    public void sendTablist(ITabList tabList) {
        synchronized (super.usernames) {
            int numFakePlayers = 80;

            if (tabList.getRows() * tabList.getColumns() < 80) {
                numFakePlayers = tabList.getColumns() * tabList.getRows() - super.uuids.size();
                if (numFakePlayers < 0) {
                    BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Could not update tablist for {0}. Please increase tab_size", getPlayer().getName());
                }
            }

            resize(numFakePlayers);

            int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().charLimit;

            // create uuidList
            List<UUID> list = new ArrayList<>(super.uuids.keySet());
            Collections.sort(list, new Comparator<UUID>() {

                @Override
                public int compare(UUID t, UUID t1) {
                    String name1 = uuids.get(t).getUsername();
                    String name2 = uuids.get(t1).getUsername();
                    return Collator.getInstance().compare(name1, name2);
                }
            });

            List<UUID> fakeUUIDs = new ArrayList<>();
            for (int i = 0; i < numFakePlayers; i++) {
                fakeUUIDs.add(java.util.UUID.nameUUIDFromBytes(
                        ("OfflinePlayer:" + getSlotID(i)).getBytes(Charsets.UTF_8)));
            }

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

                UUID uuid = null;
                boolean reorder = true;
                if (line.getSkin().getOwner() != null && list.contains(line.getSkin().getOwner()) && super.uuids.get(line.getSkin().getOwner()).getUsername().length() <= 13
                        && (((UserConnection) getPlayer()).getGamemode() != 3 || !Objects.equals(line.getSkin().getOwner(), getPlayer().getUniqueId()))) {
                    uuid = line.getSkin().getOwner();
                    list.remove(uuid);
                }
                if (uuid == null && !fakeUUIDs.isEmpty()) {
                    uuid = fakeUUIDs.get(0);
                    fakeUUIDs.remove(uuid);
                }
                if (uuid == null) {
                    for (int j = 0; j < list.size(); j++) {
                        uuid = list.get(0);
                        if (!(Objects.equals(uuid, getPlayer().getUniqueId()) && ((UserConnection) getPlayer()).getGamemode() == 3)) {
                            list.remove(uuid);
                            reorder = false;
                            break;
                        }
                    }
                }
                if (uuid == null) {
                    uuid = list.get(0);
                    list.remove(uuid);
                    reorder = false;
                }

                updateSlot(uuid, i, text, line.ping, line.getSkin(), reorder);
            }

            for (UUID offlineId : list) {
                String newName = super.uuids.get(offlineId).getUsername();
                if (sendUsernames.containsKey(offlineId) && !sendUsernames.get(offlineId).equals(newName)) {
                    PlayerListItem pli = new PlayerListItem();
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    pli.setItems(new Item[]{super.uuids.get(offlineId)});
                    getPlayer().unsafe().sendPacket(pli);
                    sendUsernames.put(offlineId, newName);
                }
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
        send.remove(offlineId);
        sendTextures.remove(offlineId);
        sendPing.remove(offlineId);
        sendUsernames.remove(offlineId);
    }

    private void updateSlot(UUID offlineId, int row, String text, int ping, Skin skin, boolean reorder) {
        boolean textureUpdate = false;
        String[] textures = skin.toProperty();
        if (textures != null) {
            textures = new String[]{textures[1], textures[2]};
        }
        // order
        if (reorder) {
            if (super.uuids.containsKey(offlineId)) {
                String newName = getSlotPrefix(row) + super.uuids.get(offlineId).getUsername();
                if (!sendUsernames.containsKey(offlineId) || !sendUsernames.get(offlineId).equals(newName)) {
                    PlayerListItem pli = new PlayerListItem();
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    Item item = new Item();
                    item.setUuid(offlineId);
                    item.setPing(ping);
                    item.setDisplayName(ComponentSerializer.toString(
                            TextComponent.
                                    fromLegacyText(text)));

                    item.setUsername(newName);
                    item.setGamemode(super.uuids.get(offlineId).getGamemode());
                    item.setProperties(super.uuids.get(offlineId).getProperties());
                    pli.setItems(new Item[]{item});
                    getPlayer().unsafe().sendPacket(pli);
                    textureUpdate = true;
                    sendUsernames.put(offlineId, newName);
                }
            } else {
                String newName = getSlotID(row);
                if (!sendUsernames.containsKey(offlineId) || !sendUsernames.get(offlineId).equals(newName)) {
                    PlayerListItem pli = new PlayerListItem();
                    pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                    Item item = new Item();
                    item.setUuid(offlineId);
                    item.setPing(ping);
                    item.setDisplayName(ComponentSerializer.toString(
                            TextComponent.
                                    fromLegacyText(text)));

                    item.setUsername(newName);
                    item.setGamemode(0);
                    if (textures != null) {
                        item.setProperties(new String[][]{{"textures", textures[0],
                                textures[1]
                        }});
                        sendTextures.put(offlineId, item.getProperties()[0][1]);
                    } else {
                        item.setProperties(new String[0][0]);
                        sendTextures.remove(offlineId);

                    }
                    pli.setItems(new Item[]{item});
                    getPlayer().unsafe().sendPacket(pli);
                    textureUpdate = true;
                    sendUsernames.put(offlineId, newName);
                }
            }
        } else {
            String newName = super.uuids.get(offlineId).getUsername();
            if (sendUsernames.containsKey(offlineId) && !sendUsernames.get(offlineId).equals(newName)) {
                PlayerListItem pli = new PlayerListItem();
                pli.setAction(PlayerListItem.Action.ADD_PLAYER);
                Item item = new Item();
                item.setUuid(offlineId);
                item.setPing(ping);
                item.setDisplayName(ComponentSerializer.toString(
                        TextComponent.
                                fromLegacyText(text)));

                item.setUsername(newName);
                item.setGamemode(super.uuids.get(offlineId).getGamemode());
                item.setProperties(super.uuids.get(offlineId).getProperties());
                pli.setItems(new Item[]{item});
                getPlayer().unsafe().sendPacket(pli);
                textureUpdate = true;
                sendUsernames.put(offlineId, newName);
            }
        }
        if (!sendUsernames.containsKey(offlineId) && super.uuids.containsKey(offlineId)) {
            sendUsernames.put(offlineId, super.uuids.get(offlineId).getUsername());
        }
        // textures
        if (!textureUpdate && !super.uuids.containsKey(offlineId) && ((sendTextures.get(offlineId) == null && textures != null) || (sendTextures.get(offlineId) != null && textures == null) || (textures != null && sendTextures.get(offlineId) != null && !textures[0].
                equals(
                        sendTextures.get(offlineId))))) {
            // update texture
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
            Item item = new Item();
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setDisplayName(ComponentSerializer.toString(
                    TextComponent.
                            fromLegacyText(text)));

            item.setUsername(sendUsernames.get(offlineId));
            item.setGamemode(0);
            if (textures != null) {
                item.setProperties(new String[][]{{"textures", textures[0],
                        textures[1]
                }});
                sendTextures.put(offlineId, item.getProperties()[0][1]);
            } else {
                item.setProperties(new String[0][0]);
                sendTextures.remove(offlineId);

            }
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
            textureUpdate = true;
        }

        // update ping
        if (sendPing.get(offlineId) == null) {
            sendPing.put(offlineId, 0);
        }
        if (ping != sendPing.get(offlineId)) {
            sendPing.put(offlineId, ping);
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
            Item item = new Item();
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setUsername(sendUsernames.get(offlineId));
            item.setProperties(new String[0][0]);
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
        }

        // update name
        String old = send.get(offlineId);
        if (textureUpdate || old == null || !old.equals(text) || super.uuids.containsKey(offlineId)) {
            send.put(offlineId, text);
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
            Item item = new Item();
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setDisplayName(ComponentSerializer.toString(TextComponent.
                    fromLegacyText(text)));

            item.setUsername(sendUsernames.get(offlineId));
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
        sendUsernames.put(offlineId, item.getUsername());
        sendPing.put(offlineId, 0);
    }

    @Override
    public void unload() {
        resize(0);
    }
}