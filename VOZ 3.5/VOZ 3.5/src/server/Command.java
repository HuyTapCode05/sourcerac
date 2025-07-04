package server;

/**
 * @author YourSoulMatee
 */
import VOZ.SystemMetrics;
import boss.AnTromManager;
import boss.BossManager;
import boss.BrolyManager;
import boss.GasDestroyManager;
import boss.LunarNewYearEventManager;
import boss.OtherBossManager;
import boss.RedRibbonHQManager;
import boss.SnakeWayManager;
import boss.TreasureUnderSeaManager;
import boss.TrungThuEventManager;
import consts.ConstNpc;
import item.Item;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;

import minigame.LuckyNumber.LuckyNumber;
import models.GiftCode.GiftCodeManager;
import models.ShenronEvent.ShenronEvent;
import models.ShenronEvent.ShenronEventManager;
import network.SessionManager;
import player.Pet;
import player.Player;
import player.badges.BadgesData;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import services.NpcService;
import services.PetService;
import services.Service;
import services.SkillService;
import services.TaskService;
import services.func.ChangeMapService;
import services.func.Input;
import skill.Skill;
import utils.Util;

public class Command {

    private static Command instance;

    public static Command gI() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    public void chat(Player player, String text) {
        if (!check(player, text)) {
            Service.gI().chat(player, text);
        }
    }

    public boolean check(Player player, String text) {
        if (player.isAdmin()) {
            if (text.equals("code")) {
                GiftCodeManager.gI().checkInfomationGiftCode(player);
                return true;
            } else if (text.equals("mapboss")) {
                BossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapbroly")) {
                BrolyManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapantrom")) {
                AnTromManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapboss2")) {
                OtherBossManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapdt")) {
                RedRibbonHQManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapbdkb")) {
                TreasureUnderSeaManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapcdrd")) {
                SnakeWayManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("mapkghd")) {
                GasDestroyManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("maptrungthu")) {
                TrungThuEventManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("maptet")) {
                LunarNewYearEventManager.gI().showListBoss(player);
                return true;
            } else if (text.equals("hsk")) {
                Service.gI().releaseCooldownSkill(player);
                return true;
            } else if (text.startsWith("sucmanh1")) {
                try {
                    long power = 1_000_000_000L; // 1 tỷ sức mạnh
                    Service.gI().addSMTN(player, (byte) 2, power, false);
                    Service.gI().sendThongBao(player, "Bạn đã được cộng thêm 1 tỷ sức mạnh!");
                    return true;
                } catch (Exception e) {
                }
            } else if (text.equals("battu")) {
                if (player.isBattu) {
                    player.isBattu = false;
                } else {
                    player.isBattu = true;
                }
                Service.gI().sendThongBao(player, "Bất tử" + (player.isBattu ? ": ON" : ": OFF"));
                return true;
            } else if (text.equals("tanthu")) {
                try {
                    long power = 10_000_000L; // 10 triệu sức mạnh
                    player.nPoint.power = power; // Đặt sức mạnh trực tiếp
                    Service.gI().SendPowerInfo(player); // Cập nhật sức mạnh mới cho người chơi
                    Service.gI().sendThongBao(player, "Sức mạnh của bạn đã được đặt lại thành 10 triệu!");
                    return true;
                } catch (Exception e) {
                }

            } else if (text.startsWith("dt11")) {
                try {
                    long power = 1_000_000_000L; // 1 tỷ sức mạnh cho đệ tử
                    Service.gI().addSMTN(player.pet, (byte) 2, power, false);
                    Service.gI().sendThongBao(player, "Đệ tử của bạn đã được cộng thêm 1 tỷ sức mạnh!");
                    return true;
                } catch (Exception e) {
                }
            } else if (text.equals("test")) {
                switch (player.gender) {
                    case 0 ->
                        SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME, 1);
                    case 2 ->
                        SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG, 1);
                    default ->
                        SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA, 1);
                }
                return true;
            } else if (text.equals("test2")) {
                switch (player.gender) {
                    case 0 -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                    case 2 -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                    default -> {
                        SkillService.gI().learSkillSpecial(player, Skill.PHAN_THAN, 6);
                    }
                }
                return true;
            } else if (text.equals("dragon")) {
                ShenronEvent shenron = new ShenronEvent();
                shenron.setPlayer(player);
                ShenronEventManager.gI().add(shenron);
                player.shenronEvent = shenron;
                shenron.setZone(player.zone);
                shenron.activeShenron(true, ShenronEvent.DRAGON_EVENT);
                shenron.sendWhishesShenron();
                return true;
            } else if (text.equals("admin")) {
                NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1, "|0|Time start: " + ServerManager.timeStart + "\nClients: " + Client.gI().getPlayers().size() + " người chơi\n Sessions: " + SessionManager.gI().getNumSession() + "\nThreads: " + Thread.activeCount() + " luồng" + "\n" + SystemMetrics.ToString(),
                        "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Boss", "Call Broly", "Buff VND", "Buff\nhộp thư", "Đóng");
                return true;
            } else if (text.equals("daucatmoi")) {
                for (int i = 0; i < 10; i++) {
                    ServerNotify.gI().notify("BOSS Nro vừa xuất hiện tại nhà anh ấy");
                }
                return true;
            } else if (text.startsWith("m ")) {
                int mapId = Integer.parseInt(text.replace("m ", ""));
                ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
                return true;
            }
            if (text.startsWith("dmg")) {
                try {
                    long dameg = Integer.parseInt(text.replaceAll("dmg", ""));
                    player.nPoint.dameg = dameg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("hpg")) {
                try {
                    long hpg = Integer.parseInt(text.replaceAll("hpg", ""));
                    player.nPoint.hpg = hpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("mpg")) {
                try {
                    long mpg = Integer.parseInt(text.replaceAll("mpg", ""));
                    player.nPoint.mpg = mpg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("defg")) {
                try {
                    int defg = Integer.parseInt(text.replaceAll("defg", ""));
                    player.nPoint.defg = defg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("fw")) {
                network.server.VOZServer.firewall.clear();
                network.server.VOZServer.firewallDownDataGame.clear();
            }

            if (text.startsWith("crg")) {
                try {
                    int critg = Integer.parseInt(text.replaceAll("crg", ""));
                    player.nPoint.critg = critg;
                    Service.gI().point(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("ntask")) {
                try {
                    int idTask = Integer.parseInt(text.replaceAll("ntask", ""));
                    player.playerTask.taskMain.id = idTask - 1;
                    player.playerTask.taskMain.index = 0;
                    TaskService.gI().sendNextTaskMain(player);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (text.startsWith("badges_")) {
                int idBadges = Integer.parseInt(text.replaceAll("badges_", ""));
                player.badges.idBadges = idBadges;
            }
            if (text.startsWith("kq")) {
                Service.gI().sendThongBao(player, "Kết quả Lucky Round tiếp theo là: " + LuckyNumber.RESULT);
                return true;
            }
            if (text.startsWith("danhhieu_")) {
                int idGender = Integer.parseInt(text.replaceAll("danhhieu_", ""));
                BadgesData data = new BadgesData(player, idGender, 5);
                return true;
            }
            if (text.startsWith("gender_")) {
                byte idGender = Byte.parseByte(text.replaceAll("gender_", ""));
                player.gender = idGender;
                return true;
            }
            if (text.startsWith("toado")) {
                Service.gI().sendThongBaoOK(player, "x: " + player.location.x + " - y: " + player.location.y);
                return true;
            }

            if (text.startsWith("i ")) {
                int itemId = Integer.parseInt(text.replace("i ", ""));
                Item item = ItemService.gI().createNewItem((short) itemId);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop((short) itemId);
                if (!ops.isEmpty()) {
                    item.itemOptions = ops;
                }
                InventoryService.gI().addItemBag(player, item);
                InventoryService.gI().sendItemBag(player);

                String thongBao = "GET " + item.template.name + " [" + item.template.id + "] SUCCESS, " + player.name + "!";
                Service.gI().sendThongBao(player, thongBao);
                Util.FileManager.writeToFile("thongbao.txt", thongBao);
                return true;
            } else if (text.equals("item")) {
                Input.gI().createFormGiveItem(player);
                return true;
            } else if (text.equals("getitem")) {
                Input.gI().createFormGetItem(player);
                return true;
            }

        }

        if (text.startsWith(
                "ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        }

        if (text.startsWith(
                "fw")) {
            network.server.VOZServer.firewall.clear();
            network.server.VOZServer.firewallDownDataGame.clear();

            Service.gI().sendThongBao(player, "CLEAR FIREWALL SUCCESS !");
        }
        if (text.equals("ve nha")) {  
            int mapId = player.gender + 21; 
            ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);  
            return true;
        }
        if (player.pet
                != null) {
            switch (text) {
                case "di theo", "follow" ->
                    player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" ->
                    player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" ->
                    player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" ->
                    player.pet.changeStatus(Pet.GOHOME);
                case "bien hinh" ->
                    player.pet.transform();

            }
        }

        return false;
    }

}
