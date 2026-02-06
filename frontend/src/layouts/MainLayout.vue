<template>
  <div class="main-layout" :class="{ 'sidebar-open': sidebarOpen }">
    <!-- 顶部导航栏 -->
    <header class="top-navbar">
      <div class="navbar-left">
        <button class="menu-btn" @click="toggleSidebar">
          <Menu :size="22" />
        </button>
        <div class="logo" @click="router.push('/')">
          <div class="logo-icon">
            <GraduationCap :size="24" color="#165dff" />
          </div>
          <span class="logo-text">校园社团</span>
        </div>
      </div>

      <div class="navbar-center">
        <div class="search-box">
          <Search :size="18" />
          <input type="text" placeholder="搜索帖子、用户..." v-model="searchKeyword" @keyup.enter="handleSearch" />
        </div>
      </div>

      <div class="navbar-right">
        <button class="nav-icon-btn" @click="toggleChatPopup">
          <MessageCircle :size="20" />
          <span v-if="notificationStore.unreadCount > 0" class="badge">{{ notificationStore.unreadCount }}</span>
        </button>
        <a-dropdown trigger="click">
          <a-avatar :size="32" class="user-avatar">
            <img v-if="userStore.avatar" :src="userStore.avatar" alt="avatar" />
            <span v-else>{{ (userStore.nickname || 'U').charAt(0) }}</span>
          </a-avatar>
          <template #content>
            <a-doption @click="router.push('/profile')">
              <User :size="16" /> 个人中心
            </a-doption>
            <a-doption @click="router.push('/profile/edit')">
              <Settings :size="16" /> 编辑资料
            </a-doption>
            <a-divider style="margin: 4px 0" />
            <a-doption @click="handleLogout">
              <LogOut :size="16" /> 退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </header>

    <!-- 侧边栏遮罩（移动端） -->
    <div class="sidebar-overlay" v-if="sidebarOpen" @click="sidebarOpen = false"></div>

    <!-- 左侧边栏 -->
    <aside class="sidebar">
      <nav class="sidebar-nav">
        <div class="nav-section">
          <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
            <LayoutList :size="18" />
            <span>话题</span>
          </router-link>
          <router-link to="/my-posts" class="nav-item" :class="{ active: route.path === '/my-posts' }">
            <FileText :size="18" />
            <span>我的帖子</span>
          </router-link>
          <router-link to="/notifications" class="nav-item" :class="{ active: route.path === '/notifications' }">
            <Bell :size="18" />
            <span>我的消息</span>
            <span v-if="notificationStore.unreadCount > 0" class="nav-badge">{{ notificationStore.unreadCount }}</span>
          </router-link>
        </div>

        <div class="nav-section">
          <div class="section-title" @click="categoryExpanded = !categoryExpanded">
            <ChevronDown :size="16" :class="{ rotated: !categoryExpanded }" />
            <span>社团分类</span>
          </div>
          <div class="category-list" v-show="categoryExpanded">
            <a class="nav-item category-item" v-for="cat in categories" :key="cat.id" @click="filterByCategory(cat.id)">
              <span class="category-icon" :style="{ background: cat.color }"></span>
              <span>{{ cat.name }}</span>
            </a>
          </div>
        </div>
      </nav>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <button class="chat-btn" @click="toggleChatPopup">
          <MessageCircle :size="18" />
          <span>聊天</span>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <router-view />
    </main>

    <!-- 聊天弹窗 -->
    <ChatPopup v-model:visible="chatPopupVisible" />
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notification'
import {
  Menu, Search, MessageCircle, User, Settings, LogOut,
  LayoutList, FileText, Bell, ChevronDown, GraduationCap
} from 'lucide-vue-next'
import ChatPopup from '@/components/ChatPopup.vue'
import { getClubList } from '@/api/club'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const sidebarOpen = ref(false)
const chatPopupVisible = ref(false)
const searchKeyword = ref('')
const categoryExpanded = ref(true)

const categories = ref([])

// 获取社团列表
const fetchClubs = async () => {
  try {
    const res = await getClubList()
    categories.value = res.data.map(club => ({
      id: club.id,
      name: club.name,
      color: club.color || '#86909c'
    }))
  } catch (error) {
    console.error('获取社团列表失败:', error)
    // 使用默认数据
    categories.value = [
      { id: 1, name: '篮球社', color: '#ff7d00' },
      { id: 2, name: '摄影社', color: '#00b42a' },
      { id: 3, name: '读书会', color: '#165dff' },
      { id: 4, name: '音乐社', color: '#722ed1' },
      { id: 5, name: '编程社', color: '#f53f3f' }
    ]
  }
}

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const toggleChatPopup = () => {
  chatPopupVisible.value = !chatPopupVisible.value
}

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/search', query: { q: searchKeyword.value } })
  }
}

const filterByCategory = (categoryId) => {
  router.push({ path: '/', query: { category: categoryId } })
  sidebarOpen.value = false
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

// 路由变化时关闭侧边栏（移动端）
watch(() => route.path, () => {
  sidebarOpen.value = false
})

onMounted(() => {
  fetchClubs()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background: #f0f2f5;
}

/* 顶部导航栏 */
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  z-index: 1000;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-btn {
  display: none;
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
  color: #4e5969;
  border-radius: 8px;
}

.menu-btn:hover {
  background: #f2f3f5;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.logo-icon {
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.navbar-center {
  flex: 1;
  max-width: 480px;
  margin: 0 24px;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f2f3f5;
  border-radius: 20px;
  padding: 8px 16px;
  color: #86909c;
}

.search-box input {
  flex: 1;
  border: none;
  background: none;
  outline: none;
  font-size: 14px;
  color: #1d2129;
}

.search-box input::placeholder {
  color: #c9cdd4;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-icon-btn {
  position: relative;
  background: none;
  border: none;
  padding: 8px;
  cursor: pointer;
  color: #4e5969;
  border-radius: 8px;
}

.nav-icon-btn:hover {
  background: #f2f3f5;
}

.nav-icon-btn .badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: #f53f3f;
  color: #fff;
  font-size: 10px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-avatar {
  cursor: pointer;
}

/* 侧边栏 */
.sidebar {
  position: fixed;
  top: 56px;
  left: 0;
  bottom: 0;
  width: 220px;
  background: #fff;
  border-right: 1px solid #e5e6eb;
  display: flex;
  flex-direction: column;
  z-index: 900;
  overflow-y: auto;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 0;
}

.nav-section {
  padding: 8px 12px;
}

.nav-section + .nav-section {
  border-top: 1px solid #f2f3f5;
  margin-top: 8px;
  padding-top: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909c;
  padding: 8px;
  cursor: pointer;
  user-select: none;
}

.section-title svg {
  transition: transform 0.2s;
}

.section-title svg.rotated {
  transform: rotate(-90deg);
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  color: #4e5969;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.nav-item:hover {
  background: #f2f3f5;
}

.nav-item.active {
  background: #e8f3ff;
  color: #165dff;
}

.nav-badge {
  margin-left: auto;
  background: #f53f3f;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
}

.category-item {
  padding-left: 20px;
}

.category-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid #f2f3f5;
}

.chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 10px;
  background: #f2f3f5;
  border: none;
  border-radius: 8px;
  color: #4e5969;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-btn:hover {
  background: #e5e6eb;
}

/* 主内容区 */
.main-content {
  margin-left: 220px;
  margin-top: 56px;
  min-height: calc(100vh - 56px);
  padding: 20px;
}

/* 遮罩层 */
.sidebar-overlay {
  display: none;
}

/* 响应式 - 移动端 */
@media (max-width: 768px) {
  .menu-btn {
    display: flex;
  }

  .navbar-center {
    display: none;
  }

  .logo-text {
    display: none;
  }

  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }

  .sidebar-open .sidebar {
    transform: translateX(0);
  }

  .sidebar-overlay {
    display: block;
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 800;
  }

  .main-content {
    margin-left: 0;
    padding: 16px;
  }
}

/* 平板端 */
@media (min-width: 769px) and (max-width: 1024px) {
  .sidebar {
    width: 200px;
  }

  .main-content {
    margin-left: 200px;
  }
}
</style>
