package npc.npc_list;

import clan.Clan;
import consts.ConstNpc;
import item.Item;
import java.util.ArrayList;
import models.TreasureUnderSea.TreasureUnderSea;
import models.TreasureUnderSea.TreasureUnderSeaService;
import npc.Npc;
import static npc.NpcFactory.PLAYERID_OBJECT;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PlayerService;
import services.RewardService;
import services.Service;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import shop.ShopService;
import skill.Skill;
import utils.Logger;
import utils.SkillUtil;
import utils.TimeUtil;
import utils.Util;

public class QuyLaoKame extends Npc {

    public QuyLaoKame(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        Item ruacon = InventoryService.gI().findItemBag(player, 874);
        if (canOpenNpc(player)) {
            ArrayList<String> menu = new ArrayList<>();
            if (!player.canReward) {
                menu.add("Nói\nchuyện");
                if (ruacon != null && ruacon.quantity >= 1) {
                    menu.add("Giao\nRùa con");
                }
            } else {
                menu.add("Giao\nLân con");
            }
            String[] menus = menu.toArray(String[]::new);
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU, "Con muốn hỏi gì nào?", menus);
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.canReward) {
                RewardService.gI().rewardLancon(player);
                return;
            }
            switch (player.iDMark.getIndexMenu()) {
                case ConstNpc.BASE_MENU -> {
                    switch (select) {
                        case 0 -> {
                                if (player.LearnSkill.Time != -1 && player.LearnSkill.Time <= System.currentTimeMillis()) {

                                player.LearnSkill.Time = -1;
                                try {
                                    var curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId).point);
                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 62);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                } catch (Exception e) {
                                    Logger.log(e.toString());
                                }
                            }
                            ArrayList<String> menu = new ArrayList<>();
                            menu.add("Nhiệm vụ");
                            menu.add("Học\nKỹ năng");
                            Clan clan = player.clan;
                            if (clan != null) {
                                menu.add("Về khu\nvực bang");
                                if (clan.isLeader(player)) {
                                    menu.add("Giải tán\nBang hội");
                                }
                            }
                            menu.add("Kho báu\ndưới biển");
                            String[] menus = menu.toArray(String[]::new);

                            this.createOtherMenu(player, 0,
                                    "Chào con, ta rất vui khi gặp con\nCon muốn làm gì nào ?", menus);
                        }
                        case 2 -> {
                            Item ruacon = InventoryService.gI().findItemBag(player, 874);
                            if (ruacon != null && ruacon.quantity >= 1) {
                                this.createOtherMenu(player, 1,
                                        "Cảm ơn cậu đã cứu con rùa của ta\nĐể cảm ơn ta sẽ tặng cậu món quà.",
                                        "Nhận quà", "Đóng");
                                break;
                            }
                        }
                    }
                } 
                case 0 -> {
                    switch (select) {
                        case 0 ->
                            NpcService.gI().createTutorial(player, tempId, avartar, player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
                        case 1 -> {
                            if (player.LearnSkill.Time != -1) {
                                var ngoc = 5;
                                var time = player.LearnSkill.Time - System.currentTimeMillis();
                                if (time / 600_000 >= 2) {
                                    ngoc += time / 600_000;
                                }
                                String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                                byte level = Byte.parseByte(subName[subName.length - 1]);
                                this.createOtherMenu(player, 12,
                                        "Con đang học kỹ năng\n" + SkillUtil.findSkillTemplate(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId)).name + " cấp " + level + "\nThời gian còn lại " + TimeUtil.getTime(time),
                                        "Học\nCấp tốc\n" + ngoc + " ngọc", "Huỷ", "Bỏ qua");
                            } else {
                                ShopService.gI().opendShop(player, "QUY_LAO", false);
                            }
                        }
                        case 2 -> {
                            Clan clan = player.clan;
                            if (clan != null && select == 2) {
                                ChangeMapService.gI().changeMapNonSpaceship(player, 153, Util.nextInt(100, 200), 432);
                            } else {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 3 -> {
                            boolean clanCheck = true;
                            Clan clan = player.clan;
                            if (clan != null) {
                                clanCheck = false;
                                if (clan.isLeader(player)) {
                                    createOtherMenu(player, 3, "Con có chắc muốn giải tán bang hội không?", "Đồng ý", "Từ chối");
                                } else {
                                    clanCheck = true;
                                }
                            }
                            if (clanCheck) {
                                if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                            "Bang hội con đang ở hang kho báu cấp "
                                            + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                            "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                                } else {
                                    this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                            "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                            "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                                }
                            }
                        }
                        case 4 -> {
                            if (player.clan != null && player.clan.BanDoKhoBau != null) {
                                this.createOtherMenu(player, ConstNpc.MENU_OPENED_DBKB,
                                        "Bang hội con đang ở hang kho báu cấp "
                                        + player.clan.BanDoKhoBau.level + "\ncon có muốn đi cùng họ không?",
                                        "Top\nBang hội", "Thành tích\nBang", "Đồng ý", "Từ chối");
                            } else {
                                this.createOtherMenu(player, ConstNpc.MENU_OPEN_DBKB,
                                        "Đây là bản đồ kho báu hải tặc tí hon\nCác con cứ yên tâm lên đường\nỞ đây có ta lo\nNhớ chọn cấp độ vừa sức mình nhé",
                                        "Top\nBang hội", "Thành tích\nBang", "Chọn\ncấp độ", "Từ chối");
                            }
                        }
                    }
                }
                case 3 -> {
                    Clan clan = player.clan;
                    if (clan != null) {
                        if (clan.isLeader(player)) {
                            if (select == 0) {
                                Input.gI().createFormGiaiTanBangHoi(player);
                            }
                        }
                    }
                }
                case ConstNpc.MENU_OPENED_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                ChangeMapService.gI().goToDBKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
                case 12 -> {
                    switch (select) {
                        case 1:
                            this.createOtherMenu(player, 13,
                                    "Con có muốn huỷ học kỹ năng này và nhận lại 50% số tiềm năng không ?",
                                    "Ok", "Đóng");
                            break;
                        case 0:
                            var time = player.LearnSkill.Time - System.currentTimeMillis();
                            var ngoc = 5;
                            if (time / 600_000 >= 2) {
                                ngoc += time / 600_000;
                            }
                            if (player.inventory.gem < ngoc) {
                                Service.gI().sendThongBao(player, "Bạn không có đủ ngọc");
                                return;
                            }
                            player.inventory.subGem(ngoc);
                            player.LearnSkill.Time = -1;
                            try {
                                String[] subName = ItemService.gI().getTemplate(player.LearnSkill.ItemTemplateSkillId).name.split("");
                                byte level = Byte.parseByte(subName[subName.length - 1]);
                                Skill curSkill = SkillUtil.getSkillByItemID(player, player.LearnSkill.ItemTemplateSkillId);
                                if (curSkill.point == 0) {
                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 23);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                } else {
                                    curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(player.LearnSkill.ItemTemplateSkillId), level);

                                    player.BoughtSkill.add((int) player.LearnSkill.ItemTemplateSkillId);
                                    //System.out.println(curSkill.template.name + " - " + curSkill.point);
                                    SkillUtil.setSkill(player, curSkill);
                                    var msg = Service.gI().messageSubCommand((byte) 62);
                                    msg.writer().writeShort(curSkill.skillId);
                                    player.sendMessage(msg);
                                    msg.cleanup();
                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                }
                            } catch (Exception e) {
                                Logger.log(e.toString());
                            }
                            break;

                    }
                }
                case ConstNpc.MENU_OPEN_DBKB -> {
                    switch (select) {
                        case 2 -> {
                            if (player.clan == null) {
                                Service.gI().sendThongBao(player, "Hãy vào bang hội trước");
                                return;
                            }
                            if (player.isAdmin() || player.nPoint.power >= TreasureUnderSea.POWER_CAN_GO_TO_DBKB) {
                                Input.gI().createFormChooseLevelBDKB(player);
                            } else {
                                this.npcChat(player, "Yêu cầu sức mạnh lớn hơn "
                                        + Util.numberToMoney(TreasureUnderSea.POWER_CAN_GO_TO_DBKB));
                            }
                        }

                    }
                }
                case ConstNpc.MENU_ACCEPT_GO_TO_BDKB -> {
                    switch (select) {
                        case 0 ->
                            TreasureUnderSeaService.gI().openBanDoKhoBau(player, Byte.parseByte(String.valueOf(PLAYERID_OBJECT.get(player.id))));
                    }
                }
            }
        }
    }
  
}
