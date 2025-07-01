package npc;

/*
 *
 *
 * @author YourSoulMatee
 */

import consts.ConstNpc;
import consts.ConstTask;
import consts.event.ConstDataEventLunaNewYear;
import consts.event.ConstDataEventSM;
import player.Player;
import server.Manager;
import services.TaskService;
import java.util.ArrayList;
import java.util.List;

public class NpcManager {

    public static Npc getByIdAndMap(int id, int mapId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == id && npc.mapId == mapId) {
                return npc;
            }
        }
        return null;
    }

    public static Npc getNpc(byte tempId) {
        for (Npc npc : Manager.NPCS) {
            if (npc.tempId == tempId) {
                return npc;
            }
        }
        return null;
    }

    public static List<Npc> getNpcsByMapPlayer(Player player) {
    List<Npc> list = new ArrayList<>();
    if (player.zone != null) {
        for (Npc npc : player.zone.map.npcs) {

            if (npc.tempId == ConstNpc.QUA_TRUNG && player.mabuEgg == null && player.zone.map.mapId == (21 + player.gender)) {
                continue;
            } 
   
            else if (npc.tempId == ConstNpc.CALICK && TaskService.gI().getIdTask(player) < ConstTask.TASK_23_0) {
                continue;
            } 

            else if (npc.tempId == ConstNpc.QUOC_VUONG && player.nPoint.power < 17000000000L) {
                continue;
            } 
            
            else if (npc.tempId == ConstNpc.BA_HAT_MIT && player.zone.map.mapId == 5 && ConstDataEventSM.isRunningSK) {
                continue;
            }
            list.add(npc);
        }
    }
    return list;
}
}
