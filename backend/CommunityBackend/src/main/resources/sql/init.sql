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
    college     VARCHAR(100),                        -- 学院
    birthday    VARCHAR(20),                         -- 生日
    qq_number   VARCHAR(20),                         -- QQ号
    wechat_id   VARCHAR(50),                         -- 微信号

    -- 关联账户状态
    wechat_linked   BOOLEAN DEFAULT FALSE,           -- 微信是否绑定
    qq_linked       BOOLEAN DEFAULT FALSE,           -- QQ是否绑定
    github_linked   BOOLEAN DEFAULT FALSE,           -- GitHub是否绑定

    -- 邮箱验证和安全设置
    email_verified      BOOLEAN DEFAULT FALSE,       -- 邮箱是否验证
    password_updated_at TIMESTAMP,                   -- 密码最后修改时间
    two_factor_enabled  BOOLEAN DEFAULT FALSE,       -- 两步验证是否启用
    sms_verify_enabled  BOOLEAN DEFAULT FALSE,       -- 短信验证是否启用

    -- 通知设置
    notify_post_reply     BOOLEAN DEFAULT TRUE,      -- 帖子回复通知
    notify_comment_reply  BOOLEAN DEFAULT TRUE,      -- 评论回复通知
    notify_mention        BOOLEAN DEFAULT TRUE,      -- @提及通知
    notify_club_activity  BOOLEAN DEFAULT TRUE,      -- 社团活动通知
    notify_system         BOOLEAN DEFAULT TRUE,      -- 系统通知

    -- 邮件设置
    email_system_notify   BOOLEAN DEFAULT TRUE,      -- 系统通知邮件
    email_activity_remind BOOLEAN DEFAULT TRUE,      -- 活动提醒邮件
    email_weekly_digest   BOOLEAN DEFAULT FALSE,     -- 周报邮件

    -- 隐私设置
    public_profile        BOOLEAN DEFAULT TRUE,      -- 公开个人资料
    show_online           BOOLEAN DEFAULT TRUE,      -- 显示在线状态
    allow_message         BOOLEAN DEFAULT TRUE,      -- 允许私信

    -- 界面设置
    dark_mode             BOOLEAN DEFAULT FALSE,     -- 深色模式
    compact_mode          BOOLEAN DEFAULT FALSE,     -- 紧凑模式

    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 社团表（存储社团信息）
CREATE TABLE club (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,             -- 社团名称
    description   TEXT,                              -- 社团简介
    color         VARCHAR(20) DEFAULT '#165dff',     -- 社团主题色（用于前端标签显示）
    avatar        VARCHAR(500),                      -- 社团头像/Logo URL
    owner_id      BIGINT REFERENCES users(user_id),  -- 社团负责人
    member_count  INT DEFAULT 0,                     -- 成员数量（冗余字段）
    post_count    INT DEFAULT 0,                     -- 帖子数量（冗余字段）
    status        SMALLINT DEFAULT 1,                -- 状态：1正常 0已禁用
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 校园墙帖子表（核心业务表）
CREATE TABLE post (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(user_id),  -- 发帖人（外键关联users表）
    title         VARCHAR(200),                      -- 帖子标题
    content       TEXT NOT NULL,                     -- 帖子正文（TEXT类型不限长度）
    images        TEXT,                              -- 图片URL列表，用逗号分隔
    club_id       BIGINT REFERENCES club(id),        -- 所属社团ID
    tags          VARCHAR(500),                      -- 标签，用逗号分隔
    is_anonymous  BOOLEAN DEFAULT FALSE,             -- 是否匿名：TRUE匿名发布，列表页隐藏发布者信息
    view_count    INT DEFAULT 0,                     -- 浏览次数（冗余字段，用Redis计数后同步过来）
    like_count    INT DEFAULT 0,                     -- 点赞数（冗余字段，方便按热度排序，不用每次COUNT查询）
    comment_count INT DEFAULT 0,                     -- 评论数（同上，冗余字段提升查询性能）
    status        SMALLINT DEFAULT 1,                -- 状态：1正常 0已删除(软删除) 2审核中
    pinned        BOOLEAN DEFAULT FALSE,             -- 是否置顶
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 帖子表索引：按「谁的帖子」「哪个社团」「发布时间倒序」查询非常频繁，所以加索引
CREATE INDEX idx_post_user_id ON post(user_id);
CREATE INDEX idx_post_club_id ON post(club_id);
CREATE INDEX idx_post_created_at ON post(created_at DESC);

-- 5. 评论表（支持二级评论：顶级评论 + 回复）
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

-- 6. 帖子点赞表（记录"谁给哪个帖子点了赞"）
--    UNIQUE(post_id, user_id) 保证同一个人对同一个帖子只能点赞一次
--    实际业务中点赞状态会先存Redis（快），再异步同步到这张表（持久化）
CREATE TABLE post_like (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT NOT NULL REFERENCES post(id),      -- 哪个帖子
    user_id     BIGINT NOT NULL REFERENCES users(user_id), -- 谁点的赞
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(post_id, user_id)                               -- 联合唯一：防止重复点赞
);

-- 7. 聊天室表（私聊和群聊共用一张表，通过 room_type 区分）
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

-- 8. 聊天室成员表（多对多关系的中间表：一个用户可以在多个聊天室，一个聊天室有多个用户）
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

-- 9. 聊天消息表（所有聊天记录都存在这一张表里，通过 room_id 区分是哪个聊天室的消息）
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

-- 10. AI 摘要表（存储 AI 对校园墙帖子的自动总结结果）
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


-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入社团数据
INSERT INTO club (name, description, color, member_count, post_count) VALUES
('篮球社', '热爱篮球运动的同学们聚集地，每周组织训练和比赛', '#ff7d00', 56, 12),
('摄影社', '用镜头记录校园美好瞬间，分享摄影技巧和作品', '#00b42a', 38, 8),
('读书会', '共同阅读，分享思想，每周推荐优质书籍', '#165dff', 42, 15),
('音乐社', '唱歌、乐器、作曲，音乐爱好者的天堂', '#722ed1', 65, 10),
('编程社', '代码改变世界，一起学习编程技术', '#f53f3f', 48, 20);

-- 插入测试用户
INSERT INTO users (user_id, username, nickname, password, role, status) VALUES
(1001, 'zhangsan', '张三', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 1),
(1002, 'lisi', '李四', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 1),
(1003, 'xiaoming', '小明', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 1),
(1004, 'xiaohong', '小红', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 1),
(1005, 'wangwu', '王五', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'USER', 1);

-- 插入测试帖子
INSERT INTO post (user_id, title, content, club_id, tags, view_count, like_count, comment_count, pinned, status, created_at, updated_at) VALUES
(1001, '篮球社本周六下午3点体育馆集合训练', '各位社员注意啦！本周六（2月8日）下午3点在体育馆集合，进行常规训练。请大家准时参加，记得带好运动装备和水杯。本次训练内容包括：基础运球、投篮练习、团队配合战术演练。新加入的同学也不用担心，我们会有老成员带着一起练习。期待大家的参与！', 1, '活动通知,训练', 1520, 45, 23, true, 1, NOW() - INTERVAL '10 minutes', NOW() - INTERVAL '10 minutes'),
(1002, '摄影社招新啦！欢迎热爱摄影的同学加入', '摄影社2024年春季招新开始了！无论你是摄影小白还是大神，只要你热爱摄影，欢迎加入我们的大家庭。我们定期组织外拍活动、摄影技巧分享会、后期处理教学等。加入我们，一起用镜头记录校园的美好瞬间！报名方式：扫描下方二维码或直接私信社长。', 2, '招新,摄影', 3200, 89, 89, false, 1, NOW() - INTERVAL '30 minutes', NOW() - INTERVAL '30 minutes'),
(1003, '读书会本周推荐：《人类简史》', '本周读书会为大家推荐以色列历史学家尤瓦尔·赫拉利的经典著作《人类简史》，欢迎大家阅读并参与讨论。这本书从认知革命、农业革命、科学革命三个角度，讲述了人类从东非的一个普通动物到地球主宰的进化历程。本周六下午2点在图书馆三楼研讨室，我们将一起分享读书心得。', 3, '书籍推荐', 890, 32, 45, false, 1, NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),
(1004, '音乐社周末弹唱会，欢迎参加', '音乐社本周日晚上7点将在小剧场举办一场小型弹唱会，届时将有多位社员带来精彩的吉他弹唱表演。曲目涵盖流行、民谣、摇滚等多种风格。欢迎所有热爱音乐的同学前来观看，也欢迎想要上台表演的同学提前报名！', 4, '活动,弹唱会', 650, 28, 15, false, 1, NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'),
(1005, '编程社算法竞赛培训班开始报名', '编程社将于下周开始举办算法竞赛培训班，面向有一定编程基础、想要参加ACM/蓝桥杯等竞赛的同学。培训内容包括：基础数据结构、常见算法、动态规划、图论等。每周两次课程，由有竞赛经验的学长学姐授课。名额有限，先到先得！', 5, '竞赛,培训', 1200, 56, 32, false, 1, NOW() - INTERVAL '3 hours', NOW() - INTERVAL '3 hours'),
(1001, '失物招领：图书馆捡到一个黑色钱包', '今天下午在图书馆二楼自习室捡到一个黑色钱包，里面有校园卡和一些现金。请失主尽快联系我认领。联系方式：微信 zhangsan123。', NULL, '失物招领', 320, 5, 8, false, 1, NOW() - INTERVAL '4 hours', NOW() - INTERVAL '4 hours'),
(1002, '求助：有没有同学有《高等数学》第七版的教材', '急需一本《高等数学》同济第七版上册，有没有学长学姐有闲置的可以转让或借用？可以有偿购买，价格好商量。谢谢大家！', NULL, '求助,教材', 180, 3, 12, false, 1, NOW() - INTERVAL '5 hours', NOW() - INTERVAL '5 hours');

-- 插入测试评论
INSERT INTO comment (post_id, user_id, parent_id, content, like_count, status) VALUES
(1, 1002, 0, '收到！周六见！', 5, 1),
(1, 1003, 0, '我是新加入的，期待和大家一起训练', 3, 1),
(1, 1004, 1, '李四你来接我呗', 2, 1),
(2, 1005, 0, '我想加入！请问需要自备相机吗？', 8, 1),
(2, 1003, 4, '手机也可以的，主要是热爱摄影就行', 4, 1),
(3, 1001, 0, '这本书太棒了，强烈推荐！', 6, 1);

-- 插入聊天频道（社团群聊）
INSERT INTO chat_room (room_name, room_type, owner_id) VALUES
('常规频道', 'channel', NULL),
('篮球社群聊', 'channel', NULL),
('摄影社群聊', 'channel', NULL);


-- ============================================
-- user_profile 表结构升级脚本
-- 执行此脚本为现有数据库添加新字段
-- ============================================

-- 添加新的基本信息字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS college VARCHAR(100);
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS birthday VARCHAR(20);
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS qq_number VARCHAR(20);
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS wechat_id VARCHAR(50);

-- 添加关联账户状态字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS wechat_linked BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS qq_linked BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS github_linked BOOLEAN DEFAULT FALSE;

-- 添加邮箱验证和安全设置字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS password_updated_at TIMESTAMP;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS sms_verify_enabled BOOLEAN DEFAULT FALSE;

-- 添加通知设置字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS notify_post_reply BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS notify_comment_reply BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS notify_mention BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS notify_club_activity BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS notify_system BOOLEAN DEFAULT TRUE;

-- 添加邮件设置字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS email_system_notify BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS email_activity_remind BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS email_weekly_digest BOOLEAN DEFAULT FALSE;

-- 添加隐私设置字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS public_profile BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS show_online BOOLEAN DEFAULT TRUE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS allow_message BOOLEAN DEFAULT TRUE;

-- 添加界面设置字段
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS dark_mode BOOLEAN DEFAULT FALSE;
ALTER TABLE user_profile ADD COLUMN IF NOT EXISTS compact_mode BOOLEAN DEFAULT FALSE;

-- 添加字段注释（可选，PostgreSQL语法）
COMMENT ON COLUMN user_profile.college IS '学院';
COMMENT ON COLUMN user_profile.birthday IS '生日';
COMMENT ON COLUMN user_profile.qq_number IS 'QQ号';
COMMENT ON COLUMN user_profile.wechat_id IS '微信号';
COMMENT ON COLUMN user_profile.wechat_linked IS '微信是否绑定';
COMMENT ON COLUMN user_profile.qq_linked IS 'QQ是否绑定';
COMMENT ON COLUMN user_profile.github_linked IS 'GitHub是否绑定';
COMMENT ON COLUMN user_profile.email_verified IS '邮箱是否验证';
COMMENT ON COLUMN user_profile.password_updated_at IS '密码最后修改时间';
COMMENT ON COLUMN user_profile.two_factor_enabled IS '两步验证是否启用';
COMMENT ON COLUMN user_profile.sms_verify_enabled IS '短信验证是否启用';
COMMENT ON COLUMN user_profile.notify_post_reply IS '帖子回复通知';
COMMENT ON COLUMN user_profile.notify_comment_reply IS '评论回复通知';
COMMENT ON COLUMN user_profile.notify_mention IS '@提及通知';
COMMENT ON COLUMN user_profile.notify_club_activity IS '社团活动通知';
COMMENT ON COLUMN user_profile.notify_system IS '系统通知';
COMMENT ON COLUMN user_profile.email_system_notify IS '系统通知邮件';
COMMENT ON COLUMN user_profile.email_activity_remind IS '活动提醒邮件';
COMMENT ON COLUMN user_profile.email_weekly_digest IS '周报邮件';
COMMENT ON COLUMN user_profile.public_profile IS '公开个人资料';
COMMENT ON COLUMN user_profile.show_online IS '显示在线状态';
COMMENT ON COLUMN user_profile.allow_message IS '允许私信';
COMMENT ON COLUMN user_profile.dark_mode IS '深色模式';
COMMENT ON COLUMN user_profile.compact_mode IS '紧凑模式';
