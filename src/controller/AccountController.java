package controller;

import dao.AccountDao;
import entity.Account;
import view.AccountView;

import java.awt.event.ActionEvent;  //导入Swing事件相关类
import java.awt.event.ActionListener;
import java.util.ArrayList;  //导入集合类
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;  //导入Swing弹窗类

/**
 * 控制器类：连接 View 和 Dao
 * 负责业务逻辑转发、数据格式转换（List -> Vector）
 */
public class AccountController {

    private AccountView view;
    private AccountDao dao;

    public AccountController(AccountView view, AccountDao dao) {
        this.view = view;
        this.dao = dao;

        // 1. 绑定 View 的监听器
        initListeners();
        
        // 2. 初始化分类列表并加载一次数据
        refreshCategories();  //刷新分类选项：合并系统内置与数据库已有分类
        refreshTableData();   //从数据库读取并刷新 UI 表格
        
        // 3. 显示界面
        this.view.setVisible(true);
    }

    private void initListeners() {
        // 绑定保存按钮逻辑
        view.addSaveListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSave();
            }
        });

        // 绑定删除按钮逻辑
        view.addDeleteListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        // 绑定查询/刷新按钮逻辑
        view.addQueryListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshCategories();
                refreshTableData();
            }
        });

        // 绑定分类管理按钮逻辑
        view.addManageCategoryListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleManageCategories();
            }
        });

        // 绑定按用户名查询按钮逻辑
        view.addSearchByUsernameListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearchByUsername();
            }
        });

        // 分类筛选变更时自动刷新
        view.addFilterCategoryChangeListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshTableData();
            }
        });
    }

    /**
     * 处理保存逻辑
     */
    private void handleSave() {
        // 从 View 获取数据
        String platform = view.getPlatformName();
        String username = view.getUserName();
        String password = view.getPassword();
        String category = view.getCategory();
        int level = view.getLevel();
        String remark = view.getRemark();

        if (platform.isEmpty() || username.isEmpty() || password.isEmpty()) {  //校验失败，提示用户（调用View的弹窗方法）
            view.showMessage("请填写完整的平台、账号和密码信息！");
            return;
        }

        // 封装实体
        Account acc = new Account(platform, username, password, category, level, remark);

        // 调用 DAO 保存 (DAO 内部会自动根据 level 加密)
        if (dao.addAccount(acc)) {   //保存成功：提示用户、清空输入框、刷新表格
            view.showMessage("保存成功！");
            view.clearInputs();
            refreshTableData();
        } else {
            view.showMessage("保存失败，请检查数据库连接。");
        }
    }

    /**
     * 处理删除逻辑
     */
    private void handleDelete() {
        int id = view.getSelectedRowId();  //从View层获取用户选中的表格行ID（主键）
        if (id == -1) {
            view.showMessage("请先在表格中选择要删除的行。");
            return;
        }

        if (dao.deleteAccount(id)) {
            view.showMessage("删除成功！");
            refreshTableData();
        } else {
            view.showMessage("删除失败。");
        }
    }

    /**
     * 核心逻辑：从数据库读取并刷新 UI 表格
     */
    private void refreshTableData() {
        // 1. 根据筛选分类获取数据 (DAO 内部会自动解密)
        String filterCategory = view.getFilterCategory();
        List<Account> accounts = (filterCategory == null) ? dao.findAll() : dao.findByCategory(filterCategory); 
        // - 无筛选（null）：查询所有账号
        // - 有筛选：查询指定分类的账号（DAO内部会解密密码）

        // 2. 将 List<Account> 转换为 View 需要的 Vector<Vector<Object>>
        Vector<Vector<Object>> data = new Vector<>();
        for (Account acc : accounts) {
            Vector<Object> row = new Vector<>();
            row.add(acc.getId());
            row.add(acc.getPlatformName());
            row.add(acc.getUsername());
            
            // 针对不同级别，可以在此处决定是否在 UI 显示掩码（例如：****）
            // 目前 DAO 已经解密，所以这里显示的是明文
            row.add(acc.getPassword()); 
            
            row.add(acc.getCategory());
            row.add(acc.getLevel());
            row.add(acc.getRemark());
            data.add(row);
        }

        // 3. 更新视图
        view.updateTable(data);
    }

    /**
     * 处理按用户名查询功能（支持模糊搜索），并结合当前分类筛选
     */
    private void handleSearchByUsername() {
        String keyword = view.getUserName();
        if (keyword == null || keyword.trim().isEmpty()) {
            view.showMessage("请输入要查询的用户名关键词。");
            return;
        }

        String filterCategory = view.getFilterCategory();
        List<Account> accounts = (filterCategory == null)
                ? dao.findByUsernameLike(keyword.trim())
                : dao.findByUsernameLikeAndCategory(keyword.trim(), filterCategory);

        Vector<Vector<Object>> data = new Vector<>();
        for (Account acc : accounts) {
            Vector<Object> row = new Vector<>();
            row.add(acc.getId());
            row.add(acc.getPlatformName());
            row.add(acc.getUsername());
            row.add(acc.getPassword());
            row.add(acc.getCategory());
            row.add(acc.getLevel());
            row.add(acc.getRemark());
            data.add(row);
        }
        view.updateTable(data);
    }

    /**
     * 刷新分类选项：合并系统内置与数据库已有分类
     */
    private void refreshCategories() {
        List<String> defaults = List.of("社交网站", "游戏账号", "邮箱账号", "银行卡", "办公系统", "其他");
        List<String> fromDb = dao.findDistinctCategories();
        List<String> merged = new ArrayList<>();  //合并分类：去重，先加内置，再加数据库的自定义分类
        for (String d : defaults) if (!merged.contains(d)) merged.add(d);
        for (String c : fromDb) if (!merged.contains(c)) merged.add(c);
        view.setCategoryOptions(merged);
    }

    /**
     * 简单的分类管理：在 UI 中添加或移除分类选项（不单独持久化到 DB）
     */
    private void handleManageCategories() {
        // 当前分类来源于 DB 和内置的合并
        List<String> current = new ArrayList<>();
        current.addAll(List.of("社交网站", "游戏账号", "邮箱账号", "银行卡", "办公系统", "其他"));
        for (String c : dao.findDistinctCategories()) {
            if (!current.contains(c)) current.add(c);
        }

        String[] options = {"新增分类", "删除分类", "取消"};
        int sel = JOptionPane.showOptionDialog(
                view,                           //父窗口
                "请选择分类管理操作",             //提示文字
                "分类管理",                      //标题
                JOptionPane.DEFAULT_OPTION,     //选项类型
                JOptionPane.PLAIN_MESSAGE,      //消息类型
                null,                           //图标（默认）
                options,                        //选项数组
                options[0]                      //默认选中项
        );

        if (sel == 0) { // 新增
            String name = JOptionPane.showInputDialog(view, "输入新的分类名称：", "新增分类", JOptionPane.PLAIN_MESSAGE);
            if (name != null) {
                name = name.trim();
                if (name.isEmpty()) {
                    view.showMessage("分类名称不能为空。");
                    return;
                }
                if (current.contains(name)) {
                    view.showMessage("已存在同名分类。");
                    return;
                }
                current.add(name);
                view.setCategoryOptions(current);
                view.showMessage("已添加分类：" + name + "（提示：分类将随账号保存而持久化）");  //（仅新增账号时选这个分类，才会存到数据库）
            }
        } else if (sel == 1) { // 删除
            if (current.isEmpty()) {
                view.showMessage("暂无可删除的分类。");
                return;
            }
            Object choice = JOptionPane.showInputDialog(
                    view,
                    "选择要删除的分类：",
                    "删除分类",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    current.toArray(),
                    null
            );
            if (choice != null) {  //移除选中的分类，更新下拉框
                String toRemove = choice.toString();
                current.remove(toRemove);
                view.setCategoryOptions(current);
                view.showMessage("已从选项中移除分类：" + toRemove + "（提示：已存在账号的分类不会被删除）");
            }
        }// sel==2：取消操作，无逻辑
    }
}