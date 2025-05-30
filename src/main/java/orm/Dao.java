package orm;

import assistant.LogUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Dao {

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
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS rels (id INTEGER PRIMARY KEY AUTOINCREMENT,url TEXT NOT NULL DEFAULT '',auth TEXT NOT NULL DEFAULT '',param TEXT NOT NULL DEFAULT '',sensitive TEXT NOT NULL DEFAULT '',UNIQUE(url,auth,param))";

            stmt.execute(createTableSQL);
            System.out.println("Table created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertDB(UserRel userRel) {
        UserRel ur = findOne(userRel);
        if (ur != null) {
            if (ur.getSensitive().equals(userRel.getSensitive())) {
                return;
            }

            List<String> fields = new ArrayList<>();
            fields.addAll(Arrays.asList(ur.getSensitive().split("&")));
            fields.addAll(Arrays.asList(userRel.getSensitive().split("&")));
//          去重
            fields = fields.stream().distinct().collect(Collectors.toList());

            fields.sort(Comparator.comparing(String::toString));

            StringBuilder encodedUrl = new StringBuilder();
            for (String entry : fields) {
                if (encodedUrl.length() > 0) {
                    encodedUrl.append("&");
                }
                encodedUrl.append(entry);
            }
            userRel.setSensitive(encodedUrl.toString());

            LogUtils.log("updateDB: " + userRel);
            updateOne(userRel);
        } else {
            LogUtils.log("insertDB: " + userRel);
            insertOne(userRel);
        }
    }


    public static void updateOne(UserRel userRel) {
        String updateSQL = "update  rels set sensitive = ? where url=? and auth=? and param=?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            // 2. 插入数据
            pstmt.setString(1, userRel.getSensitive());
            pstmt.setString(2, userRel.getUrl());
            pstmt.setString(3, userRel.getAuth());
            pstmt.setString(4, userRel.getParam());
            pstmt.executeUpdate();
            System.out.println("Data update successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void insertOne(UserRel userRel) {
        String insertSQL = "INSERT INTO rels (url,auth,param,sensitive) VALUES (?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            // 2. 插入数据
            pstmt.setString(1, userRel.getUrl());
            pstmt.setString(2, userRel.getAuth());
            pstmt.setString(3, userRel.getParam());
            pstmt.setString(4, userRel.getSensitive());
            pstmt.executeUpdate();
            System.out.println("Data inserted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UserRel findOne(UserRel userRel) {
        UserRel ur = null;
        String selectSQL = "SELECT * FROM rels where url=? and auth=? and param=?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, userRel.getUrl());
            pstmt.setString(2, userRel.getAuth());
            pstmt.setString(3, userRel.getParam());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ur = new UserRel();
                    ur.setUrl(rs.getString("url"));
                    ur.setAuth(rs.getString("auth"));
                    ur.setParam(rs.getString("param"));
                    ur.setSensitive(rs.getString("sensitive"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ur;
    }

    public static void deleteAll() {
        String selectSQL = "DELETE FROM rels";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
