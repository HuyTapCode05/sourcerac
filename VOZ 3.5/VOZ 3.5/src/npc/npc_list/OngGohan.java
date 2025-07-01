package npc.npc_list;

/**
 * @author YourSoulMatee
 */
import Traoqua.Mocnap;
import consts.ConstNpc;
import consts.ConstTask;
import consts.ConstTaskBadges;
import item.Item;

import java.util.ArrayList;
import java.util.List;

import jdbc.daos.PlayerDAO;
import npc.Npc;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.TaskService;
import services.func.Input;
import shop.ShopService;
import task.Badges.BadgesTaskService;
import utils.Util;

public class OngGohan extends Npc {

    public OngGohan(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    int costNapVang = 1;

    int[][] napVang = {{20000, 20}, {50000, 55}, {100000, 125}, {500000, 1250}, {1000000, 2500}, {2000000, 5000}, {5000000, 15000}};

    @Override
    public void openBaseMenu(Player player) {
    
        if (canOpenNpc(player)) {
            if (!TaskService.gI().checkDoneTaskTalkNpc(player, this)) {
                this.createOtherMenu(player, ConstNpc.BASE_MENU,
                        "\nXin chào ta có thể giúp gì cho con!!!"
                        + "\nServer giống gốc cày là có lên web để mở thành viên."
                        +"\n Ở Trong nhà Thời gian online ko đc tính",
                        "GiftCode",
                        "Nạp tiền",
                        "Hộp thư",
                        "Quà Online",
                        "Mốc Nạp",
                        "Từ chối");
            }
        }
    }

    @Override
    public void confirmMenu(Player player, int select) {
            long totalSeconds = player.TimeOnline; // Thời gian online tính bằng giây
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {
                    case 0: // mã quà tặng
                        Input.gI().createFormGiftCode(player);
                        break;
                    case 1: // nạp tiền
                        String npcSay = "Số dư của con là: " + Util.mumberToLouis(player.getSession().cash) + " VND dùng để nạp qua đơn vị khác\n"
                                + "Ta đang giữ giúp con " + Util.mumberToLouis(player.getSession().goldBar) + " thỏi vàng";
                        createOtherMenu(player, ConstNpc.NAP_TIEN, npcSay,
                                "Nạp VNĐ",
                                "Nạp vàng",
                                "Nhận\nThỏi vàng",
                                "Đóng");
                        break;
                    case 29:// xóa đệ
                        this.createOtherMenu(player, 12456,
                                "|0|Bạn muốn xóa đệ với giá 10K VND ?",
                                "Đồng ý", "Không");
                        break;

                    case 39: // nhận ngọc
                        if (player.inventory.gem >= 5000) {
                            Service.gI().sendThongBao(player, "Tiêu hết đi đã r nhận cu");
                            return;
                        }
                        int soLuongNgoc = 5_000;
                        player.inventory.gem += soLuongNgoc;
                        Service.gI().sendMoney(player);
                        Service.gI().sendThongBao(player, "Bạn vừa nhận " + Util.mumberToLouis(soLuongNgoc) + " ngọc xanh");
                        break;
                    case 49: // nhận đệ
                        if (!player.getSession().actived) {
                            Service.gI().sendThongBao(player, "Vui lòng mở thành viên trước");
                            return;
                        }
                        if (player.pet == null) {
                            PetService.gI().createNormalPet(player);
                            Service.gI().sendThongBao(player, "Bạn vừa nhận được đệ tử");
                        } else {
                            this.npcChat(player, "Bú ít thôi con, giấu số đá còn lại ở đâu r ");
                        }

                        break;
                    case 59:
                        if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_9_0 && TaskService.gI().getIdTask(player) < ConstTask.TASK_11_0) {
                            player.playerTask.taskMain.id = 10;
                            player.playerTask.taskMain.index = 0;
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công");
                        } else if (TaskService.gI().getIdTask(player) >= ConstTask.TASK_18_0 && TaskService.gI().getIdTask(player) < ConstTask.TASK_20_0) {
                            player.playerTask.taskMain.id = 19;
                            player.playerTask.taskMain.index = 0;
                            TaskService.gI().sendNextTaskMain(player);
                            Service.gI().sendThongBao(player, "Bạn đã được hỗ trợ nhiệm vụ thành công");
                        } else {
                            Service.gI().sendThongBao(player, "Chỉ hỗ trợ nhiệm vụ tàu pảy pảy và nhiệm vụ DHVT, Trung úy trắng");
                        }
                        break;
                    case 2:
                        this.createOtherMenu(player, ConstNpc.MAIL_BOX,
                                "|0|Ta đang giữ hòm thư của con.\n"
                                + "Hãy xem và xử lý các món đồ bên trong.",
                                "Hòm Thư\n(" + (player.inventory.itemsMailBox.size()
                                - InventoryService.gI().getCountEmptyListItem(player.inventory.itemsMailBox))
                                + " món)",
                                "Xóa Hết\nHòm Thư", "Đóng");
                        break;
                    case 3:
                        this.createOtherMenu(player, ConstNpc.QuaOnline,
                                String.format("|7|Thời Gian Online: %d Giờ %d Phút %d Giây\n", hours, minutes, seconds)
                                + "\n⚡ **15 Phút**: x2 mỗi item cấp 1, 1 Thỏi Vàng"
                                + "\n⚡ **30**: x3 mỗi item cấp 1, 2 Thỏi Vàng, 5 Hồng Ngọc"
                                + "\n⚡ **1 Giờ**: x5 mỗi item cấp 1, 3 Thỏi Vàng, 10 Hồng Ngọc"
                                + "\n⚡ **2 Giờ**: x2 mỗi item cấp 2, 4 Thỏi Vàng, 15 Hồng Ngọc, x1 Viên NRO (7s → 4s)"
                                + "\n⚡ **3 Giờ**: x3 mỗi item cấp 2, 5 Thỏi Vàng, 20 Hồng Ngọc, x1 Viên NRO (7s → 4s), x1 Thẻ Gia Hạn"
                                + "\n⚡ **4 Giờ**: x5 mỗi item cấp 2, 6 Thỏi Vàng, 30 Hồng Ngọc, x2 Viên NRO (7s → 4s), x2 Thẻ Gia Hạn"
                                + "\n⚡ **5 Giờ**: x5 mỗi item cấp 2, 10 Thỏi Vàng, 50 Hồng Ngọc, x3 Viên NRO (7s → 4s), x1 Đổi Skill 4 Đệ Tử",
                                "Nhận", "Đóng"
                        );
                        break;

                    case 4:
                        Mocnap.gI().getAchievement(player);
                        break;

                }
            }    if (player.iDMark.getIndexMenu() == ConstNpc.QuaOnline) {
                        switch (select) {
                            case 0:
                                if (player.timereset == 0) {
                                    player.timereset = System.currentTimeMillis();
                                }

                                Object[][] rewardData = {
                                    {0, 15, new int[]{381, 382, 383, 384, 385}, 2, null}, // 15 phút
                                    {0, 30, new int[]{381, 382, 383, 384, 385}, 3, new int[]{457, 1}}, // 30 phút
                                    {1, 0, new int[]{381, 382, 383, 384, 385}, 5, new int[]{457, 1, 861, 5}}, // 45 phút
                                    {2, 0, new int[]{1099, 1100, 1101, 1102, 1103}, 2, new int[]{457, 3, 861, 10}, new int[]{ 15, 16, 17, 18, 19, 20}}, // 1 tiếng
                                    {3, 0, new int[]{1099, 1100, 1101, 1102, 1103}, 3, new int[]{457, 4, 861, 15, 1346, 1}, new int[]{15, 16, 17, 18, 19, 20}}, // 1 tiếng 30 phút
                                    {4, 0, new int[]{1099, 1100, 1101, 1102, 1103}, 5, new int[]{457, 4, 861, 20, 1346, 3}, new int[]{15, 16, 17, 18, 19, 20}}, // 2 tiếng
                                    {10, 0, new int[]{1099, 1100, 1101, 1102, 1103}, 5, new int[]{457, 6, 861, 20, 1241, 1}, new int[]{ 15, 16, 17, 18, 19, 20}} // 3 tiếng
                                };

                                // Kiểm tra và nhận quà theo từng mốc
                                for (int i = 0; i < rewardData.length; i++) {
                                    int reqHours = (int) rewardData[i][0];
                                    int reqMinutes = (int) rewardData[i][1];

                                    if ((hours >= reqHours && minutes >= reqMinutes) && player.mocquaonline == i) {
                                        int[] baseItems = (int[]) rewardData[i][2];
                                        int baseAmount = (int) rewardData[i][3];
                                        int[] extraItems = (int[]) rewardData[i][4];
                                        int[] bonusItems = (int[]) (rewardData[i].length > 5 ? rewardData[i][5] : null);

                                        // Kiểm tra chỗ trống trong hành trang
                                        int requiredSlots = baseItems.length + (extraItems != null ? extraItems.length / 2 : 0);
                                        if (InventoryService.gI().getCountEmptyBag(player) < requiredSlots) {
                                            this.npcChat(player, "Hành trang của bạn không đủ chỗ trống để nhận quà mốc " + reqHours + " tiếng " + reqMinutes + " phút.");
                                            return;
                                        }

                                        // Nhận quà
                                        this.receiveReward(player, i + 1, "Bạn đã nhận quà mốc " + reqHours + " tiếng " + reqMinutes + " phút.", baseItems, baseAmount, extraItems);

                                        // Nhận item bonus (nếu có)
                                        if (bonusItems != null) {
                                            ItemService.gI().addItemsToBag(player, bonusItems, 1);
                                        }
                                        return;
                                    }
                                }

                                // Nếu không thuộc mốc nào
                                if (player.mocquaonline >= rewardData.length) {
                                    this.npcChat(player, "Bạn đã nhận hết các mốc quà.");
                                } else {
                                    this.npcChat(player, "Bạn chưa đủ thời gian để nhận quà mốc tiếp theo.");
                                }
                                break;
                        }
                    }
            else if (player.iDMark.getIndexMenu() == ConstNpc.MAIL_BOX) {
                switch (select) {
                    case 0:
//                                player.inventory.itemsMailBox.clear();
//                                player.inventory.itemsMailBox.addAll(GodGK.getMailBox(player));
                        ShopService.gI().opendShop(player, "ITEMS_MAIL_BOX", true);
                        break;
                    case 1:
                        NpcService.gI().createMenuConMeo(player,
                                ConstNpc.CONFIRM_REMOVE_ALL_ITEM_MAIL_BOX, this.avartar,
                                "|3|Bạn chắc muốn xóa hết vật phẩm trong hòm thư?\n"
                                + "|7|Sau khi xóa sẽ không thể khôi phục!",
                                "Đồng ý", "Hủy bỏ");
                        break;
                    case 2:
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_TIEN) {
                switch (select) {
                    case 0:
                        NpcService.gI().createBigMessage(player, avartar, "Nhớ đăng nhập xong sau đó bấm NẠP!!!", (byte) 1, "NẠP", "https://dragon7super.io.vn");
                        break;
                    case 1:
                        List<String> menu = new ArrayList<>();
                        for (int i = 0; i < napVang.length; i++) {
                            menu.add(i, Util.mumberToLouis(napVang[i][0]) + "\n" + Util.mumberToLouis(napVang[i][1] * costNapVang) + " Thỏi\nvàng");
                        }
                        String[] menus = menu.toArray(new String[0]);
                        createOtherMenu(player, ConstNpc.NAP_VANG, "Ta sẽ giữ giúp con\n"
                                + "Nếu con cần dùng tới hãy quay lại đây gặp ta!", menus);
                        break;
                    case 2:
                        if (player.getSession().goldBar > 0) {
                            List<Item> listItem = new ArrayList<>();
                            Item thoiVang = ItemService.gI().createNewItem((short) 457, player.getSession().goldBar);
                            listItem.add(thoiVang);
                            if (InventoryService.gI().getCountEmptyBag(player) < listItem.size()) {
                                Service.gI().sendThongBao(player, "Cần ít nhất " + listItem.size() + " ô trống trong hành trang");
                            }
                            for (Item it : listItem) {
                                InventoryService.gI().addItemBag(player, it);
                                InventoryService.gI().sendItemBag(player);
                            }
                            Service.gI().sendThongBao(player, "Bạn đã nhận được " + player.getSession().goldBar + " thỏi vàng");
                            PlayerDAO.subGoldBar(player, -(napVang[select][1] * costNapVang));
                            PlayerDAO.subGoldBar(player, player.getSession().goldBar);
                        }
                        break;
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.NAP_VANG) {
                if (player.getSession().cash >= napVang[select][0]) {
                    List<Item> listItem = new ArrayList<>();
                    if (InventoryService.gI().getCountEmptyBag(player) < listItem.size()) {
                        Service.gI().sendThongBao(player, "Cần ít nhất " + listItem.size() + " ô trống trong hành trang");
                    }
                    for (Item it : listItem) {
                        InventoryService.gI().addItemBag(player, it);
                        InventoryService.gI().sendItemBag(player);
                    }
                    PlayerDAO.subcash(player, napVang[select][0]);
                    BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.DAI_GIA_MOI_NHU, napVang[select][0]);
                    PlayerDAO.subGoldBar(player, -(napVang[select][1] * costNapVang));
                    Service.gI().sendThongBao(player, "Bạn có thêm " + Util.mumberToLouis(napVang[select][1] * costNapVang) + " thỏi vàng");
                } else {
                    Service.gI().sendThongBao(player, "Không đủ số dư");
                }
            } else if (player.iDMark.getIndexMenu() == 12456) {
                switch (select) {
                    case 0:
                        if (player.getSession().cash < 10000) {
                            Service.gI().sendThongBao(player, "Xóa đệ cần 10K VND");
                            return;
                        }
                        if (player.pet != null) {
//                            PetService.gI().deletePet(player);
                        }
                        break;
                    case 1:
                        break;

                }
            }
        }
    }
}
