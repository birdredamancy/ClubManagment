<template>
  <div class="my-posts-page">
    <div class="page-header">
      <h2>我的帖子</h2>
    </div>

    <div class="post-list">
      <a-empty v-if="posts.length === 0" description="你还没有发布过帖子" />

      <div v-else>
        <div v-for="post in posts" :key="post.id" class="post-item" @click="router.push(`/post/${post.id}`)">
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-summary">{{ post.summary }}</p>
          <div class="post-meta">
            <span>{{ post.createdAt }}</span>
            <span>{{ post.viewCount }} 浏览</span>
            <span>{{ post.replyCount }} 回复</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const posts = ref([])
</script>

<style scoped>
.my-posts-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
}

.post-list {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.post-item {
  padding: 16px 0;
  border-bottom: 1px solid #f2f3f5;
  cursor: pointer;
}

.post-item:last-child {
  border-bottom: none;
}

.post-item:hover {
  background: #fafbfc;
  margin: 0 -20px;
  padding: 16px 20px;
}

.post-title {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
  margin: 0 0 8px 0;
}

.post-summary {
  font-size: 13px;
  color: #86909c;
  margin: 0 0 8px 0;
}

.post-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #c9cdd4;
}
</style>
