<template>
  <Teleport to="body">
    <Transition name="popup">
      <div v-if="visible" class="chat-popup" :class="{ minimized: isMinimized }">
        <!-- 头部 -->
        <div class="popup-header">
          <span class="popup-title">聊天</span>
          <div class="popup-actions">
            <button @click="isMinimized = !isMinimized">
              <Minus v-if="!isMinimized" :size="16" />
              <Maximize2 v-else :size="16" />
            </button>
            <button @click="emit('update:visible', false)">
              <X :size="16" />
            </button>
          </div>
        </div>

        <!-- 内容区 -->
        <div class="popup-body" v-show="!isMinimized">
          <!-- Tab 切换 -->
          <div class="popup-tabs">
            <div
              class="tab-item"
              :class="{ active: activeTab === 'channels' }"
              @click="activeTab = 'channels'"
            >
              <Hash :size="16" />
              <span>频道</span>
            </div>
            <div
              class="tab-item"
              :class="{ active: activeTab === 'direct' }"
              @click="activeTab = 'direct'"
            >
              <Users :size="16" />
              <span>私信</span>
            </div>
          </div>

          <!-- 频道列表 -->
          <div v-if="activeTab === 'channels'" class="chat-list">
            <div v-if="channels.length === 0" class="empty-tip">
              暂无频道
            </div>
            <div
              v-for="channel in channels"
              :key="channel.id"
              class="chat-item"
              @click="openChat(channel)"
            >
              <div class="chat-icon channel">
                <Hash :size="16" />
              </div>
              <div class="chat-info">
                <span class="chat-name">{{ channel.name }}</span>
                <span class="chat-preview">{{ channel.lastMessage }}</span>
              </div>
              <span class="chat-time">{{ channel.time }}</span>
            </div>
          </div>

          <!-- 私信列表 -->
          <div v-if="activeTab === 'direct'" class="chat-list">
            <div v-if="directMessages.length === 0" class="empty-tip">
              暂无私信
            </div>
            <div
              v-for="dm in directMessages"
              :key="dm.id"
              class="chat-item"
              @click="openChat(dm)"
            >
              <a-avatar :size="36">
                <img v-if="dm.avatar" :src="dm.avatar" />
                <span v-else>{{ dm.name.charAt(0) }}</span>
              </a-avatar>
              <div class="chat-info">
                <span class="chat-name">{{ dm.name }}</span>
                <span class="chat-preview">{{ dm.lastMessage }}</span>
              </div>
              <div class="chat-meta">
                <span class="chat-time">{{ dm.time }}</span>
                <span v-if="dm.unread" class="unread-badge">{{ dm.unread }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部导航（移动端样式） -->
        <div class="popup-footer" v-show="!isMinimized">
          <button class="footer-btn" :class="{ active: activeTab === 'channels' }" @click="activeTab = 'channels'">
            <Hash :size="18" />
            <span>频道</span>
          </button>
          <button class="footer-btn" :class="{ active: activeTab === 'direct' }" @click="activeTab = 'direct'">
            <Users :size="18" />
            <span>私信</span>
          </button>
          <button class="footer-btn">
            <Search :size="18" />
            <span>搜索</span>
          </button>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Minus, Maximize2, X, Hash, Users, Search } from 'lucide-vue-next'
import { getChannels, getDirectMessages } from '@/api/chat'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  visible: Boolean
})

const emit = defineEmits(['update:visible'])

const userStore = useUserStore()
const isMinimized = ref(false)
const activeTab = ref('channels')
const loading = ref(false)

const channels = ref([])
const directMessages = ref([])

// 获取频道列表
const fetchChannels = async () => {
  if (!userStore.isLoggedIn) {
    // 未登录时显示默认数据
    channels.value = [
      { id: 1, name: '常规频道', lastMessage: '欢迎加入社团！', time: '12:33' }
    ]
    return
  }

  try {
    const res = await getChannels()
    channels.value = res.data.map(room => ({
      id: room.id,
      name: room.name,
      lastMessage: room.lastMessage || '暂无消息',
      time: room.lastMessageTimeText || '',
      avatar: room.avatar,
      unread: room.unreadCount
    }))
  } catch (error) {
    console.error('获取频道列表失败:', error)
    // 使用默认数据
    channels.value = [
      { id: 1, name: '常规频道', lastMessage: '欢迎加入社团！', time: '12:33' }
    ]
  }
}

// 获取私信列表
const fetchDirectMessages = async () => {
  if (!userStore.isLoggedIn) {
    directMessages.value = []
    return
  }

  try {
    const res = await getDirectMessages()
    directMessages.value = res.data.map(room => ({
      id: room.id,
      name: room.name,
      lastMessage: room.lastMessage || '暂无消息',
      time: room.lastMessageTimeText || '',
      avatar: room.avatar,
      unread: room.unreadCount
    }))
  } catch (error) {
    console.error('获取私信列表失败:', error)
    directMessages.value = []
  }
}

// 加载聊天数据
const loadChatData = async () => {
  loading.value = true
  await Promise.all([fetchChannels(), fetchDirectMessages()])
  loading.value = false
}

const openChat = (chat) => {
  // TODO: 打开聊天详情页面
  console.log('Open chat:', chat)
}

// 监听弹窗可见性
watch(() => props.visible, (newVal) => {
  if (newVal) {
    loadChatData()
  }
})
</script>

<style scoped>
.chat-popup {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 420px;
  height: 450px;
  max-height: 80vh;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  z-index: 2000;
  overflow: hidden;
}

.chat-popup.minimized {
  max-height: auto;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f7f8fa;
  border-bottom: 1px solid #e5e6eb;
}

.popup-title {
  font-weight: 600;
  font-size: 15px;
  color: #1d2129;
}

.popup-actions {
  display: flex;
  gap: 4px;
}

.popup-actions button {
  background: none;
  border: none;
  padding: 6px;
  cursor: pointer;
  color: #86909c;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popup-actions button:hover {
  background: #e5e6eb;
  color: #4e5969;
}

.popup-body {
  flex: 1;
  overflow-y: auto;
}

.popup-tabs {
  display: flex;
  border-bottom: 1px solid #e5e6eb;
  padding: 0 12px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  font-size: 14px;
  color: #86909c;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
}

.tab-item:hover {
  color: #4e5969;
}

.tab-item.active {
  color: #165dff;
  border-bottom-color: #165dff;
}

.chat-list {
  padding: 8px;
}

.empty-tip {
  text-align: center;
  padding: 40px 20px;
  color: #c9cdd4;
  font-size: 14px;
}

.chat-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.chat-item:hover {
  background: #f7f8fa;
}

.chat-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.chat-icon.channel {
  background: #00b42a;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.chat-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.chat-preview {
  display: block;
  font-size: 12px;
  color: #86909c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.chat-time {
  font-size: 12px;
  color: #c9cdd4;
}

.unread-badge {
  background: #f53f3f;
  color: #fff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

.popup-footer {
  display: none;
  border-top: 1px solid #e5e6eb;
  padding: 8px;
  gap: 4px;
}

.footer-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 8px;
  background: none;
  border: none;
  border-radius: 8px;
  color: #86909c;
  font-size: 11px;
  cursor: pointer;
}

.footer-btn.active {
  color: #165dff;
  background: #e8f3ff;
}

/* 动画 */
.popup-enter-active,
.popup-leave-active {
  transition: all 0.3s ease;
}

.popup-enter-from,
.popup-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* 移动端 */
@media (max-width: 768px) {
  .chat-popup {
    bottom: 0;
    right: 0;
    left: 0;
    width: 100%;
    max-height: 70vh;
    border-radius: 16px 16px 0 0;
  }

  .popup-tabs {
    display: none;
  }

  .popup-footer {
    display: flex;
  }
}
</style>
