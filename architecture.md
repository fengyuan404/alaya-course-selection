# Alaya大学选课系统 - 技术架构文档
## 1. 技术栈选型
| 技术类别 | 选型方案 | 版本要求 | 选型理由 |
| --- | --- | --- | --- |
| 后端框架 | Spring Boot | 3.1+ | 简化Spring配置，支持快速开发，适合企业级Web应用 |
| 数据访问框架 | Spring Data JPA | 随Spring Boot 3.1+ | 简化数据库操作，支持自动化CRUD，减少重复代码 |
| 安全框架 | Spring Security | 随Spring Boot 3.1+ | 提供认证/授权功能，支持密码加密、CSRF防护，保障系统安全 |
| 数据库 | MySQL | 8.0+ | 开源稳定，社区支持广，适合存储结构化数据（用户/课程/选课记录等） |
| 构建工具 | Maven | 3.6+ | 管理项目依赖，支持一键编译/打包，团队协作时依赖版本统一 |
| IDE | IntelliJ IDEA | 2022.3+ | 对Spring Boot支持友好，调试功能强，提高开发效率 |
| 版本控制 | Git + GitHub | 无特定版本 | 分布式版本控制，支持多人协作，GitHub便于远程仓库管理 |
| 前端框架 | Thymeleaf | 3.1+ | 与Spring Boot无缝集成，无需单独部署前端，适合快速开发后端主导的Web应用 |


## 2. 系统分层架构设计
### 2.1 架构分层及职责
| 架构分层 | 包路径（示例） | 核心职责 | 关键组件/类 |
| --- | --- | --- | --- |
| 控制层（Controller） | com.alaya.controller | 1. 接收前端请求（如`/api/courses`获取课程列表）；   2. 调用服务层方法处理业务；   3. 返回响应结果（如JSON数据、页面跳转） | 课程控制器（CourseController）、用户控制器（AuthController） |
| 服务层（Service） | com.alaya.service | 1. 实现核心业务逻辑（如选课冲突检测、成绩计算）；   2. 调用数据访问层操作数据库；   3. 处理事务（如选课操作需保证原子性） | 选课服务（EnrollmentService）、课程服务（CourseService） |
| 数据访问层（Repository） | com.alaya.repository | 1. 定义数据库操作接口（如查询课程、保存选课记录）；   2. 借助Spring Data JPA自动生成SQL语句 | 课程仓库（CourseRepository）、用户仓库（UserRepository） |
| 实体层（Entity） | com.alaya.entity | 1. 映射数据库表结构（如`User`类对应`users`表）；   2. 定义实体属性及关联关系（如课程与教师的多对一关系） | 用户实体（User）、课程实体（Course）、选课实体（Enrollment） |
| 配置层（Config） | com.alaya.config | 1. 配置系统参数（如Spring Security、数据库连接）；   2. 定义Bean（如缓存配置、异常处理器） | 安全配置（SecurityConfig）、缓存配置（CacheConfig） |
| 工具层（Util） | com.alaya.util | 1. 提供通用工具方法（如时间格式转换、数据校验）；   2. 封装公共逻辑（如响应结果统一格式） | 响应工具（ResponseUtil）、时间工具（DateUtil） |


### 2.2 架构交互流程
以“学生选课”为例，各层交互流程：

1. 前端 → 控制层：学生点击“选课”，发送`POST /api/enroll`请求，携带学生ID和课程ID；
2. 控制层 → 服务层：AuthController调用EnrollmentService的`enrollStudent(studentId, courseId)`方法；
3. 服务层 → 数据访问层：EnrollmentService调用CourseRepository查询课程信息、EnrollmentRepository保存选课记录；
4. 数据访问层 → 数据库：执行SQL（如`SELECT * FROM courses WHERE id=?`查询课程、`INSERT INTO enrollments(...)`保存记录）；
5. 反向响应：数据库→数据访问层→服务层（处理结果）→控制层（返回“选课成功”）→前端（更新课表页面）。

## 3. 数据库模型设计（ER图核心关系）
### 3.1 核心数据表及字段
| 表名 | 核心字段 | 关联关系 |
| --- | --- | --- |
| users（用户表） | id（主键）、username（用户名，唯一）、password（加密存储）、email（邮箱，唯一）、role（角色：STUDENT/TEACHER/ADMIN）、created_at（创建时间） | 1. 作为教师：一对多关联courses表（一个教师可创建多门课程）；   2. 作为学生：一对多关联enrollments表（一个学生可选多门课程） |
| courses（课程表） | id（主键）、name（课程名）、description（描述）、credit（学分）、capacity（容量）、schedule（时间安排）、teacher_id（外键，关联users表）、created_at（创建时间） | 1. 多对一关联users表（多门课程属于一个教师）；   2. 一对多关联enrollments表（一门课程可被多个学生选择） |
| enrollments（选课表） | id（主键）、student_id（外键，关联users表）、course_id（外键，关联courses表）、enrolled_at（选课时间）、status（状态：ACTIVE/CANCELLED） | 1. 多对一关联users表（多个选课记录属于一个学生）；   2. 多对一关联courses表（多个选课记录属于一门课程） |
| grades（成绩表） | id（主键）、enrollment_id（外键，关联enrollments表）、score（成绩）、comments（评语）、recorded_at（录入时间） | 一对一关联enrollments表（一条选课记录对应一条成绩记录） |


### 3.2 ER图简化表示
<font style="color:rgba(0, 0, 0, 0.85);">users (1) ──1:N─→ courses (N) （一个教师创建多门课程）</font>

<font style="color:rgba(0, 0, 0, 0.85);">users (1) ──1:N─→ enrollments (N) （一个学生有多个选课记录）</font>

<font style="color:rgba(0, 0, 0, 0.85);">courses (1) ──1:N─→ enrollments (N) （一门课程有多个选课记录）</font>

<font style="color:rgba(0, 0, 0, 0.85);">enrollments (1) ──1:1─→ grades (1) （一条选课记录对应一条成绩）</font>

## <font style="color:rgba(0, 0, 0, 0.85);">4. 项目目录结构</font>
alaya-course-selection/  # 项目根目录  
├── src/  
│   ├── main/  
│   │   ├── java/  
│   │   │   └── com/  
│   │   │       └── alaya/  
│   │   │           ├── AlayaCourseSelectionApplication.java  # 项目启动类  
│   │   │           ├── controller/  # 控制层  
│   │   │           ├── service/     # 服务层  
│   │   │           ├── repository/  # 数据访问层  
│   │   │           ├── entity/      # 实体层  
│   │   │           ├── config/      # 配置层  
│   │   │           └── util/        # 工具层  
│   │   └── resources/  
│   │       ├── application.yml      # 主配置文件（指定默认环境）  
│   │       ├── application-dev.yml  # 开发环境配置（数据库连接等）  
│   │       └── templates/           # 前端页面（Thymeleaf模板，迭代0暂空）  
│   └── test/  # 测试层（迭代0暂空）  
├── pom.xml  # Maven依赖配置  
├── .gitignore  # Git忽略文件  
├── requirements.md  # 项目需求文档  
└── architecture.md  # 技术架构文档

