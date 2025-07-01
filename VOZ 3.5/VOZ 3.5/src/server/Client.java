package server;

import VOZ.Functions;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import map.ItemMap;
import player.Player;
import network.SessionManager;
import network.inetwork.ISession;
import server.io.MySession;
import services.Service;
import services.func.ChangeMapService;
import services.func.SummonDragon;
import services.func.TransactionService;
import services.NgocRongNamecService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import models.DragonNamecWar.TranhNgoc;
import services.func.SummonDragonNamek;
import utils.Logger;
import utils.Util;

public class Client implements Runnable {

    private static Client instance;
    private final Map<Long, Player> players_id = new ConcurrentHashMap<>();
    private final Map<Integer, Player> players_userId = new ConcurrentHashMap<>();
    private final Map<String, Player> players_name = new ConcurrentHashMap<>();
    private final List<Player> players = new CopyOnWriteArrayList<>();

    private Client() {
        new Thread(this, "Update Client").start();
    }

    public List<Player> getPlayers() {
        return this.players;
    }
    public Player getPlayerById(int id) {
    for (Player player : this.players) {
        if (player.id == id) {
            return player; 
        }
    }
    return null;
}

    public static Client gI() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void put(Player player) {
        players_id.putIfAbsent(player.id, player);
        players_name.putIfAbsent(player.name, player);
        players_userId.putIfAbsent(player.getSession().userId, player);
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    private void remove(MySession session) {
        if (session.player != null) {
            this.remove(session.player);
            session.player.dispose();
        }
        if (session.joinedGame) {
            session.joinedGame = false;
            try {
                DBConnecter.executeUpdate("update account set last_time_logout = ? where id = ?",
                        new Timestamp(System.currentTimeMillis()), session.userId);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }
        ServerManager.gI().disconnect(session);
    }

    private void remove(Player player) {
        players_id.remove(player.id);
        players_name.remove(player.name);
        players_userId.remove(player.getSession().userId);
        players.remove(player);
        if (!player.beforeDispose) {
            player.beforeDispose = true;
            player.mapIdBeforeLogout = player.zone.map.mapId;
            TranhNgoc.gI().removePlayersBlue(player);
            TranhNgoc.gI().removePlayersRed(player);
            if (player.idNRNM != -1) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1, player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);
                NgocRongNamecService.gI().pNrNamec[player.idNRNM - 353] = "";
                NgocRongNamecService.gI().idpNrNamec[player.idNRNM - 353] = -1;
                player.idNRNM = -1;
            }
            ChangeMapService.gI().exitMap(player);
            TransactionService.gI().cancelTrade(player);
            if (player.clan != null) {
                player.clan.removeMemberOnline(null, player);
            }
            if (SummonDragon.gI().playerSummonShenron != null
                    && SummonDragon.gI().playerSummonShenron.id == player.id) {
                SummonDragon.gI().isPlayerDisconnect = true;
            }
            if (SummonDragonNamek.gI().playerSummonShenron != null
                    && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
                SummonDragonNamek.gI().isPlayerDisconnect = true;
            }
            if (player.shenronEvent != null) {
                player.shenronEvent.isPlayerDisconnect = true;
            }
            if (player.mobMe != null) {
                player.mobMe.mobMeDie();
            }
            if (player.pet != null) {
                if (player.pet.mobMe != null) {
                    player.pet.mobMe.mobMeDie();
                }
                ChangeMapService.gI().exitMap(player.pet);
            }
        }
        PlayerDAO.updatePlayer(player);
    }

    public void kickSession(MySession session) {
        if (session != null) {
            session.disconnect();
            this.remove(session);
        }
    }

    public Player getPlayer(long playerId) {
        return players_id.get(playerId);
    }

    public Player getRandPlayer() {
        if (players.isEmpty()) {
            return null;
        }
        return players.get(Util.nextInt(players.size()));
    }

    public Player getPlayerByUser(int userId) {
        return players_userId.get(userId);
    }

    public Player getPlayer(String name) {
        return players_name.get(name);
    }

    public Player getPlayerByID(int playerId) {
        for (Player player : players) {
            if (player != null && player.id == playerId) {
                return player;
            }
        }
        return null;
    }

    public void close() {
        Logger.log(Logger.YELLOW, "BEGIN KICK OUT SESSION " + players.size() + "\n");
        for (Player pl : new ArrayList<>(players)) {
            if (pl != null && pl.getSession() != null) {
                this.kickSession((MySession) pl.getSession());
            }
        }
        Logger.success("SUCCESSFUL\n");
    }

    private void update() {
        List<ISession> sessions = new ArrayList<>(SessionManager.gI().getSessions());
        for (ISession s : sessions) {
            MySession session = (MySession) s;
            if (session == null) {
                SessionManager.gI().getSessions().remove(s);
                continue;
            }
            if (session.timeWait > 0) {
                session.timeWait--;
                if (session.timeWait == 0) {
                    kickSession(session);
                }
            }
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            long sleepTime = Math.max(1000 - (System.currentTimeMillis() - st), 10);
            Functions.sleep(sleepTime);
        }
    }

    public void show(Player player) {
        StringBuilder txt = new StringBuilder();
        txt.append("sessions: ").append(SessionManager.gI().getNumSession()).append("\n");
        txt.append("players_id: ").append(players_id.size()).append("\n");
        txt.append("players_userId: ").append(players_userId.size()).append("\n");
        txt.append("players_name: ").append(players_name.size()).append("\n");
        txt.append("players: ").append(players.size()).append("\n");
        Service.gI().sendThongBao(player, txt.toString());
    }
}
