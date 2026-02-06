<template>
  <div class="search-page">
    <div class="search-header">
      <div class="search-input-wrapper">
        <Search :size="20" />
        <input
          v-model="keyword"
          type="text"
          placeholder="搜索帖子、用户..."
          @keyup.enter="handleSearch"
          autofocus
        />
        <button v-if="keyword" class="clear-btn" @click="keyword = ''">
          <X :size="16" />
        </button>
      </div>
    </div>

    <div class="search-content">
      <div v-if="!keyword && !hasSearched" class="search-tip">
        <p>输入关键词搜索帖子或用户</p>
      </div>

      <div v-else-if="loading" class="loading-state">
        <a-spin />
      </div>

      <div v-else-if="results.length === 0 && hasSearched" class="empty-state">
        <a-empty :description="`未找到与\"${searchedKeyword}\"相关的结果`" />
      </div>

      <div v-else class="results-list">
        <h3 class="results-title">搜索结果</h3>
        <div v-for="item in results" :key="item.id" class="result-item" @click="goToResult(item)">
          <div class="result-icon">
            <FileText v-if="item.type === 'post'" :size="18" />
            <User v-else :size="18" />
          </div>
          <div class="result-content">
            <h4>{{ item.title || item.nickname }}</h4>
            <p>{{ item.summary || item.bio || '暂无简介' }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, X, FileText, User } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const searchedKeyword = ref('')
const loading = ref(false)
const hasSearched = ref(false)
const results = ref([])

const handleSearch = async () => {
  if (!keyword.value.trim()) return

  loading.value = true
  hasSearched.value = true
  searchedKeyword.value = keyword.value

  try {
    // TODO: 调用搜索API
    await new Promise(resolve => setTimeout(resolve, 500))
    results.value = []
  } finally {
    loading.value = false
  }
}

const goToResult = (item) => {
  if (item.type === 'post') {
    router.push(`/post/${item.id}`)
  } else {
    router.push(`/user/${item.id}`)
  }
}

onMounted(() => {
  if (route.query.q) {
    keyword.value = route.query.q
    handleSearch()
  }
})
</script>

<style scoped>
.search-page {
  max-width: 700px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 24px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #fff;
  border: 2px solid #165dff;
  border-radius: 12px;
  padding: 12px 16px;
}

.search-input-wrapper input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 16px;
  color: #1d2129;
}

.search-input-wrapper input::placeholder {
  color: #c9cdd4;
}

.clear-btn {
  background: none;
  border: none;
  padding: 4px;
  cursor: pointer;
  color: #86909c;
  display: flex;
}

.search-content {
  background: #fff;
  border-radius: 12px;
  min-height: 300px;
}

.search-tip,
.loading-state,
.empty-state {
  padding: 60px 20px;
  text-align: center;
  color: #86909c;
}

.results-list {
  padding: 16px;
}

.results-title {
  font-size: 14px;
  color: #86909c;
  margin: 0 0 16px 0;
  font-weight: normal;
}

.result-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.result-item:hover {
  background: #f7f8fa;
}

.result-icon {
  width: 40px;
  height: 40px;
  background: #f2f3f5;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #86909c;
}

.result-content h4 {
  font-size: 15px;
  font-weight: 500;
  color: #1d2129;
  margin: 0 0 4px 0;
}

.result-content p {
  font-size: 13px;
  color: #86909c;
  margin: 0;
}
</style>
