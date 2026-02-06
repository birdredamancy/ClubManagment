<template>
  <div class="user-detail-page">
    <div class="page-header">
      <a-button type="text" @click="router.back()">
        <ArrowLeft :size="20" />
      </a-button>
      <span class="header-title">用户主页</span>
      <div style="width: 32px"></div>
    </div>

    <a-spin :loading="loading" style="width: 100%">
      <div v-if="userInfo" class="user-content">
        <!-- 用户信息卡片 -->
        <div class="user-card">
          <a-avatar :size="72">
            <img v-if="userInfo.avatar" :src="userInfo.avatar" alt="avatar" />
            <span v-else>{{ userInfo.nickname?.charAt(0) || 'U' }}</span>
          </a-avatar>
          <h2 class="user-name">{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="user-id">@{{ userInfo.username }}</p>
          <p v-if="userInfo.bio" class="user-bio">{{ userInfo.bio }}</p>
          <div class="user-tags">
            <a-tag v-if="userInfo.major" color="arcoblue">{{ userInfo.major }}</a-tag>
            <a-tag v-if="userInfo.grade" color="green">{{ userInfo.grade }}</a-tag>
          </div>
        </div>

        <!-- 用户动态 -->
        <div class="section">
          <h3 class="section-title">TA的动态</h3>
          <div v-if="postList.length === 0" class="empty-state">
            <a-empty description="暂无动态" />
          </div>
          <div v-else class="post-list">
            <div v-for="post in postList" :key="post.postId" class="post-item" @click="goToPost(post.postId)">
              <h4 class="post-title">{{ post.title }}</h4>
              <p class="post-summary">{{ post.content?.slice(0, 80) }}...</p>
              <div class="post-meta">
                <span>{{ post.createdAt }}</span>
                <span><Eye :size="14" /> {{ post.viewCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { ArrowLeft, Eye } from 'lucide-vue-next'
import { getPublicUserProfile } from '@/api/user'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const userInfo = ref(null)
const postList = ref([])

const fetchUserInfo = async () => {
  const userId = route.params.userId
  if (!userId) return

  loading.value = true
  try {
    const res = await getPublicUserProfile(userId)
    userInfo.value = res.data
  } catch (error) {
    Message.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

const goToPost = (postId) => {
  router.push({ name: 'PostDetail', params: { id: postId } })
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.user-detail-page {
  min-height: 100vh;
  background: #f5f6f7;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
}

.user-content {
  padding: 16px;
}

.user-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  margin-bottom: 16px;
}

.user-name {
  font-size: 20px;
  font-weight: 600;
  margin: 12px 0 4px 0;
}

.user-id {
  font-size: 14px;
  color: #86909c;
  margin: 0 0 12px 0;
}

.user-bio {
  font-size: 14px;
  color: #4e5969;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.section {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px 0;
}

.empty-state {
  padding: 40px 0;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-item {
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.post-item:active {
  background: #e5e6eb;
}

.post-title {
  font-size: 15px;
  font-weight: 500;
  margin: 0 0 6px 0;
}

.post-summary {
  font-size: 13px;
  color: #86909c;
  margin: 0 0 8px 0;
  line-height: 1.5;
}

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c9cdd4;
}

.post-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
