package utils;

import java.sql.Connection;  //导入JDBC核心接口：Connection（数据库连接），用于建立和数据库的会话
import java.sql.DriverManager;  //导入JDBC的驱动管理类：（核心类，用于获取数据库连接）
import java.sql.SQLException;  //导入JDBC的异常类
import java.sql.Statement;   // 导入JDBC的Statement接口：用于执行静态SQL语句（如建表、增删改查）

public class DbConfig {
    // 数据库文件路径。如果只写文件名，它会在项目根目录下生成。
    // 你也可以指定绝对路径，例如 "jdbc:sqlite:D:/data/pwd_manager.db"
    private static final String DB_URL = "jdbc:sqlite:sqi.db";

    /**
     * 获取数据库连接
     * @return Connection 对象
     */
    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 加载驱动 (在较新的JDBC版本中这一步其实是可选的，但为了兼容性保留)
            Class.forName("org.sqlite.JDBC");
            // 建立连接
            conn = DriverManager.getConnection(DB_URL);
            System.out.println("成功连接至 SQLite 数据库！");
        } catch (ClassNotFoundException e) {
            System.err.println("找不到 SQLite 驱动，请检查 jar 包是否导入！");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("连接数据库失败！");
            e.printStackTrace();
        }
        return conn;
    }

    /**
     * 初始化数据库表结构
     * 如果表不存在，则创建表
     */
    public static void initDatabase() {
        // SQL语句：创建账号信息表
        // 注意：这里我们定义了 category(分类) 和 level(分级) 字段来满足需求1和2
        String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "platform_name TEXT NOT NULL," +   // 平台名称 (如：Steam, QQ)
                "username TEXT NOT NULL," +        // 账号
                "password TEXT NOT NULL," +        // 密码 (后续需加密存储)
                "category TEXT," +                 // 分类 (邮箱、游戏、银行卡等)
                "level INTEGER," +                 // 安全级别 (1-4)
                "remark TEXT," +                   // 备注
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ");";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 执行建表语句
            stmt.execute(sql);
            System.out.println("数据库表结构检查/初始化完成。");
            
        } catch (SQLException e) {
            System.err.println("初始化数据库表失败：" + e.getMessage());
        }
    }
}