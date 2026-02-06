<template>
  <div class="profile-page">
    <!-- 顶部提示条 -->
    <a-alert type="info" class="top-banner" :show-icon="false">
      真诚、友善、团结、专业，共建你我引以为荣之社区。《常见问题解答》
    </a-alert>

    <!-- 用户信息卡片 -->
    <div class="user-card">
      <div class="user-header">
        <a-avatar :size="56">
          <img v-if="userStore.avatar" :src="userStore.avatar" />
          <span v-else>{{ (userStore.nickname || 'U').charAt(0).toUpperCase() }}</span>
        </a-avatar>
        <div class="user-info">
          <div class="nickname">{{ userStore.nickname || '未设置昵称' }}</div>
          <div class="username">{{ userStore.userInfo?.username }}</div>
        </div>
        <!-- 桌面端展开按钮 -->
        <a-button class="expand-btn desktop-expand-btn" @click="showMoreInfo = !showMoreInfo">
          <template #icon>
            <icon-down v-if="!showMoreInfo" />
            <icon-up v-else />
          </template>
          {{ showMoreInfo ? '收起' : '展开' }}
        </a-button>
      </div>

      <!-- 移动端展开按钮 -->
      <a-button long class="expand-btn mobile-expand-btn" @click="showMoreInfo = !showMoreInfo">
        <template #icon>
          <icon-down v-if="!showMoreInfo" />
          <icon-up v-else />
        </template>
        {{ showMoreInfo ? '收起' : '展开' }}
      </a-button>

      <!-- 展开的详细信息 -->
      <div v-if="showMoreInfo" class="expanded-info">
        <a-descriptions :column="1" size="small">
          <a-descriptions-item label="学号">{{ profile?.studentId || '未设置' }}</a-descriptions-item>
          <a-descriptions-item label="专业">{{ profile?.major || '未设置' }}</a-descriptions-item>
          <a-descriptions-item label="年级">{{ profile?.grade || '未设置' }}</a-descriptions-item>
          <a-descriptions-item label="邮箱">{{ profile?.email || '未设置' }}</a-descriptions-item>
          <a-descriptions-item label="注册时间">{{ formatDate(profile?.createdAt) }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- 主导航栏 -->
      <a-tabs v-model:active-key="iconTab" class="main-tabs" @change="handleIconTabChange">
        <a-tab-pane v-for="item in iconNavItems" :key="item.key">
          <template #title>
            <component :is="item.icon" />
            {{ item.label }}
          </template>
        </a-tab-pane>
      </a-tabs>
    </div>

    <!-- 内容区域 Tabs -->
    <a-tabs v-model:active-key="contentTab" class="content-tabs">
      <a-tab-pane v-for="item in currentContentTabs" :key="item.key">
        <template #title>
          <component :is="item.icon" />
          {{ item.label }}
        </template>
      </a-tab-pane>
    </a-tabs>

    <!--  动态页面  -->
    <div v-if="iconTab === 'dynamic'" class="settings-section">
      <!-- 帐户 Tab -->
      <template v-if="contentTab === 'account'">
        <!-- 用户名 -->
        <div class="setting-group">
          <div class="setting-title">用户名</div>
          <div class="setting-content">
            <div class="setting-value">{{ userStore.userInfo?.username }}</div>
            <div class="setting-hint">其他人可以使用 @{{ userStore.userInfo?.username }} 来提及您</div>
          </div>
        </div>

        <!-- 个人资料照片 -->
        <div class="setting-group">
          <div class="setting-title">个人资料照片</div>
          <div class="setting-content avatar-setting">
            <div class="avatar-preview">
              <a-avatar :size="80">
                <img v-if="userStore.avatar" :src="userStore.avatar" />
                <span v-else>{{ (userStore.nickname || 'U').charAt(0).toUpperCase() }}</span>
              </a-avatar>
            </div>
          </div>
        </div>

        <!-- 电子邮件 -->
        <div class="setting-group">
          <div class="setting-title">电子邮件</div>
          <div class="setting-content">
            <div class="email-item">
              <div class="email-main">
                <span class="email-address">{{ profile?.email || '未设置邮箱' }}</span>
              </div>
              <a-tag v-if="profile?.email" color="arcoblue" size="small">常用</a-tag>
            </div>
            <div class="email-hint">绝不会向公众显示</div>
          </div>
        </div>

        <!-- 关联账户 -->
        <div class="setting-group">
          <div class="setting-title">关联账户</div>
          <div class="setting-content">
            <div class="linked-accounts">
              <div class="linked-item">
                <div class="linked-icon wechat">
                  <icon-wechat :size="20" />
                </div>
                <span class="linked-name">微信</span>
                <a-tag v-if="profile?.wechatLinked" color="green" size="small">已绑定</a-tag>
                <a-button v-else type="text" size="small">绑定</a-button>
              </div>
              <div class="linked-item">
                <div class="linked-icon qq">
                  <icon-qq :size="20" />
                </div>
                <span class="linked-name">QQ</span>
                <a-tag v-if="profile?.qqLinked" color="green" size="small">已绑定</a-tag>
                <a-button v-else type="text" size="small">绑定</a-button>
              </div>
              <div class="linked-item">
                <div class="linked-icon github">
                  <icon-github :size="20" />
                </div>
                <span class="linked-name">GitHub</span>
                <a-tag v-if="profile?.githubLinked" color="green" size="small">已绑定</a-tag>
                <a-button v-else type="text" size="small">绑定</a-button>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 安全性 Tab -->
      <template v-else-if="contentTab === 'security'">
        <div class="setting-group">
          <div class="setting-title">修改密码</div>
          <div class="setting-content">
            <div class="security-item">
              <div class="security-info">
                <span class="security-label">当前密码</span>
                <span class="security-hint">上次修改于 {{ formatDate(profile?.passwordUpdatedAt) || '从未修改' }}</span>
              </div>
              <a-button type="outline" size="small" @click="handleChangePassword">修改密码</a-button>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">两步验证</div>
          <div class="setting-content">
            <div class="security-item">
              <div class="security-info">
                <span class="security-label">验证器应用</span>
                <span class="security-hint">使用验证器应用生成一次性验证码</span>
              </div>
              <a-switch v-model="securitySettings.twoFactor" />
            </div>
            <div class="security-item">
              <div class="security-info">
                <span class="security-label">短信验证</span>
                <span class="security-hint">通过手机短信接收验证码</span>
              </div>
              <a-switch v-model="securitySettings.smsVerify" />
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">登录历史</div>
          <div class="setting-content">
            <div class="login-history">
              <div class="history-item" v-for="(item, index) in loginHistory" :key="index">
                <div class="history-icon">
                  <icon-desktop :size="18" />
                </div>
                <div class="history-info">
                  <span class="history-device">{{ item.device }}</span>
                  <span class="history-detail">{{ item.ip }} · {{ item.location }}</span>
                </div>
                <span class="history-time">{{ item.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 个人资料 Tab -->
      <template v-else-if="contentTab === 'profile'">
        <div class="setting-group">
          <div class="setting-title">基本信息</div>
          <div class="setting-content">
            <div class="profile-form">
              <div class="form-item">
                <span class="form-label">昵称</span>
                <span class="form-value">{{ userStore.nickname || '未设置' }}</span>
              </div>
              <div class="form-item">
                <span class="form-label">性别</span>
                <span class="form-value">{{ profile?.gender === 'male' ? '男' : profile?.gender === 'female' ? '女' : '保密' }}</span>
              </div>
              <div class="form-item">
                <span class="form-label">生日</span>
                <span class="form-value">{{ profile?.birthday || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">个人简介</div>
          <div class="setting-content">
            <div class="bio-content">
              <span v-if="profile?.bio" class="bio-text">{{ profile.bio }}</span>
              <span v-else class="bio-empty">还没有填写个人简介</span>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">学校信息</div>
          <div class="setting-content">
            <div class="school-info">
              <div class="info-row">
                <span class="info-label">学号</span>
                <span class="info-value">{{ profile?.studentId || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">学院</span>
                <span class="info-value">{{ profile?.college || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">专业</span>
                <span class="info-value">{{ profile?.major || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">年级</span>
                <span class="info-value">{{ profile?.grade || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">联系方式</div>
          <div class="setting-content">
            <div class="contact-info">
              <div class="info-row">
                <span class="info-label">手机号</span>
                <span class="info-value">{{ profile?.phone ? maskPhone(profile.phone) : '未绑定' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">QQ号</span>
                <span class="info-value">{{ profile?.qqNumber || '未设置' }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">微信号</span>
                <span class="info-value">{{ profile?.wechatId || '未设置' }}</span>
              </div>
            </div>
          </div>
        </div>
      </template>

      <!-- 电子邮件 Tab -->
      <template v-else-if="contentTab === 'email'">
        <div class="setting-group">
          <div class="setting-title">主邮箱</div>
          <div class="setting-content">
            <div class="email-detail">
              <div class="email-row">
                <span class="email-address-lg">{{ profile?.email || '未设置邮箱' }}</span>
                <a-tag v-if="profile?.emailVerified" color="green" size="small">已验证</a-tag>
                <a-tag v-else color="orange" size="small">未验证</a-tag>
              </div>
              <div class="email-actions">
                <a-button type="text" size="small" @click="handleEditEmail">修改邮箱</a-button>
                <a-button v-if="!profile?.emailVerified" type="text" size="small" status="success">发送验证邮件</a-button>
              </div>
            </div>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">备用邮箱</div>
          <div class="setting-content">
            <div v-if="backupEmails.length > 0" class="backup-emails">
              <div class="email-row" v-for="email in backupEmails" :key="email.address">
                <span class="email-address-lg">{{ email.address }}</span>
                <a-button type="text" size="small" status="danger">删除</a-button>
              </div>
            </div>
            <div v-else class="no-backup-email">
              <span class="hint-text">还没有设置备用邮箱</span>
            </div>
            <a-button type="outline" size="small" class="add-backup-btn">
              <template #icon><icon-plus :size="14" /></template>
              添加备用邮箱
            </a-button>
          </div>
        </div>

        <div class="setting-group">
          <div class="setting-title">邮件通知偏好</div>
          <div class="setting-content">
            <div class="notification-item">
              <div class="notification-info">
                <span class="notification-name">系统通知邮件</span>
                <span class="notification-desc">接收系统公告和重要更新</span>
              </div>
              <a-switch v-model="emailSettings.systemNotify" />
            </div>
            <div class="notification-item">
              <div class="notification-info">
                <span class="notification-name">活动提醒邮件</span>
                <span class="notification-desc">社团活动开始前提醒</span>
              </div>
              <a-switch v-model="emailSettings.activityRemind" />
            </div>
            <div class="notification-item">
              <div class="notification-info">
                <span class="notification-name">周报邮件</span>
                <span class="notification-desc">每周社区精选内容汇总</span>
              </div>
              <a-switch v-model="emailSettings.weeklyDigest" />
            </div>
          </div>
        </div>
      </template>
    </div>

    <!--  通知页面  -->
    <div v-else-if="iconTab === 'notification'" class="settings-section">
      <div class="setting-group">
        <div class="setting-title">通知偏好</div>
        <div class="setting-content">
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">帖子回复通知</span>
              <span class="notification-desc">当有人回复你的帖子时通知你</span>
            </div>
            <a-switch v-model="notificationSettings.postReply" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">评论回复通知</span>
              <span class="notification-desc">当有人回复你的评论时通知你</span>
            </div>
            <a-switch v-model="notificationSettings.commentReply" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">@提及通知</span>
              <span class="notification-desc">当有人@你时通知你</span>
            </div>
            <a-switch v-model="notificationSettings.mention" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">社团活动通知</span>
              <span class="notification-desc">你加入的社团有新活动时通知你</span>
            </div>
            <a-switch v-model="notificationSettings.clubActivity" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">系统通知</span>
              <span class="notification-desc">接收系统公告和更新通知</span>
            </div>
            <a-switch v-model="notificationSettings.system" />
          </div>
        </div>
      </div>

      <div class="setting-group">
        <div class="setting-title">通知列表</div>
        <div class="setting-content">
          <div v-if="notifications.length > 0" class="notifications-list">
            <div class="notification-card" v-for="notif in notifications" :key="notif.id">
              <div class="notif-icon" :class="notif.type">
                <icon-notification :size="16" />
              </div>
              <div class="notif-content">
                <span class="notif-title">{{ notif.title }}</span>
                <span class="notif-desc">{{ notif.content }}</span>
                <span class="notif-time">{{ notif.time }}</span>
              </div>
            </div>
          </div>
          <a-empty v-else description="暂无通知" />
        </div>
      </div>
    </div>

    <!--  私信页面  -->
    <div v-else-if="iconTab === 'message'" class="settings-section">
      <a-empty description="暂无私信" />
    </div>

    <!--  徽章页面  -->
    <div v-else-if="iconTab === 'badge'" class="settings-section">
      <div class="setting-group">
        <div class="setting-title">我的徽章</div>
        <div class="setting-content">
          <div class="badges-grid">
            <div class="badge-item" v-for="badge in badges" :key="badge.id">
              <div class="badge-icon" :style="{ background: badge.color }">
                <component :is="badge.icon" :size="24" />
              </div>
              <span class="badge-name">{{ badge.name }}</span>
              <span class="badge-desc">{{ badge.description }}</span>
            </div>
            <div v-if="badges.length === 0" class="no-badges">
              <icon-trophy :size="48" />
              <span>还没有获得徽章，继续努力吧！</span>
            </div>
          </div>
        </div>
      </div>

      <div class="setting-group">
        <div class="setting-title">可获得的徽章</div>
        <div class="setting-content">
          <div class="badges-grid">
            <div class="badge-item locked" v-for="badge in availableBadges" :key="badge.id">
              <div class="badge-icon locked-icon">
                <icon-lock :size="24" />
              </div>
              <span class="badge-name">{{ badge.name }}</span>
              <span class="badge-desc">{{ badge.requirement }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--  社团页面  -->
    <div v-else-if="iconTab === 'club'" class="settings-section">
      <div class="setting-group">
        <div class="setting-title">已加入的社团</div>
        <div class="setting-content">
          <div class="clubs-list">
            <div class="club-item" v-for="club in myClubs" :key="club.id" @click="router.push(`/club/${club.id}`)">
              <a-avatar :size="40" :style="{ background: club.color }">
                {{ club.name.charAt(0) }}
              </a-avatar>
              <div class="club-info">
                <span class="club-name">{{ club.name }}</span>
                <span class="club-members">{{ club.memberCount }} 成员</span>
              </div>
              <a-tag :color="club.role === 'admin' ? 'orange' : 'arcoblue'" size="small">
                {{ club.role === 'admin' ? '管理员' : '成员' }}
              </a-tag>
            </div>
            <div v-if="myClubs.length === 0" class="no-clubs">
              <icon-user-group :size="48" />
              <span>还没有加入任何社团</span>
              <a-button type="primary" @click="router.push('/clubs')">浏览社团</a-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!--  设备页面  -->
    <div v-else-if="iconTab === 'device'" class="settings-section">
      <div class="setting-group">
        <div class="setting-title">登录设备</div>
        <div class="setting-content">
          <div class="devices-list">
            <div class="device-item current">
              <div class="device-icon">
                <icon-desktop :size="24" />
              </div>
              <div class="device-info">
                <span class="device-name">当前设备</span>
                <span class="device-detail">Windows · Chrome · {{ currentIP }}</span>
                <span class="device-time">当前在线</span>
              </div>
              <a-tag color="green" size="small">当前</a-tag>
            </div>
          </div>
          <a-button long type="outline" status="danger" class="logout-all-btn">
            <template #icon><icon-export /></template>
            退出所有其他设备
          </a-button>
        </div>
      </div>
    </div>

    <!--  设置页面  -->
    <div v-else-if="iconTab === 'settings'" class="settings-section">
      <div class="setting-group">
        <div class="setting-title">界面设置</div>
        <div class="setting-content">
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">深色模式</span>
              <span class="notification-desc">切换界面主题为深色</span>
            </div>
            <a-switch v-model="uiSettings.darkMode" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">紧凑模式</span>
              <span class="notification-desc">减少页面间距，显示更多内容</span>
            </div>
            <a-switch v-model="uiSettings.compactMode" />
          </div>
        </div>
      </div>

      <div class="setting-group">
        <div class="setting-title">隐私设置</div>
        <div class="setting-content">
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">公开个人资料</span>
              <span class="notification-desc">允许其他用户查看你的个人资料</span>
            </div>
            <a-switch v-model="privacySettings.publicProfile" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">显示在线状态</span>
              <span class="notification-desc">让其他用户看到你是否在线</span>
            </div>
            <a-switch v-model="privacySettings.showOnline" />
          </div>
          <div class="notification-item">
            <div class="notification-info">
              <span class="notification-name">允许私信</span>
              <span class="notification-desc">允许其他用户向你发送私信</span>
            </div>
            <a-switch v-model="privacySettings.allowMessage" />
          </div>
        </div>
      </div>

      <div class="setting-group">
        <div class="setting-title">账户操作</div>
        <div class="setting-content">
          <div class="account-actions">
            <a-button long type="outline">导出我的数据</a-button>
            <a-button long type="outline" status="danger">注销账户</a-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <a-button long status="danger" @click="handleLogout" :loading="loading">
        <template #icon><icon-export /></template>
        退出登录
      </a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, markRaw, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Modal, Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import {
  getUserProfile,
  updateUserProfile,
  updateNotificationSettings,
  updateEmailSettings,
  updatePrivacySettings,
  updateUISettings
} from '@/api/user'
import {
  IconUser,
  IconNotification,
  IconMessage,
  IconTrophy,
  IconUserGroup,
  IconDesktop,
  IconDown,
  IconUp,
  IconExport,
  IconEdit,
  IconPlus,
  IconSettings,
  IconLock,
  IconIdcard,
  IconEmail,
  IconWechat,
  IconQq,
  IconGithub
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showMoreInfo = ref(false)
const iconTab = ref('dynamic')
const contentTab = ref('account')
const profile = ref(null)
const currentIP = ref('192.168.1.xxx')

// 通知设置
const notificationSettings = ref({
  postReply: true,
  commentReply: true,
  mention: true,
  clubActivity: true,
  system: true
})

// 安全设置
const securitySettings = ref({
  twoFactor: false,
  smsVerify: false
})

// 邮件设置
const emailSettings = ref({
  systemNotify: true,
  activityRemind: true,
  weeklyDigest: false
})

// 界面设置
const uiSettings = ref({
  darkMode: false,
  compactMode: false
})

// 隐私设置
const privacySettings = ref({
  publicProfile: true,
  showOnline: true,
  allowMessage: true
})

// 我的社团
const myClubs = ref([])

// 我的徽章
const badges = ref([])

// 可获得的徽章
const availableBadges = ref([
  { id: 1, name: '新手上路', requirement: '完成首次发帖' },
  { id: 2, name: '活跃用户', requirement: '连续登录7天' },
  { id: 3, name: '社交达人', requirement: '获得100个点赞' }
])

// 通知列表
const notifications = ref([])

// 备用邮箱
const backupEmails = ref([])

// 登录历史
const loginHistory = ref([
  { device: 'Windows PC', ip: '192.168.1.100', location: '北京', time: '刚刚' },
  { device: 'iPhone 15', ip: '10.0.0.1', location: '上海', time: '2小时前' }])

// 监听通知设置变化并保存
watch(notificationSettings, async (newVal) => {
  try {
    await updateNotificationSettings(newVal)
  } catch (error) {
    console.error('保存通知设置失败:', error)
  }
}, { deep: true })

// 监听邮件设置变化并保存
watch(emailSettings, async (newVal) => {
  try {
    await updateEmailSettings(newVal)
  } catch (error) {
    console.error('保存邮件设置失败:', error)
  }
}, { deep: true })

// 监听隐私设置变化并保存
watch(privacySettings, async (newVal) => {
  try {
    await updatePrivacySettings(newVal)
  } catch (error) {
    console.error('保存隐私设置失败:', error)
  }
}, { deep: true })

// 监听界面设置变化并保存
watch(uiSettings, async (newVal) => {
  try {
    await updateUISettings(newVal)
  } catch (error) {
    console.error('保存界面设置失败:', error)
  }
}, { deep: true })

// 图标导航项
const iconNavItems = [
  { key: 'dynamic', icon: markRaw(IconUser), label: '动态' },
  { key: 'notification', icon: markRaw(IconNotification), label: '通知' },
  { key: 'message', icon: markRaw(IconMessage), label: '私信' },
  { key: 'badge', icon: markRaw(IconTrophy), label: '徽章' },
  { key: 'club', icon: markRaw(IconUserGroup), label: '社团' },
  { key: 'device', icon: markRaw(IconDesktop), label: '设备' },
  { key: 'settings', icon: markRaw(IconSettings), label: '设置' }
]

// 各个 iconTab 对应的内容 Tab
const contentTabsMap = {
  dynamic: [
    { key: 'account', label: '帐户', icon: markRaw(IconUser) },
    { key: 'security', label: '安全性', icon: markRaw(IconLock) },
    { key: 'profile', label: '个人资料', icon: markRaw(IconIdcard) },
    { key: 'email', label: '电子邮件', icon: markRaw(IconEmail) }
  ],
  notification: [
    { key: 'all', label: '全部', icon: markRaw(IconNotification) },
    { key: 'unread', label: '未读', icon: markRaw(IconMessage) }
  ],
  message: [
    { key: 'inbox', label: '收件箱', icon: markRaw(IconMessage) },
    { key: 'sent', label: '已发送', icon: markRaw(IconExport) }
  ],
  badge: [
    { key: 'owned', label: '已获得', icon: markRaw(IconTrophy) },
    { key: 'available', label: '可获得', icon: markRaw(IconLock) }
  ],
  club: [
    { key: 'joined', label: '已加入', icon: markRaw(IconUserGroup) },
    { key: 'managed', label: '我管理的', icon: markRaw(IconSettings) }
  ],
  device: [
    { key: 'current', label: '当前设备', icon: markRaw(IconDesktop) },
    { key: 'history', label: '登录历史', icon: markRaw(IconLock) }
  ],
  settings: [
    { key: 'interface', label: '界面', icon: markRaw(IconDesktop) },
    { key: 'privacy', label: '隐私', icon: markRaw(IconLock) }
  ]
}

// 当前显示的内容 Tab
const currentContentTabs = computed(() => {
  return contentTabsMap[iconTab.value] || contentTabsMap.dynamic
})

function handleIconTabChange(key) {
  iconTab.value = key
  // 切换到对应的第一个 contentTab
  const tabs = contentTabsMap[key]
  if (tabs && tabs.length > 0) {
    contentTab.value = tabs[0].key
  }
}

function formatDate(dateStr) {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

function maskPhone(phone) {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

async function fetchProfile() {
  try {
    const res = await getUserProfile()
    profile.value = res.data

    // 同步通知设置
    if (res.data) {
      notificationSettings.value = {
        postReply: res.data.notifyPostReply ?? true,
        commentReply: res.data.notifyCommentReply ?? true,
        mention: res.data.notifyMention ?? true,
        clubActivity: res.data.notifyClubActivity ?? true,
        system: res.data.notifySystem ?? true
      }

      // 同步安全设置
      securitySettings.value = {
        twoFactor: res.data.twoFactorEnabled ?? false,
        smsVerify: res.data.smsVerifyEnabled ?? false
      }

      // 同步邮件设置
      emailSettings.value = {
        systemNotify: res.data.emailSystemNotify ?? true,
        activityRemind: res.data.emailActivityRemind ?? true,
        weeklyDigest: res.data.emailWeeklyDigest ?? false
      }

      // 同步隐私设置
      privacySettings.value = {
        publicProfile: res.data.publicProfile ?? true,
        showOnline: res.data.showOnline ?? true,
        allowMessage: res.data.allowMessage ?? true
      }

      // 同步界面设置
      uiSettings.value = {
        darkMode: res.data.darkMode ?? false,
        compactMode: res.data.compactMode ?? false
      }
    }
  } catch (error) {
    console.error('获取用户资料失败:', error)
  }
}

function handleEditAvatar() {
  // TODO: 实现头像上传功能
  Message.info('头像编辑功能开发中')
}

function handleEditEmail() {
  // TODO: 实现邮箱修改弹窗
  Message.info('邮箱编辑功能开发中')
}

async function handleEditBio() {
  const newBio = window.prompt('请输入新的个人简介:', profile.value?.bio || '')
  if (newBio !== null && newBio !== profile.value?.bio) {
    try {
      await updateUserProfile({ bio: newBio })
      profile.value.bio = newBio
      Message.success('个人简介已更新')
    } catch (error) {
      Message.error('更新失败')
    }
  }
}

async function handleEditNickname() {
  const newNickname = window.prompt('请输入新的昵称:', userStore.nickname || '')
  if (newNickname !== null && newNickname.trim() && newNickname !== userStore.nickname) {
    try {
      await updateUserProfile({ nickname: newNickname.trim() })
      userStore.setUserInfo({ ...userStore.userInfo, nickname: newNickname.trim() })
      Message.success('昵称已更新')
    } catch (error) {
      Message.error('更新失败')
    }
  }
}

function handleChangePassword() {
  // TODO: 实现密码修改弹窗
  Message.info('密码修改功能开发中')
}

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    okText: '退出',
    cancelText: '取消',
    onOk: async () => {
      loading.value = true
      try {
        await userStore.logout()
        Message.success('已退出登录')
        router.replace('/login')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
}

/* 顶部提示条 */
.top-banner {
  border-radius: 0;
  border-left: none;
  border-right: none;
  border-top: none;
}

.top-banner :deep(.arco-alert-content) {
  text-align: center;
}

/* 用户卡片 */
.user-card {
  background: #fff;
  padding: 16px 16px 0 16px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.user-header :deep(.arco-avatar) {
  flex-shrink: 0;
  border: 2px solid #e5e6eb;
}

.user-info {
  flex: 1;
}

.nickname {
  font-size: 22px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.username {
  font-size: 14px;
  color: #86909c;
}

/* 展开按钮 */
.expand-btn {
  background: #fff;
  border: 1px solid #e5e6eb;
  color: #4e5969;
  border-radius: 8px;
  height: 36px;
}

.expand-btn:hover {
  background: #f7f8fa;
  border-color: #c9cdd4;
}

/* 桌面端展开按钮默认隐藏 */
.desktop-expand-btn {
  display: none;
}

/* 移动端展开按钮默认显示 */
.mobile-expand-btn {
  display: flex;
}

/* 展开的详细信息 */
.expanded-info {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 16px 20px;
  margin-top: 16px;
}

/* 主导航栏 Tabs */
.main-tabs {
  margin-top: 12px;
  border-top: 1px solid #e5e6eb;
}

.main-tabs :deep(.arco-tabs-nav) {
  padding: 0;
}

.main-tabs :deep(.arco-tabs-nav::before) {
  display: none;
}

.main-tabs :deep(.arco-tabs-tab) {
  padding: 12px 16px;
  font-size: 15px;
  margin: 0;
}

.main-tabs :deep(.arco-tabs-tab-title) {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 内容区域 Tabs */
.content-tabs {
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  margin-top: 0;
}

.content-tabs :deep(.arco-tabs-nav) {
  padding: 0 16px;
}

.content-tabs :deep(.arco-tabs-nav::before) {
  display: none;
}

.content-tabs :deep(.arco-tabs-tab) {
  padding: 12px 16px;
  font-size: 14px;
  margin: 0;
}

.content-tabs :deep(.arco-tabs-tab-title) {
  display: flex;
  align-items: center;
  gap: 6px;
}

.content-tabs :deep(.arco-tabs-content) {
  display: none;
}

/* 设置区域 */
.settings-section {
  background: #fff;
  min-height: 300px;
}

.settings-section :deep(.arco-empty) {
  padding: 60px 0;
}

.setting-group {
  padding: 24px;
  border-bottom: 1px solid #f2f3f5;
}

.setting-group:last-child {
  border-bottom: none;
}

.setting-title {
  font-size: 18px;
  font-weight: 600;
  color: #165dff;
  margin-bottom: 20px;
}

.setting-content {
  color: #1d2129;
}

.setting-value {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
}

.setting-hint {
  font-size: 13px;
  color: #86909c;
}

/* 头像设置 */
.avatar-setting {
  display: flex;
  align-items: flex-start;
}

.avatar-preview {
  position: relative;
  display: inline-block;
}

.avatar-edit-btn {
  position: absolute;
  bottom: 0;
  right: 0;
  background: #fff;
  border: 1px solid #e5e6eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 邮箱设置 */
.email-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.email-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.email-address {
  font-size: 15px;
  color: #1d2129;
}

.email-address-lg {
  font-size: 16px;
  color: #1d2129;
  font-weight: 500;
}

.email-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.email-hint {
  font-size: 13px;
  color: #86909c;
}

.add-email-btn {
  color: #165dff;
}

.edit-btn {
  color: #86909c;
}

.edit-btn:hover {
  color: #165dff;
}

/* 关联账户 - 参考 Linux.do 样式 */
.linked-accounts {
  display: flex;
  flex-direction: column;
  gap: 0;
  border: 1px solid #e5e6eb;
  border-radius: 8px;
  overflow: hidden;
}

.linked-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  transition: background 0.2s;
}

.linked-item:last-child {
  border-bottom: none;
}

.linked-item:hover {
  background: #f7f8fa;
}

.linked-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.linked-icon.wechat {
  background: #07c160;
}

.linked-icon.qq {
  background: #12b7f5;
}

.linked-icon.github {
  background: #24292f;
}

.linked-name {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
}

.linked-item :deep(.arco-tag) {
  margin-left: auto;
}

.linked-item :deep(.arco-btn) {
  margin-left: auto;
  background: #165dff;
  color: #fff;
  border-radius: 6px;
  padding: 4px 16px;
}

.linked-item :deep(.arco-btn:hover) {
  background: #4080ff;
}

/* 个人简介 */
.bio-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.bio-text {
  flex: 1;
  font-size: 14px;
  color: #1d2129;
  line-height: 1.6;
}

.bio-empty {
  flex: 1;
  font-size: 14px;
  color: #86909c;
}

/* 学校信息 & 联系方式 */
.school-info,
.contact-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-label {
  width: 60px;
  font-size: 14px;
  color: #86909c;
}

.info-value {
  flex: 1;
  font-size: 14px;
  color: #1d2129;
}

/* 安全设置 */
.security-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.security-item:last-child {
  border-bottom: none;
}

.security-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.security-label {
  font-size: 14px;
  color: #1d2129;
}

.security-hint {
  font-size: 12px;
  color: #86909c;
}

/* 登录历史 */
.login-history {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.history-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
}

.history-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-device {
  font-size: 14px;
  color: #1d2129;
}

.history-detail {
  font-size: 12px;
  color: #86909c;
}

.history-time {
  font-size: 12px;
  color: #86909c;
}

/* 个人资料表单 */
.profile-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.form-label {
  width: 60px;
  font-size: 14px;
  color: #86909c;
}

.form-value {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #1d2129;
}

/* 邮箱详情 */
.email-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.email-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.email-actions {
  display: flex;
  gap: 12px;
}

.backup-emails {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.no-backup-email {
  padding: 16px 0;
}

.hint-text {
  font-size: 14px;
  color: #86909c;
}

.add-backup-btn {
  margin-top: 8px;
}

/* 通知设置 */
.notification-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f2f3f5;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notification-name {
  font-size: 14px;
  color: #1d2129;
}

.notification-desc {
  font-size: 12px;
  color: #86909c;
}

/* 通知列表 */
.notifications-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notification-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.notif-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #165dff;
  color: #fff;
}

.notif-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.notif-title {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.notif-desc {
  font-size: 13px;
  color: #4e5969;
}

.notif-time {
  font-size: 12px;
  color: #86909c;
}

/* 徽章 */
.badges-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 16px;
}

.badge-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
  text-align: center;
}

.badge-item.locked {
  opacity: 0.6;
}

.badge-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.badge-icon.locked-icon {
  background: #c9cdd4;
}

.badge-name {
  font-size: 13px;
  font-weight: 500;
  color: #1d2129;
}

.badge-desc {
  font-size: 11px;
  color: #86909c;
}

.no-badges {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  color: #86909c;
}

/* 社团列表 */
.clubs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.club-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.club-item:hover {
  background: #e8f3ff;
}

.club-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.club-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.club-members {
  font-size: 12px;
  color: #86909c;
}

.no-clubs {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px;
  color: #86909c;
}

/* 设备管理 */
.devices-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
}

.device-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}

.device-item.current {
  background: #e8f3ff;
  border: 1px solid #bedaff;
}

.device-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #165dff;
}

.device-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.device-detail {
  font-size: 12px;
  color: #86909c;
}

.device-time {
  font-size: 12px;
  color: #00b42a;
}

.logout-all-btn {
  margin-top: 8px;
}

/* 账户操作 */
.account-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 退出登录 */
.logout-section {
  padding: 16px;
  background: #f7f8fa;
}

.logout-section :deep(.arco-btn) {
  background: #fff;
  border: 1px solid #f53f3f;
  color: #f53f3f;
  border-radius: 8px;
  height: 44px;
}

.logout-section :deep(.arco-btn:hover) {
  background: #ffece8;
}

/*  桌面端适配 (>= 1024px)  */
@media (min-width: 1024px) {
  .profile-page {
    padding: 0;
  }

  .user-card {
    padding: 24px 32px 0 32px;
    border-radius: 0;
  }

  .user-header {
    margin-bottom: 0;
  }

  .user-header :deep(.arco-avatar) {
    width: 80px !important;
    height: 80px !important;
    font-size: 32px !important;
  }

  .nickname {
    font-size: 28px;
  }

  .username {
    font-size: 16px;
  }

  /* 桌面端展开按钮显示在右侧 */
  .desktop-expand-btn {
    display: flex;
    flex-shrink: 0;
    width: auto;
    padding: 0 24px;
    height: 40px;
    background: #f2f3f5;
    border: none;
    color: #4e5969;
    font-size: 15px;
  }

  .desktop-expand-btn:hover {
    background: #e5e6eb;
  }

  /* 隐藏移动端展开按钮 */
  .mobile-expand-btn {
    display: none;
  }

  /* 主导航栏样式 */
  .main-tabs {
    margin-top: 16px;
  }

  .main-tabs :deep(.arco-tabs-tab) {
    padding: 14px 20px;
    font-size: 15px;
  }

  /* 内容标签栏样式 */
  .content-tabs :deep(.arco-tabs-nav) {
    padding: 0 32px;
  }

  .content-tabs :deep(.arco-tabs-tab) {
    padding: 14px 20px;
    font-size: 15px;
  }

  .setting-group {
    padding: 24px 32px;
  }

  .setting-title {
    font-size: 20px;
    margin-bottom: 28px;
  }

  .setting-value {
    font-size: 18px;
  }

  .setting-hint {
    font-size: 14px;
  }

  /* 通知/隐私设置项 */
  .notification-name,
  .security-label {
    font-size: 16px;
  }

  .notification-desc,
  .security-hint {
    font-size: 13px;
  }

  /* 表单项 */
  .form-label,
  .info-label {
    font-size: 15px;
    width: 80px;
  }

  .form-value span,
  .info-value {
    font-size: 16px;
  }

  /* 邮箱地址 */
  .email-address {
    font-size: 16px;
  }

  .email-address-lg {
    font-size: 18px;
  }

  /* 关联账户 */
  .linked-name {
    font-size: 16px;
  }

  /* 设置项两列布局 */
  .school-info,
  .contact-info {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px 32px;
  }

  .profile-form {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px 32px;
  }

  /* 通知设置两列 */
  .setting-group .notification-item {
    max-width: 600px;
  }

  /* 关联账户横向排列 */
  .linked-accounts {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 0;
    border: none;
    border-radius: 0;
  }

  .linked-item {
    border: 1px solid #e5e6eb;
    border-radius: 8px;
    margin: 4px;
  }

  .linked-item:last-child {
    border-bottom: 1px solid #e5e6eb;
  }

  /* 徽章更多列 */
  .badges-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }

  /* 登录历史两列 */
  .login-history {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  /* 设备列表两列 */
  .devices-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  /* 社团列表两列 */
  .clubs-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  /* 退出登录按钮 */
  .logout-section {
    padding: 24px 32px;
  }

  .logout-section :deep(.arco-btn) {
    max-width: 320px;
    font-size: 15px;
  }
}

/*  大屏桌面端适配 (>= 1440px)  */
@media (min-width: 1440px) {
  .user-card {
    padding: 28px 48px 0 48px;
  }

  .user-header :deep(.arco-avatar) {
    width: 88px !important;
    height: 88px !important;
    font-size: 36px !important;
  }

  .nickname {
    font-size: 32px;
  }

  .username {
    font-size: 17px;
  }

  /* 主导航栏样式 */
  .main-tabs :deep(.arco-tabs-tab) {
    padding: 16px 24px;
    font-size: 16px;
  }

  /* 内容标签栏样式 */
  .content-tabs :deep(.arco-tabs-nav) {
    padding: 0 48px;
  }

  .content-tabs :deep(.arco-tabs-tab) {
    padding: 16px 24px;
    font-size: 16px;
  }

  .setting-group {
    padding: 28px 48px;
  }

  .setting-title {
    font-size: 22px;
  }

  .setting-value {
    font-size: 20px;
  }

  .school-info,
  .contact-info,
  .profile-form {
    grid-template-columns: repeat(3, 1fr);
  }

  .linked-accounts {
    grid-template-columns: repeat(3, 1fr);
  }

  .login-history,
  .devices-list,
  .clubs-list {
    grid-template-columns: repeat(3, 1fr);
  }

  .logout-section {
    padding: 28px 48px;
  }
}

/*  平板适配 (768px - 1023px)  */
@media (min-width: 768px) and (max-width: 1023px) {
  .user-card {
    padding: 20px 24px;
  }

  .setting-group {
    padding: 24px;
  }

  .linked-accounts {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    border: none;
  }

  .linked-item {
    border: 1px solid #e5e6eb;
    border-radius: 8px;
    margin: 4px;
  }

  .linked-item:last-child {
    border-bottom: 1px solid #e5e6eb;
  }

  .badges-grid {
    grid-template-columns: repeat(4, 1fr);
  }

  .login-history,
  .devices-list,
  .clubs-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
}

/*  移动端适配 (< 768px)  */
@media (max-width: 767px) {
  .user-card {
    padding: 16px;
  }

  .user-header :deep(.arco-avatar) {
    width: 56px !important;
    height: 56px !important;
  }

  .nickname {
    font-size: 20px;
  }

  .icon-nav-item {
    padding: 10px 0;
  }

  .content-tabs {
    padding: 0 12px;
  }

  .content-tab-item {
    padding: 12px 12px;
    font-size: 13px;
  }

  .content-tab-item.active::after {
    left: 12px;
    right: 12px;
  }

  .setting-group {
    padding: 16px;
  }

  .setting-title {
    font-size: 16px;
    margin-bottom: 16px;
  }

  .linked-accounts {
    border-radius: 8px;
  }

  .linked-item {
    padding: 12px 16px;
    gap: 12px;
  }

  .linked-icon {
    width: 36px;
    height: 36px;
  }

  .linked-name {
    font-size: 14px;
  }

  .logout-section {
    padding: 16px;
  }

  .badges-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .badge-item {
    padding: 12px;
  }

  .badge-icon {
    width: 40px;
    height: 40px;
  }

  .info-label {
    width: 50px;
    font-size: 13px;
  }

  .info-value {
    font-size: 13px;
  }
}

/*  小屏手机适配 (< 375px)  */
@media (max-width: 374px) {
  .user-card {
    padding: 12px;
  }

  .user-header {
    gap: 12px;
  }

  .user-header :deep(.arco-avatar) {
    width: 48px !important;
    height: 48px !important;
  }

  .nickname {
    font-size: 18px;
  }

  .content-tab-item {
    padding: 10px 8px;
    font-size: 12px;
  }

  .setting-group {
    padding: 12px;
  }

  .badges-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
