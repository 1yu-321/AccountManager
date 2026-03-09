package dao;

import entity.Account;
import utils.DbConfig;
import utils.SecurityUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问对象：负责 Account 实体在 SQLite 中的 CRUD 操作
 * CRUD：Create(新增)、Read(查询)、Update(修改)、Delete(删除)
 */
public class AccountDao {

    /**
     * 新增账号信息 (需求 1 & 3)
     * 在写入数据库前，会根据安全级別自动加密密码
     */
    public boolean addAccount(Account account) {
        String sql = "INSERT INTO accounts(platform_name, username, password, category, level, remark) VALUES(?,?,?,?,?,?)";
        
        // 根据级別加密密码
        String encryptedPwd = SecurityUtils.encryptByLevel(account.getPassword(), account.getLevel());

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, account.getPlatformName());   //给SQL占位符赋值（索引从1开始，和VALUES后的?一一对应）
            pstmt.setString(2, account.getUsername());
            pstmt.setString(3, encryptedPwd);
            pstmt.setString(4, account.getCategory());
            pstmt.setInt(5, account.getLevel());
            pstmt.setString(6, account.getRemark());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 刪除账号信息 (需求 1)
     */
    public boolean deleteAccount(int id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 修改账号信息 (需求 1)
     */
    public boolean updateAccount(Account account) {
        String sql = "UPDATE accounts SET platform_name=?, username=?, password=?, category=?, level=?, remark=? WHERE id=?";
        
        // 更新时同样需要考虑加密
        String encryptedPwd = SecurityUtils.encryptByLevel(account.getPassword(), account.getLevel());

        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, account.getPlatformName());
            pstmt.setString(2, account.getUsername());
            pstmt.setString(3, encryptedPwd);
            pstmt.setString(4, account.getCategory());
            pstmt.setInt(5, account.getLevel());
            pstmt.setString(6, account.getRemark());
            pstmt.setInt(7, account.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询所有账号 (需求 4)
     * 查询结果会自动根据级別进行解密，方便用户查看
     */
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();   //创建空集合：用于存储查询结果
        String sql = "SELECT * FROM accounts";
        
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据分类查询 (需求 1)
     */
    public List<Account> findByCategory(String category) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE category = ?";
        
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, category);   //给占位符赋值：要查询的分类名称
            ResultSet rs = pstmt.executeQuery();   //执行查询，获取结果集
            while (rs.next()) {
                list.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据用户名模糊查询（LIKE）
     */
    public List<Account> findByUsernameLike(String usernamePart) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE username LIKE ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + usernamePart + "%");  //拼接模糊查询条件：%usernamePart% 表示“包含usernamePart的任意位置”
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据用户名模糊查询并按分类过滤
     */
    public List<Account> findByUsernameLikeAndCategory(String usernamePart, String category) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM accounts WHERE username LIKE ? AND category = ?";
        try (Connection conn = DbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + usernamePart + "%");
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(extractAccountFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询系统中已有的所有不同分类（从 accounts 表中 DISTINCT 获取）
     */
    public List<String> findDistinctCategories() { //查询分类：用于筛选下拉框的选项
        List<String> cats = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM accounts ORDER BY category";
        try (Connection conn = DbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String c = rs.getString(1);
                if (c != null && !c.trim().isEmpty()) {
                    cats.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cats;
    }

    /**
     * 辅助方法：将 ResultSet 转化为 Account 对象并自动解密
     */
    private Account extractAccountFromResultSet(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setId(rs.getInt("id"));
        acc.setPlatformName(rs.getString("platform_name"));
        acc.setUsername(rs.getString("username"));
        acc.setCategory(rs.getString("category"));
        acc.setLevel(rs.getInt("level"));
        acc.setRemark(rs.getString("remark"));
        
        // 核心：从数据库读取密文后，根据级别解密回明文，供 View 层显示
        String dbPwd = rs.getString("password");
        String clearPwd = SecurityUtils.decryptByLevel(dbPwd, acc.getLevel());
        acc.setPassword(clearPwd);
        
        return acc;
    }
}