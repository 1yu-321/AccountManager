package utils;

import javax.crypto.Cipher;  //导入AES加密核心类：Cipher（密码器，负责加解密操作）
import javax.crypto.spec.SecretKeySpec;  //导入AES密钥规范类：SecretKeySpec（用于生成AES加密的密钥）
import java.nio.charset.StandardCharsets; //导入字符集规范：StandardCharsets.UTF_8（统一字符编码，避免乱码）
import java.util.Base64;  //导入Base64编码工具：用于将加密后的字节数组转成可存储的字符串

/**
 * 安全工具类：负责账号密码的加解密
 */
public class SecurityUtils {
 
    // AES 加密算法需要的密钥 (16位字符串，代表128位加密)
    // 注意：实际应用中，这个密钥应保存在安全的地方，或由用户主密码生成
    private static final String SECRET_KEY = "MySecretKey_2024"; 
    private static final String ALGORITHM = "AES";

    /**
     * 根据账号级别进行加密
     * @param password 原始明文密码
     * @param level 级别 (1-4)
     * @return 加密后的字符串（如果是1-2级则返回原文或简单处理）
     */
    public static String encryptByLevel(String password, int level) {
        if (level >= 3) {
            // 3级和4级：执行 AES 强加密
            return encryptAES(password);
        }
        // 1级和2级：返回原文（或你可以选择进行简单的 Base64 编码）
        return password;
    }

    /**
     * 根据账号级别进行解密
     * @param encryptedData 数据库中的密文
     * @param level 级别 (1-4)
     * @return 解密后的明文
     */
    public static String decryptByLevel(String encryptedData, int level) {
        if (level >= 3) {
            return decryptAES(encryptedData);
        }
        return encryptedData;
    }

    // --- AES 核心加密逻辑 ---

    private static String encryptAES(String data) {   //AES加密核心逻辑（对外隐藏，只供内部调用）
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);  //生成AES密钥对象：将字符串密钥转成UTF-8字节数组，指定算法为AES
            Cipher cipher = Cipher.getInstance(ALGORITHM);  //获取AES密码器实例：Cipher是JCE的核心类，负责执行加解密
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);  // 初始化密码器为“加密模式”，传入密钥
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)); //将明文字节数组转为密文字节数组
            // 将字节数组转换为 Base64 字符串存储在数据库
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- AES 核心解密逻辑 ---

    private static String decryptAES(String encryptedData) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);  //生成和加密相同的AES密钥对象（对称加密，密钥必须一致）
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);  //初始化密码器为“解密模式”，传入密钥
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);  //Base64解码：将数据库中的加密字符串转回字节数组
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);  //执行解密：将加密字节数组解密成明文字节数组
            return new String(decryptedBytes, StandardCharsets.UTF_8); //转成字符串：将明文字节数组按UTF-8转成字符串（密码明文）
        } catch (Exception e) {
            System.err.println("解密失败：可能是数据未加密或密钥错误");
            return encryptedData; // 失败时返回原文，防止程序崩溃
        }
    }
}