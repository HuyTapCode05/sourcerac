package mob;

/*
 *
 *
 * @author YourSoulMatee
 */
import services.InventoryService;
import services.Service;
import services.TaskService;
import services.ItemMapService;
import consts.ConstMap;
import consts.ConstMob;
import consts.ConstTask;
import consts.ConstTaskBadges;
import consts.cn;
import event.EventManager;
import static event.EventManager.LUNNAR_NEW_YEAR;
import item.Item;
import map.ItemMap;

import java.util.List;

import map.Zone;
import player.Location;
import player.Pet;
import player.Player;
import network.Message;

import java.io.IOException;

import server.Maintenance;
import server.Manager;
import utils.Util;

import java.util.ArrayList;

import models.Achievement.AchievementService;
import models.Training.TrainingService;
import player.ItemLimitDay;
import server.ServerNotify;
import services.ChatGlobalService;
import services.ItemService;
import services.MapService;
import skill.Skill;
import task.Badges.BadgesTaskService;
import utils.TimeUtil;

public class Mob {

    public int id;
    public Zone zone;
    public int tempId;
    public String name;
    public byte level;

    public List<Player> temporaryEnemies = new ArrayList<>();

    public MobPoint point;
    public MobEffectSkill effectSkill;
    public Location location;

    public byte pDame;
    public int pTiemNang;
    private long maxTiemNang;

    public long lastTimeDie;
    public int lvMob = 0;
    public int status = 5;
    public int type = 1;

    private long lastTimeAttackPlayer;
    private long timeAttack = 2000;
    public long lastTimePhucHoi = System.currentTimeMillis();
    public long lastTimeSendEffect = System.currentTimeMillis();

    public Mob(Mob mob) {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
        this.id = mob.id;
        this.tempId = mob.tempId;
        this.level = mob.level;
        this.point.setHpFull(mob.point.getHpFull());
        this.point.sethp((this.point.getHpFull()));
        this.location.x = mob.location.x;
        this.location.y = mob.location.y;
        this.pDame = mob.pDame;
        this.pTiemNang = mob.pTiemNang;
        this.type = mob.type;
        this.setTiemNang();
    }

    public Mob() {
        this.point = new MobPoint(this);
        this.effectSkill = new MobEffectSkill(this);
        this.location = new Location();
    }

    public void setTiemNang() {
        this.maxTiemNang = (long) ((long) this.point.getHpFull() * 0.2 * (long) (this.pTiemNang + Util.nextInt(-5, 5)) / 100L);
    }

    public boolean isDie() {
        return this.point.gethp() <= 0;
    }

    public void setDie() {
        this.lastTimePhucHoi = System.currentTimeMillis();
        this.lastTimeDie = System.currentTimeMillis();
    }

    public void addTemporaryEnemies(Player pl) {
        if (pl != null && !temporaryEnemies.contains(pl)) {
            temporaryEnemies.add(pl);
        }
    }

    public void injured(Player plAtt, long damage, boolean dieWhenHpFull) {
        if (!this.isDie()) {
            if (damage >= this.point.hp) {
                damage = this.point.hp;
            }
            if (!dieWhenHpFull) {
                if (this.point.hp == this.point.maxHp && damage >= this.point.hp) {
                    damage = this.point.hp - 1;
                }
                if ((this.tempId == ConstMob.MOC_NHAN || this.tempId == ConstMob.BU_NHIN_MA_QUAI) && damage > this.point.maxHp / 10) {
                    damage = this.point.maxHp / 10;
                }
            }
            if (MapService.gI().isMapKhiGasHuyDiet(this.zone.map.mapId)) {
                boolean mob76Die = true;
                for (Mob mob : this.zone.mobs) {
                    if (!mob.isDie() && mob.tempId == ConstMob.CO_MAY_HUY_DIET) {
                        mob76Die = false;
                        break;
                    }
                }
                if (!mob76Die && plAtt != null && plAtt.playerSkill != null && plAtt.playerSkill.skillSelect != null) {
                    switch (plAtt.playerSkill.skillSelect.template.id) {
                        case Skill.LIEN_HOAN, Skill.ANTOMIC, Skill.MASENKO, Skill.KAMEJOKO ->
                            damage = 1;
                    }
                }
            }
            if (!dieWhenHpFull && !isBigBoss() && !MapService.gI().isMapPhoBan(this.zone.map.mapId) && this.lvMob > 0 && plAtt != null && plAtt.charms.tdOaiHung < System.currentTimeMillis()) {
                damage = (int) ((this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000) * (10.0 / 100));
                this.mobAttackPlayer(plAtt);
            }
            if (plAtt != null && plAtt.isBoss && this.tempId > 0 && Util.isTrue(1, 2) && Util.canDoWithTime(lastTimeAttackPlayer, 2500)) {
                this.mobAttackPlayer(plAtt);
                lastTimeAttackPlayer = System.currentTimeMillis();
            }

            this.point.hp -= damage;
            addTemporaryEnemies(plAtt);
            if (this.isDie()) {
                this.status = 0;
                this.setDie();
                this.temporaryEnemies.clear();
                if (plAtt != null) {
                    this.sendMobDieAffterAttacked(plAtt, (int) damage);
                    TaskService.gI().checkDoneTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneSideTaskKillMob(plAtt, this);
                    TaskService.gI().checkDoneClanTaskKillMob(plAtt, this);
                    AchievementService.gI().checkDoneTaskKillMob(plAtt, this);
                }
                if (this.id == 13) {
                    this.zone.isbulon1Alive = false;
                }
                if (this.id == 14) {
                    this.zone.isbulon2Alive = false;
                }
            } else {
                this.sendMobStillAliveAffterAttacked(damage, plAtt != null ? (plAtt.nPoint != null && plAtt.nPoint.isCrit) : false);
            }
            if (plAtt != null) {
                if (plAtt.isPl() && plAtt.satellite != null && plAtt.satellite.isDefend) {
                    plAtt.satellite.isDefend = false;
                }
                Service.gI().addSMTN(plAtt, (byte) 2, getTiemNangForPlayer(plAtt, damage), true);
                TrainingService.gI().tangTnsmLuyenTap(plAtt, getTiemNangForPlayer(plAtt, damage));
            }
        }
    }

    public long getTiemNangForPlayer(Player pl, long dame) {

        int levelPlayer = Service.gI().getCurrLevel(pl);
        int n = levelPlayer - this.level;

        // Kiểm tra xem người chơi có đang ở bản đồ "Kho Báu" không
        if (pl.zone != null && MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)) {
            n = 0;  // Không tính chênh lệch cấp độ
            // Áp dụng tỷ lệ x1000 cho tiềm năng nếu đang ở bản đồ Kho Báu
        }

        if (pl.nPoint != null && pl.nPoint.power < 40_000_000_000L) {
            n = 0;
        }

        long pDameHit = dame * 100 / point.getHpFull();
        long tiemNang = pDameHit * maxTiemNang / 50;
        int multiplier = 1;

        // Kiểm tra và áp dụng các bùa nếu tồn tại
        if (pl.charms != null) {
            if (pl.charms.tdTriTue > System.currentTimeMillis()) {
                multiplier += 1;
            }
            if (pl.charms.tdTriTue3 > System.currentTimeMillis()) {
                multiplier += 1;
            }
            if (pl.charms.tdTriTue4 > System.currentTimeMillis()) {
                multiplier += 1;
            }
        }

        // Kiểm tra cờ đặc biệt của người chơi
        if (pl.cFlag != 0) {
            if (pl.cFlag == 8) {
                tiemNang += ((long) tiemNang * 10 / 100);
            } else {
                tiemNang += ((long) tiemNang * 5 / 100);
            }
        }

        tiemNang *= multiplier;

        if (tiemNang <= 0) {
            tiemNang = 1;
        }

        // Áp dụng điều chỉnh tiềm năng tùy theo chênh lệch cấp độ (n)
        if (n >= 0) {
            for (int i = 0; i < n; i++) {
                long sub = tiemNang * 10 / 100;
                if (sub <= 0) {
                    sub = 1;
                }
                tiemNang -= sub;
            }
        } else {
            for (int i = 0; i < -n; i++) {
                long add = tiemNang * 10 / 100;
                if (add <= 0) {
                    add = 1;
                }
                tiemNang += add;
            }
        }

        if (tiemNang <= 0) {
            tiemNang = 1;
        }

        // Tính lại tiềm năng với sức mạnh
        if (pl.nPoint != null) {
            tiemNang = (int) pl.nPoint.calSucManhTiemNang(tiemNang);
        } else {
            return 0;
        }

        if (pl.zone.map.mapId == 122 || pl.zone.map.mapId == 123 || pl.zone.map.mapId == 124) {
            tiemNang *= 2;
        }

        return tiemNang;
    }

    public void update() {
        if (zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
            if (!isDie()) {
                startDie();
                return;
            }
        }
        if (!this.isDie() && this.tempId == ConstMob.CO_MAY_HUY_DIET && Util.canDoWithTime(lastTimeSendEffect, 1000)) {
            sendEffect(55);
            lastTimeSendEffect = System.currentTimeMillis();
        }

        if (this.isDie() && !Maintenance.isRunning && !isBigBoss()) {
            switch (zone.map.type) {
                case ConstMap.MAP_DOANH_TRAI:
                    if (this.tempId == ConstMob.BULON && this.zone.isTUTAlive && Util.canDoWithTime(lastTimeDie, 10000)) {
                        this.hoiSinh();
                        this.hoiSinhMobPhoBan();
                        if (this.id == 13) {
                            this.zone.isbulon1Alive = true;
                        }
                        if (this.id == 14) {
                            this.zone.isbulon2Alive = true;
                        }
                    }
                    break;
                case ConstMap.MAP_BAN_DO_KHO_BAU:
                    break;
                case ConstMap.MAP_CON_DUONG_RAN_DOC:
                    break;
                case ConstMap.MAP_KHI_GAS_HUY_DIET:
                    break;
                case ConstMap.MAP_TAY_KARIN:
                    break;
                default:
                    if (this.zone.isGoldenFriezaAlive && TimeUtil.is21H()) {
                        return;
                    }
                    if (Util.canDoWithTime(lastTimeDie, 5000)) {
                        this.hoiSinh();
                        this.sendMobHoiSinh();
                    }
                    if (Util.canDoWithTime(lastTimePhucHoi, 30000) && !isDie()) {
                        lastTimePhucHoi = System.currentTimeMillis();
                        long hpMax = this.point.maxHp;
                        if (this.point.hp < hpMax) {
                            hoi_hp(hpMax / 10);
                        } else {
                            this.sendMobHoiSinh();
                        }
                    }
            }
        }

        effectSkill.update();
        attack();
    }

    public boolean isBigBoss() {
        return (this.tempId == ConstMob.HIRUDEGARN
                || this.tempId == ConstMob.VUA_BACH_TUOC
                || this.tempId == ConstMob.ROBOT_BAO_VE
                || this.tempId == ConstMob.GAU_TUONG_CUOP
                || this.tempId == ConstMob.VOI_CHIN_NGA
                || this.tempId == ConstMob.GA_CHIN_CUA
                || this.tempId == ConstMob.NGUA_CHIN_LMAO
                || this.tempId == ConstMob.PIANO
                || this.tempId == ConstMob.KONG
                || this.tempId == ConstMob.GOZILLA);
    }

    public void attack() {
        Player player = getPlayerCanAttack();
        if (!isDie() && !effectSkill.isHaveEffectSkill() && tempId != ConstMob.MOC_NHAN && tempId != ConstMob.BU_NHIN_MA_QUAI && tempId != ConstMob.CO_MAY_HUY_DIET && !this.isBigBoss() && (this.lvMob < 1 || MapService.gI().isMapPhoBan(this.zone.map.mapId)) && Util.canDoWithTime(lastTimeAttackPlayer, timeAttack)) {
            if (player != null) {
                this.mobAttackPlayer(player);
            }
            this.lastTimeAttackPlayer = System.currentTimeMillis();
        }
    }

    public Player getPlayerCanAttack() {
        Player plAttack = getFirstPlayerCanAttack();
        if (plAttack != null) {
            return plAttack;
        }
        int distance = 100;
        try {
            List<Player> players = this.zone.getNotBosses();
            for (Player pl : players) {
                if (!pl.isDie() && !pl.isBoss && !pl.isNewPet && (pl.satellite == null || !pl.satellite.isDefend) && (pl.effectSkin == null || !pl.effectSkin.isVoHinh) && (this.tempId > 18 || (this.tempId > 9 && this.type == 4)) || isBigBoss()) {
                    int dis = Util.getDistance(pl, this);
                    if (dis <= distance || isBigBoss()) {
                        plAttack = pl;
                        distance = dis;
                    }
                }
            }
            this.timeAttack = 2000;
        } catch (Exception e) {

        }
        return plAttack;
    }

    private Player getFirstPlayerCanAttack() {
        Player plAtt = null;
        try {
            List<Player> playersMap = zone.getHumanoids();
            int dis = 300;
            if (playersMap != null) {
                for (Player plAttt : playersMap) {
                    if (plAttt.isDie() || plAttt.isBoss || (plAttt.satellite != null && plAttt.satellite.isDefend) || (plAttt.effectSkin != null && plAttt.effectSkin.isVoHinh) || !this.temporaryEnemies.contains(plAttt)) {
                        continue;
                    }
                    int d = Util.getDistance(plAttt, this);
                    if (d <= dis) {
                        dis = d;
                        plAtt = plAttt;
                    }
                }
            }
            this.timeAttack = 1000;
        } catch (Exception e) {

        }
        return plAtt;
    }

    private void mobAttackPlayer(Player player) {
        long dameMob = Util.maxIntValue(this.point.getDameAttack());
        if (player.charms != null && player.charms.tdDaTrau > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (player.isPet && ((Pet) player).master.charms != null && ((Pet) player).master.charms.tdDeTu > System.currentTimeMillis()) {
            dameMob /= 2;
        }
        if (this.lvMob > 0 && !MapService.gI().isMapPhoBan(this.zone.map.mapId)) {
            dameMob = (long) (player.nPoint.hpMax * (10.0 / 100));
        }
        if (player.satellite != null && player.satellite.isDefend) {
            dameMob -= dameMob / 5;
        }
        if (player.itemTime != null && player.itemTime.isUseCMS) {
            dameMob = (long) Math.round(dameMob * 0.1);
        }
        if (this.lvMob > 0 && player.charms.tdOaiHung > System.currentTimeMillis()) {
            dameMob = 0;
        }
        long dame = player.injured(null, Util.maxIntValue(dameMob), false, true);

        this.sendMobAttackMe(player, dame);
        this.sendMobAttackPlayer(player);
        this.phanSatThuong(player, dame);
    }

    private void sendMobAttackMe(Player player, long dame) {
        if (!player.isPet && !player.isNewPet) {
            Message msg;
            try {
                msg = new Message(-11);
                msg.writer().writeByte(this.id);
                msg.writeLongBySoulmate(Util.maxIntValue(dame), cn.readInt); //dame
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    private void sendMobAttackPlayer(Player player) {
        Message msg;
        try {
            msg = new Message(-10);
            msg.writer().writeByte(this.id);
            msg.writer().writeInt((int) player.id);
            msg.writeLongBySoulmate(Util.maxIntValue(player.nPoint.hp), cn.readInt); //hp
            Service.gI().sendMessAnotherNotMeInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinh() {
        this.status = 5;
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
    }

    public int lvMob() {
        for (Mob mobMap : this.zone.mobs) {
            if (mobMap.lvMob > 0) {
                return 0;
            }
        }
        this.lvMob = this.tempId > 18 && !isBigBoss() ? Util.isTrue(0, 100) ? 1 : 0 : 0;
        this.point.hp = this.lvMob > 0 ? this.point.maxHp <= 20000000 ? this.point.maxHp * 10 : 2000000000 : this.point.maxHp;
        return this.lvMob;
    }

    public void sendMobHoiSinh() {
        Message msg = null;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(lvMob());
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            this.sendMobMaxHp(this.point.hp);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void hoi_hp(long hp) {
        Message msg = null;
        try {
            this.point.sethp((this.point.gethp() + hp));
            long HP = hp > 0 ? 1 : Math.abs(hp);
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongBySoulmate(Util.maxIntValue(HP), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void sendEffect(int Effect) {
        Message msg = null;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(Effect);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
        } catch (Exception e) {
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    private void sendMobDieAffterAttacked(Player plKill, long dameHit) {
        Message msg;
        try {
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            msg.writeLongBySoulmate(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(plKill.nPoint.isCrit); // crit
            List<ItemMap> items = mobReward(plKill, this.dropItemTask(plKill), msg);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
            hutItem(plKill, items);
        } catch (Exception e) {
        }
    }

    private void hutItem(Player player, List<ItemMap> items) {
        if (!player.isPet && !player.isNewPet) {
            if (player.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(player, item.itemMapId, true);
                }
            }
        } else {
            if (((Pet) player).master.charms.tdThuHut > System.currentTimeMillis()) {
                for (ItemMap item : items) {
                    ItemMapService.gI().pickItem(((Pet) player).master, item.itemMapId, true);
                }
            }
        }
    }

    private List<ItemMap> mobReward(Player player, ItemMap itemTask, Message msg) {
        List<ItemMap> itemReward = new ArrayList<>();
        try {
            itemReward = this.getItemMobReward(player, this.location.x + Util.nextInt(-10, 10),
                    this.zone.map.yPhysicInTop(this.location.x, this.location.y));
            if (itemTask != null) {
                itemReward.add(itemTask);
            }
            msg.writer().writeByte(itemReward.size()); //sl item roi
            for (ItemMap itemMap : itemReward) {
                msg.writer().writeShort(itemMap.itemMapId);// itemmapid
                msg.writer().writeShort(itemMap.itemTemplate.id); // id item
                msg.writer().writeShort(itemMap.x); // xend item
                msg.writer().writeShort(itemMap.y); // yend item
                msg.writer().writeInt((int) itemMap.playerId); // id nhan vat
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemReward;
    }

    public List<ItemMap> getItemMobReward(Player player, int x, int yEnd) {
        List<ItemMap> list = new ArrayList<>();
        if (player.isBoss) {
            return list;
        }

        if (this.tempId == 0) {
            return list;
        }
        int mapid = player.zone.map.mapId;
        BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        //========================Sự Kiện Tết========================
        if (LUNNAR_NEW_YEAR) {
            // Rơi đồ ở bất kỳ map nào, không cần giới hạn bản đồ
            if (Util.isTrue(1, 10)) {  // Tỷ lệ rơi đồ là 30%
                list.add(new ItemMap(zone, 1472, 1, x, yEnd, player.id));
            }

            if (Util.isTrue(1, 50)) {  // Tỷ lệ rơi đồ là 100%
                list.add(new ItemMap(zone, 1474, 1, x, yEnd, player.id));
            }

            if (Util.isTrue(1, 100)) {  // Tỷ lệ rơi đồ là 1%
                list.add(new ItemMap(zone, 1473, 1, x, yEnd, player.id));
            }
        }

        //========================Capsul Kì Bí========================
        if (player.itemTime.isUseMayDo
                && (Util.isTrue(3, 100)
                || (player.isActive() && Util.isTrue(1, 50)))
                && this.tempId > 57 && this.tempId < 66) {
            list.add(new ItemMap(zone, 380, 1, x, yEnd, player.id));
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

        //========================TASK========================
        if (player.isPl()) {
            int taskId = TaskService.gI().getIdTask(player);
            boolean isDrop = Util.isTrue(3, 100); // Xác suất 25%
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

        //========================Map Bang Hội========================
        if (MapService.gI().isMapUpPorata(mapid)) {
            // Kiểm tra xác suất để tạo vật phẩm ngẫu nhiên, với tỉ lệ 100% (0, 100)
            if (Util.isTrue(0, 100)) {
                // Tạo vật phẩm ngẫu nhiên, với mã vật phẩm 934 và số lượng ngẫu nhiên từ 1 đến 10
                ItemMap it = new ItemMap(zone, 934, Util.nextInt(1, 10), x, yEnd, player.id);
                it.options.add(new Item.ItemOption(30, 0));  // Thêm tùy chọn vào vật phẩm
                list.add(it);  // Thêm vật phẩm vào danh sách
            } // Kiểm tra xem người chơi có thể thả mảnh vỡ hay không (tối đa 150 lần mỗi ngày) và với tỉ lệ 1% (1, 1)
            else if (player.itemEvent.canDropManhVo(249) && Util.isTrue(1, 200)) {

                ItemMap it = new ItemMap(zone, 933, 1, x, yEnd, player.id);
                // Thêm các tùy chọn cho vật phẩm
                it.options.add(new Item.ItemOption(31, 1));  // Tùy chọn 31 với giá trị 1
                it.options.add(new Item.ItemOption(30, 0));  // Tùy chọn 30 với giá trị 0
                // Thêm vật phẩm vào danh sách
                list.add(it);
            }
        }

        //======================== Vàng Ngọc ========================
        // Kiểm tra xem bản đồ có phải là bản đồ 3 hành tinh hay không
        if (MapService.gI().isMap3Planets(mapid)) {

            // Phân chia tỷ lệ rơi vàng dựa trên các điều kiện
            if (Util.isTrue(1, 20) // Xác suất chung là 1/100
                    || (Manager.TEST && Util.isTrue(1, 5))
                    || (player.isActive() && Util.isTrue(1, 20))
                    || (player.isAdmin() && Util.isTrue(1, 20))) {

                // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 500 đến 3000
                int vang = Util.nextInt(500, 3000);

                // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                if (vang < 1000) {
                    // Nếu vàng dưới 1000, rơi item vàng cấp 76 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 76, vang, x, yEnd, player.id));
                } else if (vang < 2000) {
                    // Nếu vàng từ 1000 đến 2000, rơi item vàng cấp 188 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
                } else {
                    // Nếu vàng trên 2000, rơi item vàng cấp 189 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
                }
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
            }
        }

        // Kiểm tra nếu bản đồ hiện tại là bản đồ Nappa
        if (MapService.gI().isMapNappa(mapid)) {

            // Kiểm tra điều kiện rơi vàng (bỏ qua tỷ lệ rơi vàng, chỉ xét điều kiện khác)
            if (Util.isTrue(1, 100) // Xác suất chung là 1/100
                    || (Manager.TEST && Util.isTrue(1, 5)) // Trong chế độ test, tỷ lệ rơi vàng là 1/5
                    || (player.isActive() && Util.isTrue(1, 20)) // Nếu người chơi đang hoạt động, tỷ lệ rơi vàng là 1/20
                    || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi vàng là 10%
                    ) {

                // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 2000 đến 6000
                int vang = Util.nextInt(2000, 6000);

                // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                if (vang < 3000) {
                    // Nếu vàng dưới 3000, rơi item vàng cấp 188 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id));
                } else if (vang < 5000) {
                    // Nếu vàng từ 3000 đến 5000, rơi item vàng cấp 189 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id));
                } else {
                    // Nếu vàng trên 5000, rơi item vàng cấp 190 với số lượng vàng tương ứng
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id));
                }

            }
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

        // Vàng cold
        if (MapService.gI().isMapCold(mapid)) {

            // Kiểm tra điều kiện rơi vàng với 4 trường hợp tài khoản khác nhau
            if (Util.isTrue(1, 100) // Xác suất chung là 1/100
                    || (Manager.TEST && Util.isTrue(1, 5)) // Trong chế độ test, tỷ lệ rơi vàng là 1/5
                    || (player.isActive() && Util.isTrue(1, 20)) // Nếu người chơi đang hoạt động, tỷ lệ rơi vàng là 1/20
                    || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi vàng là 10%
                    ) {

                // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 7000 đến 15000
                int vang = Util.nextInt(8000, 18000);

                // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id)); // Rơi vàng cấp 189
                } else if (vang < 14000) {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                }
            }
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

        // Vàng tương lai
        if (MapService.gI().isMapTuongLai(mapid)) {

            // Kiểm tra điều kiện rơi vàng với 4 trường hợp tài khoản khác nhau
            if (Util.isTrue(1, 100) // Xác suất chung là 1/100
                    || (Manager.TEST && Util.isTrue(1, 5)) // Trong chế độ test, tỷ lệ rơi vàng là 1/5
                    || (player.isActive() && Util.isTrue(1, 20)) // Nếu người chơi đang hoạt động, tỷ lệ rơi vàng là 1/20
                    || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi vàng là 10%
                    ) {

                // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 5000 đến 10000
                int vang = Util.nextInt(5000, 12000);

                // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                if (vang < 6000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id)); // Rơi vàng cấp 188
                } else if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id)); // Rơi vàng cấp 189
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                }
            }
        }

        // Vàng phó bản
        if (MapService.gI().isMapPhoBan(mapid)) {

            // Kiểm tra điều kiện rơi vàng với 4 trường hợp tài khoản khác nhau
            if (Util.isTrue(1, 100) // Xác suất chung là 1/100
                    || (Manager.TEST && Util.isTrue(1, 5)) // Trong chế độ test, tỷ lệ rơi vàng là 1/5
                    || (player.isActive() && Util.isTrue(1, 10)) // Nếu người chơi đang hoạt động, tỷ lệ rơi vàng là 1/10
                    || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi vàng là 10%
                    ) {

                // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 8000 đến 20000
                int vang = Util.nextInt(8000, 20000);

                // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                if (vang < 6000) {
                    list.add(new ItemMap(zone, 188, vang, x, yEnd, player.id)); // Rơi vàng cấp 188
                } else if (vang < 10000) {
                    list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id)); // Rơi vàng cấp 189
                } else {
                    list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                }
            }

            // Vàng cold
            if (MapService.gI().isMapCold(mapid) || MapService.gI().isMapHanhTinhThucVat(mapid)) {

                // Kiểm tra điều kiện rơi vàng với 4 trường hợp tài khoản khác nhau
                if (Util.isTrue(1, 100) // Xác suất chung là 1/100
                        || (Manager.TEST && Util.isTrue(1, 5)) // Trong chế độ test, tỷ lệ rơi vàng là 1/5
                        || (player.isActive() && Util.isTrue(1, 20)) // Nếu người chơi đang hoạt động, tỷ lệ rơi vàng là 1/20
                        || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi vàng là 10%
                        ) {

                    // Tính số lượng vàng rơi ra ngẫu nhiên trong khoảng từ 7000 đến 15000
                    int vang = Util.nextInt(8000, 18000);

                    // Phân chia các mức vàng rơi dựa trên giá trị vàng đã tính
                    if (vang < 10000) {
                        list.add(new ItemMap(zone, 189, vang, x, yEnd, player.id)); // Rơi vàng cấp 189
                    } else if (vang < 14000) {
                        list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                    } else {
                        list.add(new ItemMap(zone, 190, vang, x, yEnd, player.id)); // Rơi vàng cấp 190
                    }
                }
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
            }
        }

        // Ngọc
        if (Util.isTrue(1, 100) // Xác suất chung là 1/100000
                || (Manager.TEST && Util.isTrue(1, 10)) // Trong chế độ test, tỷ lệ rơi ngọc là 1/10
                || (player.isActive() && Util.isTrue(15, 1000)) // Nếu người chơi đang hoạt động, tỷ lệ rơi ngọc là 1/200
                || (player.isAdmin() && Util.isTrue(1, 10)) // Nếu người chơi là admin, tỷ lệ rơi ngọc là 10%
                ) {
            // Thay đổi ngọc muốn rơi ở đây
            int ngoc = Util.nextInt(1, 1);
            list.add(new ItemMap(zone, 77, ngoc, x, yEnd, player.id));  // Thêm ngọc vào danh sách item
        }

        //========================SKH========================
// Kiểm tra nếu người chơi là thành viên mới (isNewMember) và đang ở đúng map
// và theo xác suất kích hoạt tính năng
        if ((!player.isNewMember || !MapService.gI().isMapUpSKH(mapid)) 
                || (!Manager.TEST || !Util.isTrue(1, 10)
                && (!player.isActive() || !MapService.gI().isMapUpSKH(mapid)) 
                && (!player.isAdmin() || !MapService.gI().isMapUpSKH(mapid)) 
                )) {
           
        } else {
        }
        {
            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
            if (!ops.isEmpty()) {
                it.options = ops;
            }

            int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
            it.options.add(new Item.ItemOption(opsrand[0], 0));
            it.options.add(new Item.ItemOption(opsrand[1], 0));
            it.options.add(new Item.ItemOption(30, 0));
            list.add(it);
        }
        if (((player.isActive() && Util.isTrue(1, 50000)) // neu mtv thi 1/50k%
                || (player.isNewMember && Util.isTrue(1, 100000))) // neu la newbie ma chua mtv thì 1/100k
                && MapService.gI().isMapUpSKH(mapid) // check map up
                ) {
            short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
            if (!ops.isEmpty()) {
                it.options = ops;
            }

            int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(player.gender);
            it.options.add(new Item.ItemOption(opsrand[0], 0));
            it.options.add(new Item.ItemOption(opsrand[1], 0));
            it.options.add(new Item.ItemOption(opsrand[2], 0));
            it.options.add(new Item.ItemOption(opsrand[3], 0)); // SKH mới thêm 4 option
            it.options.add(new Item.ItemOption(30, 0));
            list.add(it);
        }

        //========================Đồ Sao Khác Vải Thô========================
        if (((player.isActive() && Util.isTrue(0, 5000)) // Nếu người chơi đang hoạt động và có xác suất 1/10000
                || (Manager.TEST && Util.isTrue(1, 100))) // Nếu trong chế độ TEST và có xác suất 1/10000
                && MapService.gI().isMapNappa(mapid) // Kiểm tra nếu là bản đồ Nappa
                ) {
            short itTemp = (short) ItemService.gI().randTempItemDoSao(player.gender);  // Lấy item sao ngẫu nhiên cho người chơi
            ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);  // Tạo đối tượng item
            List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);  // Lấy danh sách option cho item

            if (!ops.isEmpty()) {
                it.options = ops;  // Nếu có options thì gán vào item
            }

            // Thêm option dựa trên xác suất
            int randOption = Util.nextInt(100);  // Lấy số ngẫu nhiên từ 0 đến 99
            boolean hasOption = false;  // Biến kiểm tra có thêm option hay không

            // 50% xác suất thêm option
            if (randOption < 50) {
                int randAddOption = Util.nextInt(100);  // Lấy số ngẫu nhiên để quyết định sao nào
                if (randAddOption < 50) {  // 45% cho option 1 (sao 1)
                    it.options.add(new Item.ItemOption(107, 1));  // Thêm option sao 1
                    hasOption = true;
                } else if (randAddOption < 90) {  // 30% cho option 2 (sao 2)
                    it.options.add(new Item.ItemOption(107, 2));  // Thêm option sao 2
                    hasOption = true;
                } else {  // 25% cho option 3 (sao 3)
                    it.options.add(new Item.ItemOption(107, 3));  // Thêm option sao 3
                    hasOption = true;
                }
            }

            // Nếu có option (sao) thì mới thêm vào list
            if (hasOption) {
                list.add(it);  // Thêm item vào danh sách item rơi

            }

        }

        //========================Đồ Sao 3 Map Đầu========================
        if (((Util.isTrue(1, 50))
                || (Manager.TEST && Util.isTrue(1, 30))
                || (player.isAdmin() && Util.isTrue(15, 15)))
                && MapService.gI().isMapUpSKH(mapid)) {

            // Tính toán tỷ lệ dựa trên sức mạnh
            int baseRate = 50; // Tỷ lệ cơ bản 50%
            int powerReduction = (int) Math.min(player.nPoint.power / 100000, 5) * 20; // Giảm 20% mỗi 100k sức mạnh, tối đa 5 lần
            int finalRate = Math.max(baseRate - powerReduction, 0); // Tỷ lệ không âm

            // Nếu tỷ lệ > 0, kiểm tra xác suất
            if (finalRate > 0 && Util.nextInt(100) < finalRate) {
                short itTemp = (short) ItemService.gI().randDoSao(player.gender);
                ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, player.id);
                List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);

                if (!ops.isEmpty()) {
                    it.options = ops;
                }

                // Thêm option dựa trên xác suất
                int randOption = Util.nextInt(100); // Lấy số ngẫu nhiên từ 0 đến 99
                boolean hasOption = false; // Biến để kiểm tra có thêm option hay không

                // 50% xác suất thêm option 
                if (randOption < 50) {
                    int randAddOption = Util.nextInt(100); // Lấy số ngẫu nhiên để quyết định thêm sao nào
                    if (randAddOption < 60) { // 60% cho option 1 (sao 1)
                        it.options.add(new Item.ItemOption(107, 1)); // Option 1 (sao 1)
                        hasOption = true;
                    } else if (randAddOption < 90) { // 30% cho option 2 (sao 2)
                        it.options.add(new Item.ItemOption(107, 2)); // Option 2 (sao 2)
                        hasOption = true;
                    } else { // 10% cho option 3 (sao 3)
                        it.options.add(new Item.ItemOption(107, 3)); // Option 3 (sao 3)
                        hasOption = true;
                    }
                }

                // Nếu có option (sao) thì mới thêm vào list
                if (hasOption) {
                    list.add(it);
                }
            }
        }

//========================Đồ Thần + Thức Ăn========================
        if (MapService.gI().isMapCold(mapid)) {
            if (player.isPet) {
                player = ((Pet) player).master;
            }
            if (player.isPet) {
                player = ((Pet) player).master;
            }
            if (Util.isTrue(1, 2000000)
                    || (player.isActive() && Util.isTrue(1, 250000))
                    || (Manager.TEST && Util.isTrue(1, 2000))
                    || (player.isAdmin() && Util.isTrue(20, 500))) {
                ItemMap it = ItemService.gI().randDoTL(this.zone, 1, x, yEnd, player.id);
                list.add(it);

                String message = player.name + " vừa nhặt được " + it.itemTemplate.name
                        + " tại " + this.zone.map.mapName + " khu " + this.zone.zoneId;
                ServerNotify.gI().notify(message);

                ChatGlobalService.gI().autoChatGlobal(player, "[Hệ thống] " + message);

                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
            }
            if (Util.isTrue(1, 20000) // 1/20000 xác suất
                    || (player.isActive() && Util.isTrue(1, 700)) // 1/700 xác suất khi người chơi hoạt động
                    || (Manager.TEST && Util.isTrue(1, 100)) // 1/100 xác suất khi là test
                    || (player.isAdmin() && Util.isTrue(10, 100))) { // 10/100 xác suất khi là admin
                if (InventoryService.gI().fullSetThan(player)) {
                    // Tạo item thức ăn
                    ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
                    it.options.add(new Item.ItemOption(30, 0)); // Option thức ăn
                    list.add(it);

                    // Cộng nhiệm vụ khi nhặt thức ăn
                    BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
                }
            }
        }

// Thức ăn tương lai
        if (MapService.gI().isMapTuongLai(mapid)
                && ((Util.isTrue(1, 20000) // 1/20000 xác suất
                || (player.isActive() && Util.isTrue(1, 1000)) // 1/1000 xác suất khi người chơi kích hoạt
                || (Manager.TEST && Util.isTrue(1, 200)) // 1/500 xác suất khi là test
                || (player.isAdmin() && Util.isTrue(10, 100)))) // 10/100 xác suất khi là admin
                && InventoryService.gI().fullSetThan(player)) {

            ItemMap it = new ItemMap(zone, Util.nextInt(663, 667), 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(30, 0)); // Option thức ăn
            list.add(it);

            // Cộng nhiệm vụ khi nhặt thức ăn tương lai
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

//========================Sao Pha Lê Mảnh đá Vụn Đá Nâng Cấp========================
        if (player.nPoint.isDoSPL
                && (Util.isTrue(1, 20000) // 1/20000 xác suất
                || (player.isActive() && Util.isTrue(1, 200)) // 1/1000 xác suất khi người chơi hoạt động
                || (Manager.TEST && Util.isTrue(1, 50)) // 1/500 xác suất khi là test
                || (player.isAdmin() && Util.isTrue(10, 100)))) { // 10/100 xác suất khi là admin

            int rand = Util.nextInt(0, 6); // Lấy giá trị ngẫu nhiên từ 0 đến 5
            ItemMap it = new ItemMap(zone, 441 + rand, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(95 + rand, (rand == 3 || rand == 4) ? 3 : 5));
            list.add(it);

            // Cộng nhiệm vụ khi nhặt đá sao pha lê
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

// Đá nâng cấp
        if (MapService.gI().isMapCold(mapid)) {
            if (Util.isTrue(1, 10000)
                    || (Manager.TEST && Util.isTrue(1, 5))
                    || (player.isActive() && Util.isTrue(1, 500))) {
                int rand = Util.nextInt(0, 4);
                ItemMap it = new ItemMap(zone, 220 + rand, 1, x, yEnd, player.id);
                it.options.add(new Item.ItemOption(71 - rand, 0));
                list.add(it);

                // Cộng nhiệm vụ khi nhặt đá nâng cấp
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
            }
        }

// Mảnh đá vụn cho bản đồ Doanh Trại
        if (MapService.gI().isMapDoanhTrai(mapid)
                && (Util.isTrue(1, 10000)
                || (Manager.TEST && Util.isTrue(1, 5))
                || (player.isActive() && Util.isTrue(1, 10)))) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);

            // Cộng nhiệm vụ khi nhặt mảnh đá vụn cho Doanh Trại
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

// Mảnh đá vụn cho bản đồ 3 Planets (tỷ lệ khác)
        if (MapService.gI().isMap3Planets(mapid)
                && (Util.isTrue(1, 8000)
                || (Manager.TEST && Util.isTrue(1, 10))
                || (player.isActive() && Util.isTrue(1, 24)))) {
            ItemMap it = new ItemMap(zone, 225, 1, x, yEnd, player.id);
            it.options.add(new Item.ItemOption(74, 0));
            list.add(it);

            // Cộng nhiệm vụ khi nhặt mảnh đá vụn cho 3 Planets
            BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
        }

// Kiểm tra nếu map nằm trong danh sách các map cần áp dụng xác suất
        if (MapService.gI().isMap3Planets(mapid)
                || MapService.gI().isMapNappa(mapid)
                || MapService.gI().isMapTuongLai(mapid)
                || MapService.gI().isMapCold(mapid)) {
            if (Util.isTrue(1, 700)
                    || (player.isActive() && Util.isTrue(1, 100))) {
                int rand = Util.nextInt(0, 1);
                ItemMap it = new ItemMap(zone, 19 + rand, 1, x, yEnd, player.id);
                list.add(it);

                // Cộng nhiệm vụ khi nhặt mảnh đá hoặc đồ từ các bản đồ này
                BadgesTaskService.updateCountBagesTask(player, ConstTaskBadges.ONG_THAN_VE_CHAI, 1);
            }
        }

        //========================Mảnh Thiên Sứ========================
        if ((Util.isTrue(1, 10000)
                || (player.isActive() && Util.isTrue(2, 1000))) && MapService.gI().isMapHanhTinhThucVat(mapid) && InventoryService.gI().findItemNTK(player)) {

            list.add(new ItemMap(zone, Util.nextInt(1066, 1070), 1, x, yEnd, player.id));
        }

        return list;
    }

    private ItemMap dropItemTask(Player player) {
        ItemMap itemMap = null;
        switch (tempId) {
            case ConstMob.KHUNG_LONG:
            case ConstMob.LON_LOI:
            case ConstMob.QUY_DAT:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_2_0) {
                    itemMap = new ItemMap(zone, 73, 1, location.x, location.y, player.id);
                }
                break;
            case ConstMob.THAN_LAN_ME:
            case ConstMob.QUY_BAY_ME:
            case ConstMob.PHI_LONG_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_8_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 20, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con thằn lằn mẹ này không giữ ngọc, hãy tìm con thằn lằn mẹ khác");
                    }
                }
            case ConstMob.OC_MUON_HON:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con ốc mượn hồn này không giữ truyện tranh, hãy thử tìm con ốc mượn hồn khác");
                    }
                }
            case ConstMob.HEO_XAYDA_ME:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con heo xayda mẹ này không giữ truyện tranh, hãy thử tìm con heo xayda mẹ khác");
                    }
                }
            case ConstMob.OC_SEN:
                if (TaskService.gI().getIdTask(player) == ConstTask.TASK_14_1) {
                    if (Util.isTrue(1, 3)) {
                        itemMap = new ItemMap(zone, 85, 1, location.x, location.y, player.id);
                    } else {
                        Service.gI().sendThongBao(player, "Con ốc xên này không giữ truyện tranh, hãy thử tìm con ốc xên khác");
                    }
                }
        }
        if (itemMap != null) {
            return itemMap;
        }
        return null;
    }

    private void sendMobStillAliveAffterAttacked(long dameHit, boolean crit) {
        Message msg;
        try {
            msg = new Message(-9);
            msg.writer().writeByte(this.id);
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.gethp()), cn.readInt);
            msg.writeLongBySoulmate(Util.maxIntValue(dameHit), cn.readInt);
            msg.writer().writeBoolean(crit); // chí mạng
            msg.writer().writeInt(-1);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobPhoBan() {
        this.point.hp = this.point.maxHp;
        this.setTiemNang();
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void hoiSinhMobTayKarin() {
        this.point.hp = this.point.maxHp;
        this.maxTiemNang = 1;
        Message msg;
        try {
            msg = new Message(-13);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(this.tempId);
            msg.writer().writeByte(this.lvMob); //level mob
            msg.writeLongBySoulmate(Util.maxIntValue(this.point.hp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendSieuQuai(int type) {
        Message msg;
        try {
            msg = new Message(-75);
            msg.writer().writeByte(this.id);
            msg.writer().writeByte(type);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDisable(boolean bool) {
        Message msg;
        try {
            msg = new Message(81);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendDoneMove(boolean bool) {
        Message msg;
        try {
            msg = new Message(82);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendFire(boolean bool) {
        Message msg;
        try {
            msg = new Message(85);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendIce(boolean bool) {
        Message msg;
        try {
            msg = new Message(86);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendWind(boolean bool) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writer().writeBoolean(bool);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    public void sendMobMaxHp(long maxHp) {
        Message msg;
        try {
            msg = new Message(87);
            msg.writer().writeByte(this.id);
            msg.writeLongBySoulmate(Util.maxIntValue(maxHp), cn.readInt);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }

    private void phanSatThuong(Player plTarget, long dame) {
        if (plTarget.nPoint == null) {
            return;
        }
        int percentPST = plTarget.nPoint.tlPST;
        if (percentPST != 0) {
            long damePST = Util.maxIntValue(dame * percentPST / 100L);
            Message msg;
            try {
                msg = new Message(-9);
                msg.writer().writeByte(this.id);
                if (damePST >= this.point.hp) {
                    damePST = this.point.hp - 1;
                }
                long hpMob = Util.maxIntValue(this.point.hp);
                injured(null, damePST, true);
                damePST = hpMob - this.point.hp;
                msg.writeLongBySoulmate(Util.maxIntValue(this.point.hp), cn.readInt);
                msg.writeLongBySoulmate(Util.maxIntValue(damePST), cn.readInt);
                msg.writer().writeBoolean(false);
                msg.writer().writeByte(36);
                Service.gI().sendMessAllPlayerInMap(this.zone, msg);
                msg.cleanup();
            } catch (IOException e) {
            }
        }
    }

    public void startDie() {
        Message msg;
        try {
            setDie();
            this.point.hp = -1;
            this.status = 0;
            msg = new Message(-12);
            msg.writer().writeByte(this.id);
            Service.gI().sendMessAllPlayerInMap(this.zone, msg);
            msg.cleanup();
        } catch (IOException e) {
        }
    }
}
