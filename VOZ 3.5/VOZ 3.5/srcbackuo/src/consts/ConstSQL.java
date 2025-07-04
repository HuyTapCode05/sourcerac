package consts;

import consts.event.ConstDataEventSM;

public class ConstSQL {

    public static final String TOP_SM = "SELECT name, gender, items_body, CAST( JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) AS sm FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY CAST( JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) DESC LIMIT 20;";
    public static final String TOP_NAP = "SELECT name, gender, items_body,CAST( cash AS UNSIGNED) AS cash FROM account, player WHERE account.id = player.account_id ORDER BY cash DESC LIMIT 20;";

    public static final String TOP_DUA_SM = "SELECT name, gender, items_body, CAST( JSON_EXTRACT(data_point, '$[1]') AS UNSIGNED) AS sm FROM player WHERE create_time > '2024-" + ConstDataEventSM.MONTH_OPEN + "-" + ConstDataEventSM.DATE_OPEN + " " + ConstDataEventSM.HOUR_OPEN + ":" + ConstDataEventSM.MIN_OPEN + ":00' ORDER BY CAST( split_str(data_point,',',2) AS UNSIGNED) DESC LIMIT 20;";
    public static final String TOP_DUA_NAP = "SELECT name, gender, items_body, CAST( danap AS UNSIGNED) AS danap FROM account, player WHERE account.id = player.account_id AND account.danap >= 100000 ORDER BY account.danap DESC LIMIT 10;";
    public static final String TOP_DUA_QUOC_VUONG = "SELECT name, gender, items_body, \n"
            + "  account.id as accountId, \n"
            + "  player.name, \n"
            + "	CAST(\n"
            + "    REPLACE(\n"
            + "      SUBSTRING_INDEX(\n"
            + "        SUBSTRING_INDEX(\n"
            + "          CONCAT(\n"
            + "            '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "            SUBSTRING_INDEX(\n"
            + "              SUBSTRING_INDEX(player.items_bag, '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', -1), \n"
            + "              ']', \n"
            + "              1\n"
            + "            )\n"
            + "          ), \n"
            + "          ',', \n"
            + "          2\n"
            + "        ), \n"
            + "        ']', \n"
            + "        1\n"
            + "      ), \n"
            + "      '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "      ''\n"
            + "    ) as unsigned\n"
            + "  ) as tv_hanhtrang, \n"
            + "	CAST(\n"
            + "    REPLACE(\n"
            + "      SUBSTRING_INDEX(\n"
            + "        SUBSTRING_INDEX(\n"
            + "          CONCAT(\n"
            + "            '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "            SUBSTRING_INDEX(\n"
            + "              SUBSTRING_INDEX(player.items_box, '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', -1), \n"
            + "              ']', \n"
            + "              1\n"
            + "            )\n"
            + "          ), \n"
            + "          ',', \n"
            + "          2\n"
            + "        ), \n"
            + "        ']', \n"
            + "        1\n"
            + "      ), \n"
            + "      '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "      ''\n"
            + "    ) as unsigned\n"
            + "  ) as tv_ruong,\n"
            + "  CAST(\n"
            + "    REPLACE(\n"
            + "      SUBSTRING_INDEX(\n"
            + "        SUBSTRING_INDEX(\n"
            + "          CONCAT(\n"
            + "            '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "            SUBSTRING_INDEX(\n"
            + "              SUBSTRING_INDEX(player.items_bag, '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', -1), \n"
            + "              ']', \n"
            + "              1\n"
            + "            )\n"
            + "          ), \n"
            + "          ',', \n"
            + "          2\n"
            + "        ), \n"
            + "        ']', \n"
            + "        1\n"
            + "      ), \n"
            + "      '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "      ''\n"
            + "    ) as unsigned\n"
            + "  ) + CAST(\n"
            + "    REPLACE(\n"
            + "      SUBSTRING_INDEX(\n"
            + "        SUBSTRING_INDEX(\n"
            + "          CONCAT(\n"
            + "            '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "            SUBSTRING_INDEX(\n"
            + "              SUBSTRING_INDEX(player.items_box, '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', -1), \n"
            + "              ']', \n"
            + "              1\n"
            + "            )\n"
            + "          ), \n"
            + "          ',', \n"
            + "          2\n"
            + "        ), \n"
            + "        ']', \n"
            + "        1\n"
            + "      ), \n"
            + "      '[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",', \n"
            + "      ''\n"
            + "    ) as unsigned\n"
            + "  ) AS thoi_vang \n"
            + "from \n"
            + "  player \n"
            + "  inner join account on account.id = player.account_id \n"
            + "where \n"
            + "  (\n"
            + "    player.items_box like '%\"[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",%' \n"
            + "    or player.items_bag like '%\"[" + ConstTranhNgocNamek.ITEM_TRANH_NGOC + ",%'\n"
            + " )\n"
            + "order by \n"
            + "  thoi_vang DESC \n"
            + "limit \n"
            + "  10;";
    public static final String TOP_SD = "SELECT name, gender, items_body, CAST( split_str(data_point,',',8) AS UNSIGNED) AS sd FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str(data_point,',',8)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_HP = "SELECT name, gender, items_body, CAST( split_str(data_point,',',6) AS UNSIGNED) AS hp FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str(data_point,',',6)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_KI = "SELECT name, gender, items_body, CAST( split_str(data_point,',',7) AS UNSIGNED) AS ki FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str(data_point,',',7)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_NV = "SELECT name, gender, items_body, CAST( JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) AS nv, CAST( JSON_EXTRACT(data_task, '$[1]') AS UNSIGNED) AS subnv, CAST( JSON_EXTRACT(data_task, '$[3]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY CAST( JSON_EXTRACT(data_task, '$[0]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[1]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[2]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_task, '$[3]') AS UNSIGNED) ASC LIMIT 20;";
    public static final String TOP_SK = "SELECT name, gender, items_body, CAST( split_str( data_inventory,',',5)  AS UNSIGNED) AS event FROM player INNER JOIN account ON account.id = player.account_id WHERE account.is_admin = 0 AND account.ban = 0 ORDER BY CAST( split_str( data_inventory,',',5)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String TOP_PVP = "SELECT name, gender, items_body, CAST( pointPvp AS UNSIGNED) AS pointPvp FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY CAST( pointPvp AS UNSIGNED) DESC LIMIT 100;";
    public static final String TOP_NHS = "SELECT name, gender, items_body, NguHanhSonPoint FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY NguHanhSonPoint DESC LIMIT 100;";
    public static final String TOP_DC = "SELECT name, gender, items_body, dicanh, juventus FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 ORDER BY dicanh DESC LIMIT 100;";
    public static final String TOP_VDST = "SELECT name, gender, items_body, CAST( JSON_EXTRACT(vodaisinhtu, '$[2]') AS UNSIGNED) AS lasttime, CAST( JSON_EXTRACT(vodaisinhtu, '$[3]') AS UNSIGNED) AS time FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 AND CAST( JSON_EXTRACT(vodaisinhtu, '$[3]') AS UNSIGNED) > 0 ORDER BY CAST( JSON_EXTRACT(vodaisinhtu, '$[3]') AS UNSIGNED) DESC LIMIT 20;";
    public static final String TOP_WHIS = "SELECT name, player.id, gender, items_body, CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) AS top, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) AS time, CAST( JSON_EXTRACT(data_luyentap, '$[7]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 AND CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 0 ORDER BY CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) ASC LIMIT 20;";
    public static final String TOP_3_WHIS = "SELECT name, id, gender, items_body, CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) AS top, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) AS time, CAST( JSON_EXTRACT(data_luyentap, '$[7]') AS UNSIGNED) AS lasttime FROM player INNER JOIN account ON account.id = player.account_id WHERE account.ban = 0 AND CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) > 0 ORDER BY CAST( JSON_EXTRACT(data_luyentap, '$[5]') AS UNSIGNED) DESC, CAST( JSON_EXTRACT(data_luyentap, '$[6]') AS UNSIGNED) ASC LIMIT 3;";

}
