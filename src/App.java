
import dao.AccountDao;
import view.AccountView;
import controller.AccountController;
import utils.DbConfig;

import javax.swing.JOptionPane;  //导入Swing的弹窗工具包：用于创建密码输入框、提示框

public class App {
    // 预先设定的主密码（实际项目中应该加密存储，这里简化用明文）
    private static final String MASTER_PASSWORD = "123456";

    public static void main(String[] args) {
        // 步骤1：先校验主密码
        String inputPwd = JOptionPane.showInputDialog(null, "请输入主密码:", "主密码验证", JOptionPane.PLAIN_MESSAGE);   //JOptionPane.PLAIN_MESSAGE：弹窗样式（普通文本输入框）
        
        // 校验逻辑：密码为空 或 输入错误
        if (inputPwd == null || !inputPwd.equals(MASTER_PASSWORD)) {
            JOptionPane.showMessageDialog(null, "主密码错误，系统将退出！", "验证失败", JOptionPane.ERROR_MESSAGE);   //JOptionPane.ERROR_MESSAGE：弹窗样式（错误提示，带红色叉号）
            System.exit(0); // 退出程序
        }

        // 步骤2：密码正确，继续原流程
        DbConfig.initDatabase();
        AccountView view = new AccountView();  //创建界面对象
        AccountDao dao = new AccountDao();   //创建操作数据库的方法
        new AccountController(view, dao);  // 创建控制器对象，并把视图、数据层对象传给控制器
    }
}