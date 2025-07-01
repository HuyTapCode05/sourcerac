/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models.Combine.manifest;

import consts.ConstFont;
import consts.ConstNpc;
import item.Item;
import java.util.Random;
import models.Combine.CombineService;
import player.Player;
import server.Manager;
import services.InventoryService;
import services.ItemService;
import services.RewardService;
import services.Service;
import utils.Util;

/**
 *
 * @author Administrator
 */
public class CheTaoSKH {

    public static boolean Isphoitl(Item item) {
        if (item.template.id >= 555 && item.template.id <= 567) {
            return true;
        }
        return false;
    }

    public static void showInfoCombine(Player player) {
        if (player.combine != null && player.combine.itemsCombine != null && player.combine.itemsCombine.size() == 2) {
            Item phoitl = null;
            Item linhon = null;
            for (Item item : player.combine.itemsCombine) {
                if (Isphoitl(item)) {
                    phoitl = item;
                } else if (item.template.id >= 1786 && item.template.id <= 1788) {
                    linhon = item;
                }
            }
            player.combine.goldCombine = 500_000_000;
            int goldCombie = player.combine.goldCombine;
            if (phoitl != null && linhon != null && linhon.quantity >= 1000) {
                String npcSay = "Sau khi cường hoá,"
                        + " sẽ được nâng cấp"
                        + "Và nhận được " + player.combine.itemsCombine.stream().filter(Item::isDTL).findFirst().get().typeName() + " kích hoạt Thường tương ứng\n";
                CombineService.gI().baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                        "Cường hoá\n" + Util.numberToMoney(goldCombie) + " vàng", "Từ chối");
            } else {
                Service.gI().sendThongBaoOK(player, "Cần 1 trang bị Thần Linh và 1000 linh hồn");
            }
        } else {
            Service.gI().sendThongBaoOK(player, "Cần 1 trang bị Thần Linh và 1000 linh hồn");
        }
    }

    public static void startCombine(Player player) {
        if (player.combine.itemsCombine.size() == 2) {
            int gold = player.combine.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Bạn không đủ vàng, còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng nữa");
                Service.gI().sendMoney(player);
                return;
            }
            Item phoitl = null;
            Item linhon = null;
            for (Item item : player.combine.itemsCombine) {
                if (Isphoitl(item)) {
                    phoitl = item;
                } else if (item.template.id >= 1786 && item.template.id <= 1788) {
                    linhon = item;
                }
            }
            int gender = linhon.template.type;
            int linhHonType = linhon.template.id; // Lấy loại Linh Hồn
            int[] maleOptions = {129, 141, 127, 139, 128, 140};
            int[] femaleOptions = {132, 144, 131, 143, 130, 142};
            int[] otherOptions = {135, 138, 133, 136, 134, 137};
            int[] selectedOptions;

            int randomRow = phoitl.template.type;

// Chọn danh sách shop theo loại linh hồn
            short[] selectedRow;
            if (linhHonType == 1786) { //  Trái Đất
                selectedRow = Manager.doshoptd[randomRow];
                  selectedOptions = maleOptions;
            } else if (linhHonType == 1787) { //  Namek
                selectedRow = Manager.doshopnm[randomRow];
                 selectedOptions = femaleOptions;
            } else { //  Xayda
                selectedRow = Manager.doshopxd[randomRow];
                selectedOptions = otherOptions;
            }

            short randomItem = selectedRow[Util.nextInt(selectedRow.length)];



            Item newItem = ItemService.gI().createNewItem(randomItem);

            RewardService.gI().initBaseOptionClothes(newItem.template.id, newItem.template.type, newItem.itemOptions);

            if (Util.isTrue(15, 100)) {
                newItem.itemOptions.add(new Item.ItemOption(selectedOptions[0], 0));
                newItem.itemOptions.add(new Item.ItemOption(selectedOptions[1], 0));
            } else {
                if (Util.isTrue(75, 100)) {
                    newItem.itemOptions.add(new Item.ItemOption(selectedOptions[2], 0));
                    newItem.itemOptions.add(new Item.ItemOption(selectedOptions[3], 0));
                } else {
                    newItem.itemOptions.add(new Item.ItemOption(selectedOptions[4], 0));
                    newItem.itemOptions.add(new Item.ItemOption(selectedOptions[5], 0));
                }
            }

            InventoryService.gI().addItemBag(player, newItem);
            InventoryService.gI().subQuantityItemsBag(player, phoitl, 1);
            InventoryService.gI().subQuantityItemsBag(player, linhon, 1000);
            CombineService.gI().sendEffectSuccessCombine(player);
            InventoryService.gI().sendItemBag(player);
            Service.gI().sendMoney(player);
            CombineService.gI().reOpenItemCombine(player);
        }
    }
}
