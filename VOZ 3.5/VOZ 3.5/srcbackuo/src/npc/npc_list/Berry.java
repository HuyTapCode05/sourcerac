package npc.npc_list;

import consts.ConstNpc;
import consts.ConstTask;
import item.Item;
import npc.Npc;
import player.Player;
import services.*;
import shop.ShopService;
import utils.Util;

public class Berry extends Npc {

    public Berry(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            if (TaskService.gI().getIdTask(player) == ConstTask.TASK_31_5) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Bạn sẽ mang tôi về Bardock thật sao", "OK");
            } else {
                super.openBaseMenu(player); // Gọi phương thức của lớp cha nếu không phải TASK_31_5
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {

            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0 ->
                        TaskService.gI().checkDoneTask31(player);
                    default -> {
                    }
                }
            }
//            }
        }
    }
}
