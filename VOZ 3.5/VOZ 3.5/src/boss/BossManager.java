package boss;

/*
 *
 *
 * @author YourSoulMatee
 */
import VOZ.Functions;
import static boss.BossID.SOI_HEC_QUYN1;
import boss.list.AnTrom.AnTrom;
import boss.list.AnTrom.AnTromTV;
import boss.list.Black.BlackGoku;
import boss.list.Nappa.Rambo;
import boss.list.Nappa.MapDauDinh;
import boss.list.Nappa.Kuku;
import boss.list.Android.Android19;
import boss.list.Android.Pic;
import boss.list.Android.Android14;
import boss.list.Android.Poc;
import boss.list.Android.Android13;
import boss.list.Android.KingKong;
import boss.list.Android.DrKore;
import boss.list.Android.Android15;
import boss.list.GoldenFrieza.DeathBeam1;
import boss.list.GoldenFrieza.DeathBeam2;
import boss.list.GoldenFrieza.DeathBeam3;
import boss.list.GoldenFrieza.DeathBeam4;
import boss.list.GoldenFrieza.DeathBeam5;
import boss.list.GoldenFrieza.GoldenFrieza;
import boss.list.Cooler.Cooler;
import boss.list.Cell.SieuBoHung;
import boss.list.Cell.XenBoHung;
import boss.list.Broly.Broly;
import boss.list.Broly.SuperBroly;
import boss.list.ChristmasEvent.OngGiaNoel;
import boss.list.TaoPaiPai.TaoPaiPai;
import boss.list.Frieza.Fide;
import boss.list.HungVuongEvent.SonTinh;
import boss.list.HungVuongEvent.ThuyTinh;
import boss.list.HalloweenEvent.BiMa;
import boss.list.HalloweenEvent.Doi;
import boss.list.HalloweenEvent.MaTroi;
import boss.list.TrungThuEvent.KhiDot;
import boss.list.TrungThuEvent.NguyetThan;
import boss.list.TrungThuEvent.NhatThan;
import boss.list.MajinBuu12H.Mabu;
import boss.list.MajinBuu12H.BuiBui;
import boss.list.MajinBuu12H.BuiBui2;
import boss.list.MajinBuu12H.Cadic;
import boss.list.MajinBuu12H.Drabura;
import boss.list.MajinBuu12H.Drabura2;
import boss.list.MajinBuu12H.Drabura3;
import boss.list.MajinBuu12H.Goku;
import boss.list.MajinBuu12H.Yacon;
import boss.list.MajinBuu14H.Mabu2H;
import boss.list.MajinBuu14H.SuperBu;
import boss.list.GinyuForce.SO1;
import boss.list.GinyuForce.SO2;
import boss.list.GinyuForce.SO3;
import boss.list.GinyuForce.SO4;
import boss.list.GinyuForce.TDT;
import boss.list.NamekGinyuForce.SO1_NM;
import boss.list.NamekGinyuForce.SO2_NM;
import boss.list.NamekGinyuForce.SO3_NM;
import boss.list.NamekGinyuForce.SO4_NM;
import boss.list.NamekGinyuForce.TDT_NM;
import boss.list.Earth.BIDO;
import boss.list.Earth.BOJACK;
import boss.list.Earth.BUJIN;
import boss.list.Earth.KOGU;
import boss.list.Earth.SUPER_BOJACK;
import boss.list.Earth.ZANGYA;
import boss.list.Yardart.CHIENBINH0;
import boss.list.Yardart.CHIENBINH1;
import boss.list.Yardart.CHIENBINH2;
import boss.list.Yardart.CHIENBINH3;
import boss.list.Yardart.CHIENBINH4;
import boss.list.Yardart.CHIENBINH5;
import boss.list.Yardart.DOITRUONG5;
import boss.list.Yardart.TANBINH0;
import boss.list.Yardart.TANBINH1;
import boss.list.Yardart.TANBINH2;
import boss.list.Yardart.TANBINH3;
import boss.list.Yardart.TANBINH4;
import boss.list.Yardart.TANBINH5;
import boss.list.Yardart.TAPSU0;
import boss.list.Yardart.TAPSU1;
import boss.list.Yardart.TAPSU2;
import boss.list.Yardart.TAPSU3;
import boss.list.Yardart.TAPSU4;
import boss.list.Cell.XENCON1;
import boss.list.Cell.XENCON2;
import boss.list.Cell.XENCON3;
import boss.list.Cell.XENCON4;
import boss.list.Cell.XENCON5;
import boss.list.Cell.XENCON6;
import boss.list.Cell.XENCON7;
import boss.list.Cumber.Cumber;
import boss.list.LunarNewYearEvent.LanCon;
import boss.miniboss.SoiHecQuyn;
import player.Player;
import network.Message;
import services.MapService;

import java.util.ArrayList;
import java.util.List;

import map.Zone;
import server.Maintenance;
import utils.Logger;

public class BossManager implements Runnable {

    private static BossManager instance;
    public static byte ratioReward = 10;

    public static BossManager gI() {
        if (instance == null) {
            instance = new BossManager();
        }
        return instance;
    }

    public BossManager() {
        this.bosses = new ArrayList<>();
    }

    protected final List<Boss> bosses;

    public void addBoss(Boss boss) {
        this.bosses.add(boss);
    }

    public void removeBoss(Boss boss) {
        this.bosses.remove(boss);
    }

    public void loadBoss() {
        this.createBoss(BossID.TIEU_DOI_TRUONG);
        this.createBoss(BossID.TIEU_DOI_TRUONG_NM);
        this.createBoss(BossID.BOJACK);
        this.createBoss(BossID.SUPER_BOJACK);
        this.createBoss(BossID.KING_KONG);
        this.createBoss(BossID.XEN_BO_HUNG);
        this.createBoss(BossID.SIEU_BO_HUNG);
        this.createBoss(BossID.KUKU, 1);
        this.createBoss(BossID.MAP_DAU_DINH, 1);
        this.createBoss(BossID.RAMBO, 1);
        this.createBoss(BossID.FIDE);
        this.createBoss(BossID.ANDROID_14);
        this.createBoss(BossID.DR_KORE);
        this.createBoss(BossID.COOLER);
        this.createBoss(BossID.BLACK_GOKU, 2);
        this.createBoss(BossID.GOLDEN_FRIEZA, 5);
        this.createBoss(BossID.BROLY, 30);
        this.createBoss(BossID.CUMBER);
        this.createBoss(SOI_HEC_QUYN1,3);
    }

    public void createBoss(int bossID, int total) {
        for (int i = 0; i < total; i++) {
            createBoss(bossID);
        }
    }

    public Boss createBoss(int bossID) {
        try {
            return switch (bossID) {
                case BossID.TAP_SU_0 ->
                    new TAPSU0();
                case BossID.TAP_SU_1 ->
                    new TAPSU1();
                case BossID.TAP_SU_2 ->
                    new TAPSU2();
                case BossID.TAP_SU_3 ->
                    new TAPSU3();
                case BossID.TAP_SU_4 ->
                    new TAPSU4();
                case BossID.TAN_BINH_5 ->
                    new TANBINH5();
                case BossID.TAN_BINH_0 ->
                    new TANBINH0();
                case BossID.TAN_BINH_1 ->
                    new TANBINH1();
                case BossID.TAN_BINH_2 ->
                    new TANBINH2();
                case BossID.TAN_BINH_3 ->
                    new TANBINH3();
                case BossID.TAN_BINH_4 ->
                    new TANBINH4();
                case BossID.CHIEN_BINH_5 ->
                    new CHIENBINH5();
                case BossID.CHIEN_BINH_0 ->
                    new CHIENBINH0();
                case BossID.CHIEN_BINH_1 ->
                    new CHIENBINH1();
                case BossID.CHIEN_BINH_2 ->
                    new CHIENBINH2();
                case BossID.CHIEN_BINH_3 ->
                    new CHIENBINH3();
                case BossID.CHIEN_BINH_4 ->
                    new CHIENBINH4();
                case BossID.DOI_TRUONG_5 ->
                    new DOITRUONG5();
                case BossID.SO_4 ->
                    new SO4();
                case BossID.SO_3 ->
                    new SO3();
                case BossID.SO_2 ->
                    new SO2();
                case BossID.SO_1 ->
                    new SO1();
                case BossID.TIEU_DOI_TRUONG ->
                    new TDT();
                case BossID.SO_4_NM ->
                    new SO4_NM();
                case BossID.SO_3_NM ->
                    new SO3_NM();
                case BossID.SO_2_NM ->
                    new SO2_NM();
                case BossID.SO_1_NM ->
                    new SO1_NM();
                case BossID.TIEU_DOI_TRUONG_NM ->
                    new TDT_NM();
                case BossID.BUJIN ->
                    new BUJIN();
                case BossID.KOGU ->
                    new KOGU();
                case BossID.ZANGYA ->
                    new ZANGYA();
                case BossID.BIDO ->
                    new BIDO();
                case BossID.BOJACK ->
                    new BOJACK();
                case BossID.SUPER_BOJACK ->
                    new SUPER_BOJACK();
                case BossID.KUKU ->
                    new Kuku();
                case BossID.MAP_DAU_DINH ->
                    new MapDauDinh();
                case BossID.RAMBO ->
                    new Rambo();
                case BossID.TAU_PAY_PAY_DONG_NAM_KARIN ->
                    new TaoPaiPai();
                case BossID.DRABURA ->
                    new Drabura();
                case BossID.BUI_BUI ->
                    new BuiBui();
                case BossID.BUI_BUI_2 ->
                    new BuiBui2();
                case BossID.YA_CON ->
                    new Yacon();
                case BossID.DRABURA_2 ->
                    new Drabura2();
                case BossID.GOKU ->
                    new Goku();
                case BossID.CADIC ->
                    new Cadic();
                case BossID.MABU_12H ->
                    new Mabu();
                case BossID.DRABURA_3 ->
                    new Drabura3();
                case BossID.MABU ->
                    new Mabu2H();
                case BossID.SUPERBU ->
                    new SuperBu();
                case BossID.FIDE ->
                    new Fide();
                case BossID.DR_KORE ->
                    new DrKore();
                case BossID.ANDROID_19 ->
                    new Android19();
                case BossID.ANDROID_13 ->
                    new Android13();
                case BossID.ANDROID_14 ->
                    new Android14();
                case BossID.ANDROID_15 ->
                    new Android15();
                case BossID.PIC ->
                    new Pic();
                case BossID.POC ->
                    new Poc();
                case BossID.KING_KONG ->
                    new KingKong();
                case BossID.XEN_BO_HUNG ->
                    new XenBoHung();
                case BossID.SIEU_BO_HUNG ->
                    new SieuBoHung();
                case BossID.XEN_CON_1 ->
                    new XENCON1();
                case BossID.XEN_CON_2 ->
                    new XENCON2();
                case BossID.XEN_CON_3 ->
                    new XENCON3();
                case BossID.XEN_CON_4 ->
                    new XENCON4();
                case BossID.XEN_CON_5 ->
                    new XENCON5();
                case BossID.XEN_CON_6 ->
                    new XENCON6();
                case BossID.XEN_CON_7 ->
                    new XENCON7();
                case BossID.COOLER ->
                    new Cooler();
                case BossID.BROLY ->
                    new Broly();
                case BossID.SUPER_BROLY ->
                    new SuperBroly();
                case BossID.AN_TROM ->
                    new AnTrom();
                case BossID.AN_TROM_TV ->
                    new AnTromTV();
                case BossID.KHIDOT ->
                    new KhiDot();
                case BossID.NGUYETTHAN ->
                    new NguyetThan();
                case BossID.NHATTHAN ->
                    new NhatThan();
                case BossID.GOLDEN_FRIEZA ->
                    new GoldenFrieza();
                case BossID.DEATH_BEAM_1 ->
                    new DeathBeam1();
                case BossID.DEATH_BEAM_2 ->
                    new DeathBeam2();
                case BossID.DEATH_BEAM_3 ->
                    new DeathBeam3();
                case BossID.DEATH_BEAM_4 ->
                    new DeathBeam4();
                case BossID.DEATH_BEAM_5 ->
                    new DeathBeam5();
                case BossID.BIMA ->
                    new BiMa();
                case BossID.MATROI ->
                    new MaTroi();
                case BossID.DOI ->
                    new Doi();
                case BossID.ONG_GIA_NOEL ->
                    new OngGiaNoel();
                case BossID.SON_TINH ->
                    new SonTinh();
                case BossID.THUY_TINH ->
                    new ThuyTinh();
                case BossID.LAN_CON ->
                    new LanCon();
                case BossID.BLACK_GOKU ->
                    new BlackGoku();
                case BossID.CUMBER ->
                    new Cumber();
                case BossID.SOI_HEC_QUYN1 ->
                    new SoiHecQuyn();
                default ->
                    null;
            };
        } catch (Exception e) {
            Logger.error(e + "\n");
            return null;
        }
    }

    public Boss getBoss(int id) {
        try {
            Boss boss = this.bosses.get(id);
            if (boss != null) {
                return boss;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public void showListBoss(Player player) {
        if (!player.isAdmin()) {
            return;
        }
        player.iDMark.setMenuType(3);
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg.writer().writeUTF("Boss");
            msg.writer().writeByte((int) bosses.stream().filter(boss -> !MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0]) && !MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])).count());
            for (int i = 0; i < bosses.size(); i++) {
                Boss boss = this.bosses.get(i);
                if (MapService.gI().isMapBossFinal(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapYardart(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapHuyDiet(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapMaBu(boss.data[0].getMapJoin()[0]) || MapService.gI().isMapBlackBallWar(boss.data[0].getMapJoin()[0])) {
                    continue;
                }
                msg.writer().writeInt(i);
                msg.writer().writeInt(i);
                msg.writer().writeShort(boss.data[0].getOutfit()[0]);
                if (player.getSession().version >= 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(boss.data[0].getOutfit()[1]);
                msg.writer().writeShort(boss.data[0].getOutfit()[2]);
                msg.writer().writeUTF(boss.data[0].getName());
                if (boss.zone != null) {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF(boss.zone.map.mapName + "(" + boss.zone.map.mapId + ") khu " + boss.zone.zoneId + "");
                } else {
                    msg.writer().writeUTF(boss.bossStatus.toString());
                    msg.writer().writeUTF("Chết rồi");
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public Boss getBossById(int bossId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && !boss.isDie()).findFirst().orElse(null);
    }

    public boolean checkBosses(Zone zone, int BossID) {
        return this.bosses.stream().filter(boss -> boss.id == BossID && boss.zone != null && boss.zone.equals(zone) && !boss.isDie()).findFirst().orElse(null) != null;
    }

    public Player findBossClone(Player player) {
        return player.zone.getBosses().stream().filter(boss -> boss.id < -100_000_000 && !boss.isDie()).findFirst().orElse(null);
    }

    public Boss getBossById(int bossId, int mapId, int zoneId) {
        return this.bosses.stream().filter(boss -> boss.id == bossId && boss.zone != null && boss.zone.map.mapId == mapId && boss.zone.zoneId == zoneId && !boss.isDie()).findFirst().orElse(null);
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning) {
            try {
                int delay = 150;
                long st = System.currentTimeMillis();
                for (int i = this.bosses.size() - 1; i >= 0; i--) {
                    try {
                        this.bosses.get(i).update();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//                if (delay - (System.currentTimeMillis() - st) > 0) {
//                    Thread.sleep(delay - (System.currentTimeMillis() - st));
//                }
                Functions.sleep(Math.max(delay - (System.currentTimeMillis() - st), 10));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
