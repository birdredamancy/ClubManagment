<template>
  <div class="home-page">
    <!-- 公告横幅 -->
    <div class="announcement-banner">
      <PartyPopper :size="16" />
      <span>欢迎来到校园社团，友善交流、共建社区！</span>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          class="filter-tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
          <span v-if="tab.count" class="tab-count">({{ tab.count }})</span>
        </button>
      </div>
      <button class="create-btn" @click="router.push('/post/create')">
        <PenSquare :size="16" />
        <span>发布话题</span>
      </button>
    </div>

    <!-- 新帖子提示 -->
    <div v-if="newPostCount > 0" class="new-posts-tip" @click="loadNewPosts">
      查看 {{ newPostCount }} 个新的或更新的话题
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <div v-if="loading" class="loading-state">
        <a-spin />
      </div>

      <div v-else-if="posts.length === 0" class="empty-state">
        <a-empty description="暂无话题，快去发布第一条吧！" />
      </div>

      <div v-else>
        <div
          v-for="post in posts"
          :key="post.id"
          class="post-item"
          @click="router.push(`/post/${post.id}`)"
        >
          <!-- 帖子状态标识 -->
          <div v-if="post.pinned" class="post-badge pinned">
            <Pin :size="12" />
          </div>

          <!-- 帖子内容 -->
          <div class="post-content">
            <h3 class="post-title">
              {{ post.title }}
              <Flame v-if="post.isHot" class="hot-tag" :size="14" color="#f53f3f" />
            </h3>

            <!-- 标签 -->
            <div class="post-tags">
              <span class="tag category" :style="{ color: post.categoryColor }">
                <span class="tag-icon" :style="{ background: post.categoryColor }"></span>
                {{ post.categoryName }}
              </span>
              <span v-for="tag in post.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>

            <!-- 帖子摘要 -->
            <p class="post-summary">{{ post.summary }}</p>
          </div>

          <!-- 帖子元信息 -->
          <div class="post-meta">
            <!-- 参与者头像 -->
            <div class="post-avatars">
              <a-avatar
                v-for="(user, index) in post.participants?.slice(0, 4)"
                :key="index"
                :size="24"
                :style="{ marginLeft: index > 0 ? '-8px' : '0' }"
              >
                <img v-if="user.avatar" :src="user.avatar" />
                <span v-else>{{ user.name?.charAt(0) }}</span>
              </a-avatar>
            </div>

            <!-- 统计数据 -->
            <div class="post-stats">
              <span class="stat-item">
                <MessageSquare :size="14" />
                {{ formatNumber(post.replyCount) }}
              </span>
              <span class="stat-item">
                <Eye :size="14" />
                {{ formatNumber(post.viewCount) }}
              </span>
              <span class="stat-item time">{{ post.lastActiveTime }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <a-button long @click="loadMore">加载更多</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { PenSquare, Pin, MessageSquare, Eye, PartyPopper, Flame } from 'lucide-vue-next'
import { getPostList } from '@/api/post'

const router = useRouter()
const route = useRoute()

const activeTab = ref('latest')
const loading = ref(false)
const hasMore = ref(true)
const newPostCount = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const tabs = [
  { key: 'latest', label: '最新' },
  { key: 'new', label: '新帖', count: 0 },
  { key: 'hot', label: '热门' },
  { key: 'my', label: '我的帖子' }
]

const posts = ref([])

// 获取帖子列表
const fetchPosts = async (reset = false) => {
  if (reset) {
    currentPage.value = 1
    posts.value = []
  }

  loading.value = true
  try {
    const params = {
      tab: activeTab.value,
      page: currentPage.value,
      size: pageSize.value
    }

    // 如果有社团筛选
    const clubId = route.query.category
    if (clubId) {
      params.clubId = clubId
    }

    const res = await getPostList(params)
    const newPosts = res.data.records.map(post => ({
      id: post.id,
      title: post.title,
      summary: post.summary,
      categoryName: post.clubName || '综合讨论',
      categoryColor: post.clubColor || '#86909c',
      tags: post.tags || [],
      pinned: post.pinned,
      isHot: post.isHot,
      replyCount: post.commentCount,
      viewCount: post.viewCount,
      lastActiveTime: post.lastActiveTime,
      participants: post.participants || []
    }))

    if (reset) {
      posts.value = newPosts
    } else {
      posts.value = [...posts.value, ...newPosts]
    }

    hasMore.value = currentPage.value < Math.ceil(res.data.total / pageSize.value)
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

const formatNumber = (num) => {
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num
}

const loadNewPosts = () => {
  newPostCount.value = 0
  fetchPosts(true)
}

const loadMore = () => {
  currentPage.value++
  fetchPosts()
}

// 监听tab变化
watch(activeTab, () => {
  fetchPosts(true)
})

// 监听路由query变化（社团筛选）
watch(() => route.query.category, () => {
  fetchPosts(true)
})

onMounted(() => {
  fetchPosts(true)
})
</script>

<style scoped>
.home-page {
  max-width: 900px;
  margin: 0 auto;
}

/* 公告横幅 */
.announcement-banner {
  background: linear-gradient(135deg, #e8f3ff 0%, #f0e8ff 100%);
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #165dff;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 16px;
  border: none;
  background: none;
  font-size: 14px;
  color: #4e5969;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.filter-tab:hover {
  background: #f2f3f5;
}

.filter-tab.active {
  color: #165dff;
  background: #e8f3ff;
  font-weight: 500;
}

.tab-count {
  color: #86909c;
  font-weight: normal;
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #165dff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
}

.create-btn:hover {
  background: #4080ff;
}

/* 新帖子提示 */
.new-posts-tip {
  background: #e8f3ff;
  color: #165dff;
  text-align: center;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 16px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.new-posts-tip:hover {
  background: #d4e8ff;
}

/* 帖子列表 */
.post-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.loading-state,
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.post-item {
  position: relative;
  padding: 16px 20px;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
  transition: background 0.2s;
}

.post-item:hover {
  background: #fafbfc;
}

.post-item:last-child {
  border-bottom: none;
}

.post-badge {
  position: absolute;
  top: 16px;
  left: 8px;
  color: #86909c;
}

.post-badge.pinned {
  color: #ff7d00;
}

.post-content {
  padding-left: 16px;
}

.post-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.hot-tag {
  margin-left: 6px;
  vertical-align: middle;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909c;
  background: #f2f3f5;
  padding: 2px 8px;
  border-radius: 4px;
}

.tag.category {
  background: transparent;
  padding-left: 0;
}

.tag-icon {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.post-summary {
  font-size: 13px;
  color: #86909c;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-left: 16px;
}

.post-avatars {
  display: flex;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909c;
}

.stat-item.time {
  color: #c9cdd4;
}

/* 加载更多 */
.load-more {
  padding: 16px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .home-page {
    padding: 0;
  }

  .announcement-banner {
    border-radius: 0;
    margin-bottom: 12px;
  }

  .filter-bar {
    padding: 0 12px;
  }

  .filter-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
    -webkit-overflow-scrolling: touch;
  }

  .filter-tab {
    white-space: nowrap;
  }

  .create-btn span {
    display: none;
  }

  .create-btn {
    padding: 8px 12px;
  }

  .post-list {
    border-radius: 0;
  }

  .post-item {
    padding: 12px 16px;
  }

  .post-summary {
    -webkit-line-clamp: 1;
  }

  .post-stats {
    gap: 12px;
  }
}
</style>
