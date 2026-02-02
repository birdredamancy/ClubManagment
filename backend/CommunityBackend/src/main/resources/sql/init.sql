-- ============================================
-- 校园社区管理系统 - PostgreSQL 建表脚本
-- 使用前请先创建数据库: CREATE DATABASE community_db;
-- ============================================


-- ============================================
-- PostgreSQL 常用数据类型说明
-- ============================================
--
-- ┌──────────────────┬────────────────────────────────────────────────────┐
-- │ 数据类型          │ 说明                                              │
-- ├──────────────────┼────────────────────────────────────────────────────┤
-- │ BIGSERIAL        │ 自增长的大整数（1,2,3...自动递增），常用来做主键     │
-- │                  │ 等价于 BIGINT + 自动递增，不需要手动赋值             │
-- │ BIGINT           │ 大整数，范围 -9223372036854775808 ~ +92...08       │
-- │                  │ 对应 Java 的 Long 类型                             │
-- │ INT / INTEGER    │ 普通整数，范围 -2147483648 ~ 2147483647            │
-- │                  │ 对应 Java 的 Integer 类型                          │
-- │ SMALLINT         │ 小整数，范围 -32768 ~ 32767                        │
-- │                  │ 适合存状态值（0/1/2），节省空间                      │
-- │ VARCHAR(n)       │ 可变长度字符串，最多 n 个字符                       │
-- │                  │ 例：VARCHAR(50) 最多存 50 个字符                    │
-- │ TEXT             │ 不限长度的文本，适合存帖子正文、消息内容等长文本       │
-- │ BOOLEAN          │ 布尔值，只有 TRUE 或 FALSE 两个值                  │
-- │ TIMESTAMP        │ 时间戳，格式 '2026-02-03 14:30:00'                 │
-- │                  │ 对应 Java 的 LocalDateTime 类型                    │
-- └──────────────────┴────────────────────────────────────────────────────┘
--
-- ============================================
-- 常用约束关键字说明
-- ============================================
--
-- ┌──────────────────────────────┬──────────────────────────────────────────┐
-- │ 约束关键字                    │ 说明                                    │
-- ├──────────────────────────────┼──────────────────────────────────────────┤
-- │ PRIMARY KEY                  │ 主键，每张表只能有一个，值不能重复也不能  │
-- │                              │ 为空，用来唯一标识一行数据                │
-- │ NOT NULL                     │ 非空约束，该字段必须有值，不允许插入 NULL  │
-- │ UNIQUE                       │ 唯一约束，该字段的值不能和其他行重复       │
-- │                              │ 例：username UNIQUE 表示用户名不能重名     │
-- │ DEFAULT 值                   │ 默认值，插入数据时如果不给该字段赋值，     │
-- │                              │ 就自动使用这个默认值                       │
-- │                              │ 例：DEFAULT 0 表示默认是 0                │
-- │ REFERENCES 表名(字段)        │ 外键约束，表示该字段的值必须在另一张表中   │
-- │                              │ 存在。用来维护表与表之间的关联关系         │
-- │                              │ 例：REFERENCES users(user_id) 表示该字段  │
-- │                              │ 的值必须是 users 表 user_id 列中已有的值   │
-- │ UNIQUE(字段1, 字段2)         │ 联合唯一约束，两个字段的组合不能重复       │
-- │                              │ 例：UNIQUE(post_id, user_id) 表示同一个   │
-- │                              │ 用户对同一个帖子只能有一条记录（防重复点赞）│
-- └──────────────────────────────┴──────────────────────────────────────────┘
--
-- ============================================
-- CREATE INDEX 索引说明
-- ============================================
--
-- 索引就像一本书的"目录"，帮数据库更快地查找数据。
--
-- 没有索引：查询时数据库要逐行扫描整张表（全表扫描），数据越多越慢
-- 有索引：  数据库通过索引直接定位到目标行，速度大幅提升
--
-- 什么时候需要加索引？
--   1. WHERE 条件经常用到的字段    例: WHERE user_id = 123
--   2. ORDER BY 排序经常用到的字段  例: ORDER BY created_at DESC
--   3. 外键字段（关联查询用到的）   例: post_id, room_id
--
-- 注意：索引不是越多越好，每个索引都会占用磁盘空间，并且会拖慢 INSERT/UPDATE 速度。
--       只对「经常查询」的字段加索引即可。
--
-- 语法：CREATE INDEX 索引名 ON 表名(字段名);
--       CREATE INDEX 索引名 ON 表名(字段名 DESC);  -- DESC 表示按降序排列
--
-- ============================================
-- CURRENT_TIMESTAMP 说明
-- ============================================
--
-- CURRENT_TIMESTAMP 是 PostgreSQL 内置函数，返回当前时间。
-- 写成 DEFAULT CURRENT_TIMESTAMP 表示：插入数据时如果不指定该字段，
-- 数据库会自动填入当前时间，不需要你在 Java 代码里手动 set。
--


-- ============================================
-- 下面开始建表（共 9 张表）
-- 执行顺序不能乱，因为有外键依赖关系：
--   users -> user_profile, post, comment, post_like,
--            chat_room_member, chat_message
--   post  -> comment, post_like
--   chat_room -> chat_room_member, chat_message
-- ============================================


-- 1. 用户表（最基础的表，其他表都依赖它）
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,              -- 数据库自增主键，MyBatis-Plus 自动回填
    user_id     BIGINT UNIQUE NOT NULL,             -- 业务用户ID（雪花算法生成，暴露给前端用这个，而不是自增id）
    username    VARCHAR(50) UNIQUE NOT NULL,         -- 登录用户名，全局唯一
    nickname    VARCHAR(50),                         -- 昵称，可以重复
    password    VARCHAR(255) NOT NULL,               -- 密码（BCrypt加密后存储，原始密码6位加密后约60位，给255足够）
    avatar      VARCHAR(500),                        -- 头像图片的URL地址
    phone       VARCHAR(20),                         -- 手机号
    email       VARCHAR(100),                        -- 邮箱
    role        VARCHAR(20) DEFAULT 'USER',          -- 角色：USER(普通用户) / ADMIN(管理员) / MODERATOR(版主)
    status      SMALLINT DEFAULT 1,                  -- 账号状态：1正常 0禁用
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 注册时间（自动填充，不需要手动set）
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP  -- 最后更新时间
);

-- 2. 用户详情表（和 users 是一对一关系，存放用户的扩展信息）
--    为什么要分两张表？ -> users存登录相关信息（查询频繁），user_profile存个人资料（偶尔查询）
--    这样 users 表更轻量，登录验证时查询更快
CREATE TABLE user_profile (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT UNIQUE NOT NULL REFERENCES users(user_id), -- 外键关联到users表，UNIQUE保证一对一
    student_id  VARCHAR(30),                         -- 学号
    nickname    VARCHAR(50),                         -- 昵称
    gender      VARCHAR(10),                         -- 性别：MALE / FEMALE / OTHER
    bio         VARCHAR(500),                        -- 个人简介/签名
    major       VARCHAR(100),                        -- 专业
    grade       VARCHAR(20),                         -- 年级，例如 "2024"
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 校园墙帖子表（核心业务表）
CREATE TABLE post (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(user_id),  -- 发帖人（外键关联users表）
    content       TEXT NOT NULL,                     -- 帖子正文（TEXT类型不限长度）
    images        TEXT,                              -- 图片URL列表，用JSON数组格式存储，例如 '["url1","url2"]'
    category      VARCHAR(30) DEFAULT 'general',     -- 分类：general(日常)/confession(表白)/lost_found(失物招领)/trade(二手)/help(求助)
    is_anonymous  BOOLEAN DEFAULT FALSE,             -- 是否匿名：TRUE匿名发布，列表页隐藏发布者信息
    view_count    INT DEFAULT 0,                     -- 浏览次数（冗余字段，用Redis计数后同步过来）
    like_count    INT DEFAULT 0,                     -- 点赞数（冗余字段，方便按热度排序，不用每次COUNT查询）
    comment_count INT DEFAULT 0,                     -- 评论数（同上，冗余字段提升查询性能）
    status        SMALLINT DEFAULT 1,                -- 状态：1正常 0已删除(软删除) 2审核中
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 帖子表索引：按「谁的帖子」「哪个分类」「发布时间倒序」查询非常频繁，所以加索引
CREATE INDEX idx_post_user_id ON post(user_id);
CREATE INDEX idx_post_category ON post(category);
CREATE INDEX idx_post_created_at ON post(created_at DESC);

-- 4. 评论表（支持二级评论：顶级评论 + 回复）
--    parent_id=0 表示顶级评论（直接评论帖子）
--    parent_id=某个评论id 表示回复那条评论
--    这种设计叫「邻接表」，是最简单的树形结构实现方式
CREATE TABLE comment (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES post(id),     -- 属于哪个帖子
    user_id     BIGINT NOT NULL REFERENCES users(user_id),-- 评论者
    parent_id   BIGINT DEFAULT 0,                        -- 父评论ID，0=顶级评论，其他值=回复某条评论
    content     TEXT NOT NULL,                            -- 评论内容
    like_count  INT DEFAULT 0,                           -- 评论点赞数
    status      SMALLINT DEFAULT 1,                      -- 1正常 0已删除
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 查询某个帖子的所有评论是最常见的操作，所以给 post_id 加索引
CREATE INDEX idx_comment_post_id ON comment(post_id);

-- 5. 帖子点赞表（记录"谁给哪个帖子点了赞"）
--    UNIQUE(post_id, user_id) 保证同一个人对同一个帖子只能点赞一次
--    实际业务中点赞状态会先存Redis（快），再异步同步到这张表（持久化）
CREATE TABLE post_like (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES post(id),      -- 哪个帖子
    user_id     BIGINT NOT NULL REFERENCES users(user_id), -- 谁点的赞
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id)                               -- 联合唯一：防止重复点赞
);

-- 6. 聊天室表（私聊和群聊共用一张表，通过 room_type 区分）
--    私聊：room_type='PRIVATE'，两个人自动创建，room_name 为空
--    群聊：room_type='GROUP'，有群名、群主、群头像
CREATE TABLE chat_room (
    id          BIGSERIAL PRIMARY KEY,
    room_name   VARCHAR(100),                        -- 群聊名称（私聊时为空，前端展示对方昵称）
    room_type   VARCHAR(20) NOT NULL,                -- PRIVATE(私聊) / GROUP(群聊)
    owner_id    BIGINT,                              -- 群主用户ID（私聊时为空）
    avatar      VARCHAR(500),                        -- 群头像URL（私聊时为空）
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 聊天室成员表（多对多关系的中间表：一个用户可以在多个聊天室，一个聊天室有多个用户）
--    这是经典的「多对多关系」处理方式：用一张中间表来关联两张主表
CREATE TABLE chat_room_member (
    id          BIGSERIAL PRIMARY KEY,
    room_id     BIGINT NOT NULL REFERENCES chat_room(id),  -- 属于哪个聊天室
    user_id     BIGINT NOT NULL REFERENCES users(user_id), -- 哪个用户
    role        VARCHAR(20) DEFAULT 'MEMBER',              -- 成员角色：OWNER(群主) / ADMIN(管理员) / MEMBER(普通成员)
    joined_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,       -- 加入时间
    UNIQUE(room_id, user_id)                               -- 同一个人不能重复加入同一个聊天室
);

-- 查询「某个用户参与了哪些聊天室」非常频繁，所以给 user_id 加索引
CREATE INDEX idx_member_user_id ON chat_room_member(user_id);

-- 8. 聊天消息表（所有聊天记录都存在这一张表里，通过 room_id 区分是哪个聊天室的消息）
CREATE TABLE chat_message (
    id          BIGSERIAL PRIMARY KEY,
    room_id     BIGINT NOT NULL REFERENCES chat_room(id),  -- 属于哪个聊天室
    sender_id   BIGINT NOT NULL REFERENCES users(user_id), -- 谁发的消息
    content     TEXT NOT NULL,                              -- 消息内容
    msg_type    VARCHAR(20) DEFAULT 'TEXT',                 -- 消息类型：TEXT(文字) / IMAGE(图片) / FILE(文件) / SYSTEM(系统通知)
    status      SMALLINT DEFAULT 1,                        -- 1正常 0已撤回
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP        -- 发送时间
);

-- 聊天消息的两个核心查询场景：按聊天室查、按时间倒序查（加载历史消息）
CREATE INDEX idx_message_room_id ON chat_message(room_id);
CREATE INDEX idx_message_created_at ON chat_message(created_at DESC);

-- 9. AI 摘要表（存储 AI 对校园墙帖子的自动总结结果）
--    由定时任务或管理员手动触发生成，调用外部AI大模型API（如DeepSeek）
CREATE TABLE ai_summary (
    id               BIGSERIAL PRIMARY KEY,
    summary_type     VARCHAR(30) NOT NULL,            -- 总结类型：DAILY(每日) / WEEKLY(每周) / CATEGORY(按分类)
    category         VARCHAR(30),                     -- 帖子分类（仅 CATEGORY 类型时有值）
    content          TEXT NOT NULL,                   -- AI 生成的摘要正文（可以是JSON格式的结构化内容）
    post_count       INT DEFAULT 0,                   -- 本次总结涉及了多少条帖子
    date_range_start TIMESTAMP,                       -- 总结覆盖的起始时间
    date_range_end   TIMESTAMP,                       -- 总结覆盖的结束时间
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 摘要生成时间
);

CREATE INDEX idx_summary_type ON ai_summary(summary_type);
CREATE INDEX idx_summary_created_at ON ai_summary(created_at DESC);
