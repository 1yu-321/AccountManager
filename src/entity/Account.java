package entity;

import java.io.Serializable;

/**
 * 账号信息实体类
 * 对应数据库中的 accounts 表
 */
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    // 数据库ID (主键)
    private Integer id;

    // 平台名称 (如：QQ, 招商银行, Steam)
    private String platformName;

    // 账号/用户名
    private String username;

    // 密码 (注意：根据 level 级别，这里可能存储的是加密后的密文)
    private String password;

    // 分类 (需求1：邮箱、游戏、社交、银行卡、办公等)
    private String category;

    // 安全级别 (需求2：1-普通, 2-敏感, 3-重要, 4-非常重要)
    private Integer level;

    // 备注信息
    private String remark;

    // --- 构造方法 ---

    // 1. 无参构造器 (必须要有)
    public Account() {
    }

    // 2. 全参构造器 (用于查询后封装对象)
    public Account(Integer id, String platformName, String username, String password, String category, Integer level, String remark) {
        this.id = id;
        this.platformName = platformName;
        this.username = username;
        this.password = password;
        this.category = category;
        this.level = level;
        this.remark = remark;
    }

    // 3. 不带ID的构造器 (用于新增数据，ID由数据库自动生成)
    public Account(String platformName, String username, String password, String category, Integer level, String remark) {
        this.platformName = platformName;
        this.username = username;
        this.password = password;
        this.category = category;
        this.level = level;
        this.remark = remark;
    }

    // --- Getter 和 Setter 方法 ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // --- toString 方法 (方便打印调试) ---

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", 平台='" + platformName + '\'' +
                ", 账号='" + username + '\'' +
                ", 分类='" + category + '\'' +
                ", 级别=" + level +
                '}';
        // 出于安全考虑，toString通常不打印密码
    }
}