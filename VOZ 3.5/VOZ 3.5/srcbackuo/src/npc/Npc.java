package npc;

/*
 *
 *
 * @author YourSoulMatee
 */

import consts.ConstNpc;
import consts.event.ConstDataEventLunaNewYear;
import item.Item;
import map.Map;
import map.Zone;
import player.Player;
import server.Manager;
import network.Message;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import services.Service;
import utils.Logger;
import utils.Util;

public abstract class Npc implements IAtionNpc {

    public int mapId;
    public Map map;

    public int status;

    public int cx;

    public int cy;

    public int tempId;

    public int avartar;

    public BaseMenu baseMenu;

    public int indexChat;

    public int timeChat;

    public long lastChatTime;

    public Npc(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        this.map = MapService.gI().getMapById(mapId);
        this.mapId = mapId;
        this.status = status;
        this.cx = cx;
        this.cy = cy;
        this.tempId = tempId;
        this.avartar = avartar;
        Manager.NPCS.add(this);
    }

    public void initBaseMenu(String text) {
        text = text.substring(1);
        String[] data = text.split("\\|");
        baseMenu = new BaseMenu();
        baseMenu.npcId = tempId;
        baseMenu.npcSay = data[0].replaceAll("<>", "\n");
        baseMenu.menuSelect = new String[data.length - 1];
        for (int i = 0; i < baseMenu.menuSelect.length; i++) {
            baseMenu.menuSelect[i] = data[i + 1].replaceAll("<>", "\n");
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String... menuSelect) {
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   @Override
public void openBaseMenu(Player player) {
    if (canOpenNpc(player)) {
        // Kiểm tra nếu sự kiện Tết Nguyên Đán đang diễn ra
        if (ConstDataEventLunaNewYear.isRunningSK) {
            // Nếu người chơi chưa nhận quà
            if (!player.hasReceivedLunarGift) {
                try {
                    Message msg = new Message(32);
                    msg.writer().writeShort(tempId);
                    msg.writer().writeUTF("Ta có phần quà cho con");
                    msg.writer().writeByte(1);
                    msg.writer().writeUTF("Nhận");
                    player.sendMessage(msg);
                    msg.cleanup();
         
                    player.iDMark.setIndexMenu(ConstNpc.RECEIVE_GIFT_MENU);
                } catch (Exception e) {
                    Logger.logException(Npc.class, e);
                }
                return; // Dừng ở đây nếu người chơi chưa nhận quà
            }
        }

        player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
        try {
            if (baseMenu != null) {
                baseMenu.openMenu(player);
            } else {
                Message msg = new Message(32);
                msg.writer().writeShort(tempId);
                msg.writer().writeUTF("Ta có thể giúp gì cho ngươi ?");
                msg.writer().writeByte(1);
                msg.writer().writeUTF("Từ chối");
                player.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception e) {
            Logger.logException(Npc.class, e);
        }
    }
}


    public void npcChat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void npcChat(Zone zone, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            Service.gI().sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void npcChat(String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            for (Zone zone : map.zones) {
                Service.gI().sendMessAllPlayerInMap(zone, msg);
            }
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public boolean canOpenNpc(Player player) {
        if (this.tempId == ConstNpc.DAU_THAN) {
            if (player.zone.map.mapId == 21
                    || player.zone.map.mapId == 22
                    || player.zone.map.mapId == 23) {
                return true;
            } else {
                Service.gI().hideWaitDialog(player);
                Service.gI().sendThongBao(player, "Không thể thực hiện");
                return false;
            }
        }
        if (player.zone.map.mapId == this.mapId
                && (Util.getDistance(this.cx, this.cy, player.location.x, player.location.y) <= 60 || !MapService.gI().isMapBlackBallWar(mapId))) {
            player.iDMark.setNpcChose(this);
            return true;
        } else if (this.tempId == ConstNpc.LY_TIEU_NUONG) {
            return true;
        } else {
            Service.gI().hideWaitDialog(player);
            Service.gI().sendThongBao(player, "Không thể thực hiện khi đứng quá xa");
            return false;
        }
    }

}
