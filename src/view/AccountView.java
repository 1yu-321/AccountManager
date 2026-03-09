package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * 纯 UI 类：只负责界面展示，不包含任何业务逻辑或数据库访问。
 * 遵循严格的 Getter/Setter 暴露模式。
 */
public class AccountView extends JFrame {

    // --- 组件定义 ---
    private JTextField platformInput;
    private JTextField usernameInput;
    private JPasswordField passwordInput;
    private JComboBox<String> categoryInput;
    private JComboBox<Integer> levelInput;
    private JTextField remarkInput;

    private JButton addButton;
    private JButton deleteButton;
    private JButton queryButton;
    private JButton manageCategoryButton;
    private JButton searchUsernameButton;

    private JTable dataTable;
    private DefaultTableModel tableModel;

    // 分类筛选组件
    private JComboBox<String> filterCategoryInput;

    /**
     * 无参构造函数
     */
    public AccountView() {
        initComponents();
    }

    private void initComponents() {
        setTitle("个人账号密码管理系统");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 关闭窗口时退出程序
        setLocationRelativeTo(null);    // 窗口居中显示
        setLayout(new BorderLayout(10, 10));  //边框布局（组件间距10px）

        // --- 1. 顶部：输入面板 ---
        JPanel inputPanel = new JPanel(new GridLayout(2, 6, 5, 5));  // 网格布局：2行6列，组件间距5px
        inputPanel.setBorder(BorderFactory.createTitledBorder("账号信息录入"));

        platformInput = new JTextField();
        usernameInput = new JTextField();
        passwordInput = new JPasswordField();
        
        // 需求1：分类定义
        categoryInput = new JComboBox<>(new String[]{"社交网站", "游戏账号", "邮箱账号", "银行卡", "办公系统", "其他"});
        
        // 需求2：分级定义 (1-4级)
        levelInput = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        
        remarkInput = new JTextField();

        inputPanel.add(new JLabel(" 平台名称:"));
        inputPanel.add(platformInput);
        inputPanel.add(new JLabel(" 账号:"));
        inputPanel.add(usernameInput);
        inputPanel.add(new JLabel(" 密码:"));
        inputPanel.add(passwordInput);
        inputPanel.add(new JLabel(" 分类:"));
        inputPanel.add(categoryInput);
        inputPanel.add(new JLabel(" 安全级别:"));
        inputPanel.add(levelInput);
        inputPanel.add(new JLabel(" 备注:"));
        inputPanel.add(remarkInput);

        // 顶部包装：加入分类筛选面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(BorderFactory.createTitledBorder("筛选"));   // 给筛选面板加边框和标题：“筛选”
        filterCategoryInput = new JComboBox<>(new String[]{"全部", "社交网站", "游戏账号", "邮箱账号", "银行卡", "办公系统", "其他"});
        filterPanel.add(new JLabel(" 筛选分类:"));
        filterPanel.add(filterCategoryInput);
        JPanel topPanel = new JPanel(new BorderLayout());  //创建顶部总面板，（整合输入面板和筛选面板）
        topPanel.add(inputPanel, BorderLayout.CENTER);   // 把输入面板添加到顶部总面板的“中间”位置
        topPanel.add(filterPanel, BorderLayout.SOUTH);   // 把筛选面板添加到顶部总面板的“下方”位置

        // --- 2. 中部：表格面板 ---
        String[] columnNames = {"ID", "平台", "账号", "密码", "分类", "级别", "备注"};
        tableModel = new DefaultTableModel(columnNames, 0) {  //创建表格模型：初始化0行数据，并重写isCellEditable方法让表格单元格不可编辑
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        dataTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(dataTable);   //创建滚动面板，把表格放进去（数据超过表格高度时自动显示滚动条）

        // --- 3. 底部：按钮面板 ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("保存账号");
        deleteButton = new JButton("删除选中");
        queryButton = new JButton("刷新列表");
        searchUsernameButton = new JButton("按用户名查询");
        manageCategoryButton = new JButton("分类管理");
        
        buttonPanel.add(manageCategoryButton);
        buttonPanel.add(searchUsernameButton);
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(queryButton);

        // --- 布局组装 ---
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    // --- 公共 Getter 方法 (供 Controller 读取数据) ---

    public String getPlatformName() { return platformInput.getText(); }
    public String getUserName() { return usernameInput.getText(); }
    public String getPassword() { return new String(passwordInput.getPassword()); }
    public String getCategory() { return (String) categoryInput.getSelectedItem(); }
    public Integer getLevel() { return (Integer) levelInput.getSelectedItem(); }
    public String getRemark() { return remarkInput.getText(); }

    /**
     * 获取筛选分类：当选择“全部”时返回 null，表示不过滤
     */
    public String getFilterCategory() {
        String sel = (String) filterCategoryInput.getSelectedItem();
        if (sel == null || "全部".equals(sel)) return null;  //Controller拿到null就知道不筛选
        return sel;  //否则返回选中的分类名称
    }

    public int getSelectedRowId() {
        int row = dataTable.getSelectedRow();
        if (row != -1) {
            return (int) tableModel.getValueAt(row, 0);  //从表格模型中获取选中行第0列（ID列）的值
        }
        return -1;
    }

    // --- 公共 Setter/Update 方法 (供 Controller 更新界面) ---

    /**
     * 更新表格数据
     * @param data 外部处理好的二维向量数据
     */
    public void updateTable(Vector<Vector<Object>> data) {  //二维向量结构:外层Vector(表格所有行),内层Vector(表格的一行),Object(一个单元格)
        tableModel.setRowCount(0); // 清空旧数据（比如之前显示的账号），避免新旧数据混在一起
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
        }
    }

    public void clearInputs() {  //清空所有输入框：方便用户下次输入
        platformInput.setText("");
        usernameInput.setText("");
        passwordInput.setText("");
        remarkInput.setText("");
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);  //弹出 JOptionPane 提示框，父窗口是当前View窗口，显示msg内容
    }

    /**
     * 设置分类选项（用于录入框和筛选框）
     */
    public void setCategoryOptions(List<String> categories) {
        // 录入下拉
        DefaultComboBoxModel<String> inputModel = new DefaultComboBoxModel<>();  //替换原有分类
        for (String c : categories) inputModel.addElement(c);  //遍历传入的分类列表，添加到下拉框模型中
        categoryInput.setModel(inputModel);

        // 筛选下拉（保留当前选中，若不存在则回退到“全部”）
        String prevSelected = (String) filterCategoryInput.getSelectedItem();
        DefaultComboBoxModel<String> filterModel = new DefaultComboBoxModel<>();
        filterModel.addElement("全部");
        for (String c : categories) filterModel.addElement(c);
        filterCategoryInput.setModel(filterModel);
        if (prevSelected != null) {
            boolean exists = "全部".equals(prevSelected);
            if (!exists) {
                for (int i = 0; i < filterModel.getSize(); i++) {
                    if (prevSelected.equals(filterModel.getElementAt(i))) { exists = true; break; }
                }
            }
            filterCategoryInput.setSelectedItem(exists ? prevSelected : "全部");  //如果“社交网站”还在，就继续选它；如果不在了，就选“全部”
        } else {
            filterCategoryInput.setSelectedItem("全部");  //用户之前没选任何选项，默认选“全部”（显示所有数据）
        }
    }

    // --- 监听器绑定方法 (供 Controller 注入逻辑) ---

    public void addSaveListener(ActionListener l) {  //l是listener（监听器）的首字母简写
        addButton.addActionListener(l);
    }

    public void addDeleteListener(ActionListener l) {
        deleteButton.addActionListener(l);
    }

    public void addQueryListener(ActionListener l) {
        queryButton.addActionListener(l);
    }

    public void addManageCategoryListener(ActionListener l) {
        manageCategoryButton.addActionListener(l);
    }

    public void addSearchByUsernameListener(ActionListener l) {
        searchUsernameButton.addActionListener(l);
    }

    /**
     * 分类筛选变更监听
     */
    public void addFilterCategoryChangeListener(ActionListener l) {
        filterCategoryInput.addActionListener(l);
    }
}