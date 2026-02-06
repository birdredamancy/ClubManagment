<template>
  <div class="notifications-page">
    <div class="page-header">
      <h2 class="header-title">系统通知</h2>
      <a-button v-if="notificationStore.unreadCount > 0" type="text" size="small" @click="handleMarkAllRead">
        全部已读
      </a-button>
    </div>

    <!-- 通知列表 -->
    <div class="notification-list">
      <div v-if="notificationStore.notifications.length === 0" class="empty-state">
        <Bell :size="48" color="#c9cdd4" />
        <p>暂无通知</p>
      </div>

      <div
        v-for="item in notificationStore.notifications"
        :key="item.id"
        class="notification-item"
        :class="{ unread: !item.isRead }"
        @click="handleItemClick(item)"
      >
        <div class="notification-icon" :class="item.type">
          <Sparkles v-if="item.type === 'ai_summary'" :size="20" />
          <Bell v-else :size="20" />
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-title">{{ item.title }}</span>
            <span class="notification-time">{{ formatTime(item.createdAt) }}</span>
          </div>
          <p class="notification-text">{{ item.content?.slice(0, 100) }}{{ item.content?.length > 100 ? '...' : '' }}</p>
        </div>
        <div v-if="!item.isRead" class="unread-dot"></div>
      </div>
    </div>

    <!-- AI总结详情弹窗 -->
    <a-modal
      v-model:visible="summaryModalVisible"
      title="AI社团动态总结"
      :footer="false"
      :width="360"
    >
      <div v-if="currentSummary" class="summary-detail">
        <div class="summary-meta">
          <Sparkles :size="16" color="#165dff" />
          <span>{{ formatDate(currentSummary.createdAt) }}</span>
        </div>
        <div class="summary-content">{{ currentSummary.content || currentSummary.data?.content }}</div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useNotificationStore } from '@/stores/notification'
import { Bell, Sparkles } from 'lucide-vue-next'

const notificationStore = useNotificationStore()

const summaryModalVisible = ref(false)
const currentSummary = ref(null)

const formatTime = (dateStr) => {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`

  return date.toLocaleDateString()
}

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleItemClick = (item) => {
  notificationStore.markAsRead(item.id)

  if (item.type === 'ai_summary') {
    currentSummary.value = item
    summaryModalVisible.value = true
  }
}

const handleMarkAllRead = () => {
  notificationStore.markAllAsRead()
}
</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  background: #f5f6f7;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
}

.notification-list {
  padding: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #86909c;
}

.empty-state p {
  margin: 16px 0 0 0;
  font-size: 14px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  margin-bottom: 10px;
  position: relative;
  cursor: pointer;
  transition: background 0.2s;
}

.notification-item:active {
  background: #f7f8fa;
}

.notification-item.unread {
  background: #f0f5ff;
}

.notification-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #e8f3ff;
  color: #165dff;
}

.notification-icon.ai_summary {
  background: linear-gradient(135deg, #e8f3ff, #f0e8ff);
  color: #722ed1;
}

.notification-icon.system {
  background: #fff7e8;
  color: #ff7d00;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.notification-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
}

.notification-time {
  font-size: 12px;
  color: #c9cdd4;
  flex-shrink: 0;
}

.notification-text {
  font-size: 13px;
  color: #86909c;
  margin: 0;
  line-height: 1.5;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: #165dff;
  border-radius: 50%;
  position: absolute;
  top: 16px;
  right: 16px;
}

.summary-detail {
  padding: 8px 0;
}

.summary-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  color: #86909c;
  font-size: 14px;
}

.summary-content {
  font-size: 15px;
  line-height: 1.8;
  color: #4e5969;
  white-space: pre-wrap;
}
</style>
