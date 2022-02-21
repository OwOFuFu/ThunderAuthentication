package com.md_4.thunderauthentication.functions;

import com.md_4.thunderauthentication.backend.core.MojangAPI;
import com.md_4.thunderauthentication.backend.core.UUIDFetcher;
import org.bukkit.entity.Player;

import java.sql.Statement;
import java.util.UUID;

import static com.md_4.thunderauthentication.Main.SQL;

@SuppressWarnings("all")
public class TableFunction {

    public static void createTable(Player player, UUID uuid) throws Exception {

        Statement st = SQL.getConnection().createStatement();

        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + player.getName().toString()
                + "(name VARCHAR(20), uuid VARCHAR(36), offlineuuid VARCHAR(36), premium VARCHAR(2))";

        Statement stmt = SQL.getConnection().createStatement();
        stmt.execute(sqlCreate);
        stmt.executeUpdate("INSERT INTO `"+ player.getName().toString() +"`(`name`, `uuid`, `offlineuuid` , `premium`) " + "VALUES('" + player.getName().toString() + "','" + MojangAPI.getUUID(player.getName()).toString() + "', '" + UUIDFetcher.getOFFUUID(player.getName()).toString() + "','SI')");
    }
}
