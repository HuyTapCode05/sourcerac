package npc.npc_list;

/**
 *
 * @author YourSoulMatee
 */
import consts.ConstNpc;
import consts.ConstTranhNgocNamek;
import item.Item;
import models.DragonNamecWar.TranhNgoc;
import npc.Npc;
import player.NPoint;
import player.Player;
import services.InventoryService;
import services.OpenPowerService;
import services.Service;
import services.func.TopService;
import shop.ShopService;
import utils.Util;

public class QuocVuong extends Npc {

    public QuocVuong(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        super(mapId, status, cx, cy, tempId, avartar);
    }

    @Override
    public void openBaseMenu(Player player) {
        this.createOtherMenu(player, ConstNpc.BASE_MENU,
                "Con muốn nâng giới hạn sức mạnh cho bản thân hay đệ tử?",
                "Bản thân", "Đệ tử", "Từ chối");
    }

    @Override
    public void confirmMenu(Player player, int select) {
        if (canOpenNpc(player)) {
            if (player.iDMark.isBaseMenu()) {
                switch (select) {

                    case 0 -> {
                        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
                            if (player.nPoint.limitPower == 4) {
                                Item dothan = player.inventory.itemsBody.stream()
                                        .filter(it -> it != null && it.template != null && it.template.level == 13)
                                        .findFirst()
                                        .orElse(null);

                                if (dothan == null) {
                                    this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                            "Con cần mặc ít nhất 1 món đồ thần linh để mở giới hạn sức mạnh!",
                                            "Đóng");
                                    return; // Ngừng thực thi, không tiếp tục tạo menu.
                                }
                            }
                            // Tạo menu nâng cấp khi không phải mốc 4 hoặc đã thỏa điều kiện đồ thần.
                            this.createOtherMenu(player, ConstNpc.OPEN_POWER_MYSEFT,
                                    "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của bản thân lên "
                                    + Util.numberToMoney(player.nPoint.getPowerNextLimit()),
                                    "Nâng\ngiới hạn\nsức mạnh",
                                    "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " vàng",
                                    "Đóng");
                        } else {
                            this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Sức mạnh của con đã đạt tới giới hạn",
                                    "Đóng");
                        }
                    }

                    case 1 -> {
                        if (player.pet != null) {
                            if (player.pet.nPoint.limitPower < NPoint.MAX_LIMIT) {
                                this.createOtherMenu(player, ConstNpc.OPEN_POWER_PET,
                                        "Ta sẽ truyền năng lượng giúp con mở giới hạn sức mạnh của đệ tử lên "
                                        + Util.numberToMoney(player.pet.nPoint.getPowerNextLimit()),
                                        "Nâng ngay\n" + Util.numberToMoney(OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) + " ngọc", "Đóng");
                            } else {
                                this.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        "Sức mạnh của đệ con đã đạt tới giới hạn",
                                        "Đóng");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Không thể thực hiện");
                        }

                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.OPEN_POWER_MYSEFT) {
                switch (select) {
                    case 0 ->
                        OpenPowerService.gI().openPowerBasic(player);
                    case 1 -> {
                        if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                            if (OpenPowerService.gI().openPowerSpeed(player)) {
                                player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                                Service.gI().sendMoney(player);
                            }
                        } else {
                            Service.gI().sendThongBao(player,
                                    "Bạn không đủ ngọc để mở, còn thiếu "
                                    + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)) + " ngọc");
                        }
                    }
                }
            } else if (player.iDMark.getIndexMenu() == ConstNpc.OPEN_POWER_PET) {
                if (select == 0) {
                    if (player.inventory.gem >= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER) {
                        if (OpenPowerService.gI().openPowerSpeed(player.pet)) {
                            player.inventory.gem -= OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER;
                            Service.gI().sendMoney(player);
                        }
                    } else {
                        Service.gI().sendThongBao(player,
                                "Bạn không đủ ngọc để mở, còn thiếu "
                                + Util.numberToMoney((OpenPowerService.COST_SPEED_OPEN_LIMIT_POWER - player.inventory.gem)) + " ngọc");
                    }
                }
            }
        }
    }
}
