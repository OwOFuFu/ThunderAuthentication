package com.md_4.thunderauthentication.sql;


import com.md_4.thunderauthentication.backend.core.engine.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@SuppressWarnings("all")
public class MySQL {

    private String host = Config.MySqlDbHost;
    private String port = Config.MySqlDbPort;
    private String database = Config.MySqlDbDbse;
    private String username = Config.MySqlDbUser;
    private String password = Config.MySqlDbPswd;

    private Connection connection;

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if(!isConnected()){
            Class.forName("com.mysql.cj.jdbc.Driver");



            connection = DriverManager.getConnection("jdbc:mysql://" +
                            host + ":" + port + "/" + database,
                    username, password);
        }
    }

    public void disconnect() {
        if(isConnected()){
            try {
                connection.close();
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
