# Alaya大学选课系统
Alaya大学选课系统是基于Spring Boot的迭代式开发项目，用于实现学生选课、教师管课、管理员统筹的全流程线上化管理。



## 一、项目基础信息
+ **技术栈**：Spring Boot 3.5.7、Java 18、MySQL 8.1、Maven 3.6
+ **迭代阶段**：迭代0（项目初始化与基础框架搭建）
+ **核心目标**：完成需求分析、环境配置、基础项目骨架搭建，为后续迭代打基础



## 二、如何运行项目
### 1. 本地环境准备
1. 安装JDK 17（需配置环境变量`JAVA_HOME`）；
2. 安装MySQL 8.0，创建数据库（库名建议：`alaya_course_db`）；
3. 安装Maven 3.6+ 或使用IDE自带Maven；
4. 推荐IDE：IntelliJ IDEA 2022.3+ 或 VS Code。

### 2. 配置修改
1. 打开项目配置文件：`src/main/resources/application-dev.yml`；
2. 修改数据库连接信息（替换成你的本地MySQL配置）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/alaya_course_db?useSSL=false&serverTimezone=UTC
    username: 你的MySQL用户名（如root）
    password: 你的MySQL密码（如123456）
    driver-class-name: com.mysql.cj.jdbc.Driver
```

注：迭代0使用H2内存数据库配置，无需安装

### 3. 启动项目
1. 找到项目启动类：`com.alaya.AlayaCourseSelectionApplication.java`（带`@SpringBootApplication`注解的类）；
2. 右键点击 → 选择“Run”（IDEA）或“运行”（VS Code）；
3. 启动成功后，访问测试接口：`http://localhost:8080/hello`，应返回“Hello, Spring Boot”。






## 四、备注
+ 若启动报错，优先检查：JDK版本是否为17+、MySQL是否启动、数据库连接配置是否正确。

## 五、其他
+ 维护人列表：fengyuan404
+ 联系方式：YouFull@163.com

