package Traoqua;


import item.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jdbc.DBConnecter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Player;
import server.Client;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Logger;

public class RewardSystem {
    private static final ExecutorService executor = Executors.newFixedThreadPool(3); // Xử lý tối đa 3 quà cùng lúc
private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public void distributeRewards() {
        System.out.println("🔹 Bắt đầu trao quà top...");
        
        // Cập nhật xếp hạng trước khi trao thưởng
        updateRankings();

        // Trao thưởng cho từng bảng xếp hạng
        distributeTopRewards("power", "top_power", "Thưởng top sức mạnh");
        distributeTopRewards("tasks", "top_task", "Thưởng top nhiệm vụ");
        distributeTopRewards("recharge", "top_recharge", "Thưởng top nạp");

        // Reset lại bảng xếp hạng sau khi trao quà
        resetRankings();

        System.out.println("✅ Hoàn thành trao quà top!");
    }

    private void distributeTopRewards(String type, String table, String message) {
        List<Integer> topPlayerIds = getTopPlayerIds(type, 10);

        for (int i = 0; i < topPlayerIds.size(); i++) {
            int playerId = topPlayerIds.get(i);
            Player player = Client.gI().getPlayerById(playerId);
            String rewardJson = getRewardFromSQL(table, i + 1);

            int rank = i + 1;
            System.out.println("🎁 Trao quà " + message + " cho Player ID " + playerId + " (Hạng " + rank + ")");

            executor.submit(() -> {
                try {
                    if (player != null) {
                        giveReward(player, rewardJson, message);
                    } else {
                        savePendingReward(playerId, rewardJson, message);
                        System.out.println("💾 Lưu quà cho Player ID " + playerId + " (offline).");
                    }
                } catch (Exception e) {
                    Logger.logException(RewardSystem.class, e);
                }
            });
        }
    }

    private void resetRankings() {
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM rankings")) {
            stmt.executeUpdate();
            System.out.println("🔄 Đã reset bảng xếp hạng.");
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
    }

   public void checkPendingRewards(Player player) {
    scheduler.schedule(() -> {
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, reward, message FROM pending_rewards WHERE player_id = ?")) {
            
            stmt.setInt(1, (int) player.id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String rewardJson = rs.getString("reward");
                String message = rs.getString("message");
                giveReward(player, rewardJson, message);
                
                try (PreparedStatement delStmt = conn.prepareStatement("DELETE FROM pending_rewards WHERE id = ?")) {
                    delStmt.setInt(1, rs.getInt("id"));
                    delStmt.executeUpdate();
                }
            }
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
    }, 5, TimeUnit.SECONDS); // Delay 5 giây trước khi thực thi
}

    private List<Integer> getTopPlayerIds(String type, int limit) {
        List<Integer> playerIds = new ArrayList<>();
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT player_id FROM rankings WHERE type = ? ORDER BY value DESC LIMIT ?")) {
            stmt.setString(1, type);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                playerIds.add(rs.getInt("player_id"));
            }
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
        return playerIds;
    }

    private void giveReward(Player player, String rewardJson, String message) {
        JSONArray items = (JSONArray) JSONValue.parse(rewardJson);
        for (Object obj : items) {
            JSONObject item = (JSONObject) obj;
            int itemId = Integer.parseInt(item.get("id").toString());
            int quantity = Integer.parseInt(item.get("soluong").toString());

            Item itemc = ItemService.gI().createNewItem((short) itemId, quantity);
            if (item.containsKey("options")) {
                JSONArray options = (JSONArray) item.get("options");
                for (Object opt : options) {
                    JSONObject option = (JSONObject) opt;
                    int optionId = Integer.parseInt(option.get("id").toString());
                    int param = Integer.parseInt(option.get("param").toString());
                    itemc.itemOptions.add(new Item.ItemOption(optionId, param));
                }
            }

            player.inventory.itemsMailBox.add(itemc);
            InventoryService.gI().sendItemBag(player);
        }
        Service.gI().sendThongBao(player, message);
 
    }

    private void savePendingReward(int playerId, String rewardJson, String message) {
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO pending_rewards (player_id, reward, message, created_at) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, playerId);
            stmt.setString(2, rewardJson);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
    }

    private void saveRewardToSQL(int playerId, String rewardJson, String message) {
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO reward_history (player_id, reward, message, received_at) VALUES (?, ?, ?, NOW())")) {
            stmt.setInt(1, playerId);
            stmt.setString(2, rewardJson);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
    }

    private String getRewardFromSQL(String table, int rank) {
        String reward = "[]";
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT reward FROM " + table + " WHERE rank = ?")) {
            stmt.setInt(1, rank);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                reward = rs.getString("reward");
            }
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
        return reward;
    }
       private void updateRankings() {
        try (Connection conn = DBConnecter.getConnectionServer()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO rankings (player_id, type, value) "
                            + "SELECT id, 'power', CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_point, ',', 2), ',', -1) AS UNSIGNED) "
                            + "FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) "
                            + "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO rankings (player_id, type, value) "
                            + "SELECT id, 'tasks', JSON_UNQUOTE(JSON_EXTRACT(data_task, '$[0]')) "
                            + "FROM player "
                            + "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO rankings (player_id, type, value) "
                            + "SELECT p.id, 'recharge', a.tongnap FROM player p "
                            + "JOIN account a ON p.account_id = a.id "
                            + "ON DUPLICATE KEY UPDATE value = VALUES(value)")) {
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            Logger.logException(RewardSystem.class, e);
        }
    }
}
