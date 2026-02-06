import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 通知消息 Store
 * 用于管理系统通知、AI总结推送等消息
 */
export const useNotificationStore = defineStore('notification', () => {
  // 通知列表
  const notifications = ref([])

  // 未读数量
  const unreadCount = computed(() => {
    return notifications.value.filter(n => !n.isRead).length
  })

  // 添加通知
  function addNotification(notification) {
    notifications.value.unshift({
      id: Date.now(),
      isRead: false,
      createdAt: new Date().toISOString(),
      ...notification
    })
  }

  // 添加AI总结通知（首次登录时调用）
  function addAiSummaryNotification(summary) {
    addNotification({
      type: 'ai_summary',
      title: '今日社团动态总结',
      content: summary.content,
      icon: 'sparkles',
      data: summary
    })
  }

  // 添加系统通知
  function addSystemNotification(title, content) {
    addNotification({
      type: 'system',
      title,
      content,
      icon: 'bell'
    })
  }

  // 标记为已读
  function markAsRead(id) {
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = true
    }
  }

  // 标记全部已读
  function markAllAsRead() {
    notifications.value.forEach(n => {
      n.isRead = true
    })
  }

  // 删除通知
  function removeNotification(id) {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      notifications.value.splice(index, 1)
    }
  }

  // 清空所有通知
  function clearAll() {
    notifications.value = []
  }

  // 检查今日是否已推送AI总结（用于首次登录判断）
  const lastAiSummaryDate = ref(localStorage.getItem('lastAiSummaryDate') || '')

  function shouldShowAiSummary() {
    const today = new Date().toDateString()
    return lastAiSummaryDate.value !== today
  }

  function markAiSummaryShown() {
    const today = new Date().toDateString()
    lastAiSummaryDate.value = today
    localStorage.setItem('lastAiSummaryDate', today)
  }

  return {
    notifications,
    unreadCount,
    addNotification,
    addAiSummaryNotification,
    addSystemNotification,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll,
    shouldShowAiSummary,
    markAiSummaryShown
  }
})
