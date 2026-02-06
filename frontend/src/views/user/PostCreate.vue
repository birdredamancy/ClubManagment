<template>
  <div class="post-create-page">
    <div class="page-header">
      <h2>发布话题</h2>
    </div>

    <a-form :model="form" layout="vertical" @submit="handleSubmit">
      <a-form-item field="title" label="标题" :rules="[{ required: true, message: '请输入标题' }]">
        <a-input v-model="form.title" placeholder="请输入标题" :max-length="100" show-word-limit />
      </a-form-item>

      <a-form-item field="clubId" label="所属社团">
        <a-select v-model="form.clubId" placeholder="请选择社团（可选）" allow-clear>
          <a-option v-for="club in clubs" :key="club.id" :value="club.id">
            <span class="club-option">
              <span class="club-dot" :style="{ background: club.color }"></span>
              {{ club.name }}
            </span>
          </a-option>
        </a-select>
      </a-form-item>

      <a-form-item field="content" label="内容" :rules="[{ required: true, message: '请输入内容' }]">
        <a-textarea
          v-model="form.content"
          placeholder="请输入内容，支持 Markdown 格式"
          :auto-size="{ minRows: 8, maxRows: 20 }"
          :max-length="5000"
          show-word-limit
        />
      </a-form-item>

      <a-form-item field="tags" label="标签">
        <a-input-tag v-model="form.tags" placeholder="输入标签后按回车添加" :max-tag-count="5" />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" :loading="submitting">发布</a-button>
          <a-button @click="router.back()">取消</a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { createPost } from '@/api/post'
import { getClubList } from '@/api/club'

const router = useRouter()

const form = reactive({
  title: '',
  content: '',
  clubId: null,
  tags: []
})

const clubs = ref([])
const submitting = ref(false)

async function fetchClubs() {
  try {
    const res = await getClubList()
    clubs.value = res.data
  } catch (error) {
    console.error('获取社团列表失败:', error)
  }
}

async function handleSubmit() {
  if (!form.title.trim() || !form.content.trim()) {
    Message.warning('请填写标题和内容')
    return
  }

  submitting.value = true
  try {
    const res = await createPost({
      title: form.title,
      content: form.content,
      clubId: form.clubId,
      tags: form.tags
    })
    Message.success('发布成功')
    router.push(`/post/${res.data.id}`)
  } catch (error) {
    console.error('发布失败:', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchClubs()
})
</script>

<style scoped>
.post-create-page {
  max-width: 800px;
  margin: 0 auto;
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f2f3f5;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
}

.club-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.club-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
</style>
