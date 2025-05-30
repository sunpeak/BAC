package orm;

import assistant.LogUtils;

import java.sql.*;

public class Req {


    private static final String dbUrl = "jdbc:sqlite:database.db";

    static {
        initDB();
        deleteAll();
    }

    private static void initDB() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try (Connection conn = DriverManager.getConnection(dbUrl); Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS reqs (messageId INTEGER PRIMARY KEY NOT NULL DEFAULT 0,reqBody BLOB NOT NULL DEFAULT '')";

            stmt.execute(createTableSQL);
            System.out.println("Table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOne(int messageId, byte[] reqBody) {
        String insertSQL = "INSERT INTO reqs (messageId,reqBody) VALUES (?,?)";
        try (Connection conn = DriverManager.getConnection(dbUrl); PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // 2. 插入数据
            pstmt.setInt(1, messageId);
            pstmt.setBytes(2, reqBody);
            pstmt.executeUpdate();
            LogUtils.log("insertOne: " + messageId + "," + new String(reqBody));
            System.out.println("Data inserted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static byte[] findOne(int messageId) {
        byte[] body = null;
        String selectSQL = "SELECT * FROM reqs where messageId=?";
        try (Connection conn = DriverManager.getConnection(dbUrl); PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setInt(1, messageId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    body = rs.getBytes("reqBody");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return body;
    }

    public static void deleteAll() {
        String selectSQL = "DELETE FROM reqs";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
