# AccountManager
基于Java和Mysql实现的账号管理系统，支持本地加密存储和增删改查等功能
# AccountManager

## 项目介绍
- **项目背景与目标**：本项目是一个基于 Java Swing 和 MySQL 的桌面端账号管理系统，用于安全存储、管理和查询各类账号密码信息，解决用户在多平台账号管理中易遗忘、易泄露的痛点。
- **核心功能与亮点**：
  - 账号信息的增删改查
  - 本地数据加密存储
  - 支持按平台/分类筛选账号
  - 简洁直观的图形化操作界面
- **技术栈**：Java 8+、Swing、MySQL、JDBC、MVC 架构

## 环境配置
- **操作系统要求**：Windows / macOS / Linux（支持 JRE 运行环境）
- **依赖软件版本**：
  - JDK 1.8 或更高版本
  - MySQL 5.7+ 或 8.0+
  - 无额外第三方框架依赖
- **环境变量配置**：
  - 配置 `JAVA_HOME` 指向 JDK 安装目录
  - 将 `%JAVA_HOME%\bin` 添加到系统 `PATH`
  - 配置 MySQL 服务，创建数据库 `account_manager`

## 运行步骤
1. 克隆仓库到本地
   ```bash
   git clone https://github.com/1yu-321/AccountManager.git
