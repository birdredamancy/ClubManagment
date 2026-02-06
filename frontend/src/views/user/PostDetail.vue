<template>
  <div class="post-detail-page">
    <!-- 顶部导航栏 -->
    <div class="top-nav-bar">
      <a-button type="text" class="back-btn" @click="handleBack">
        <template #icon><icon-left :size="18" /></template>
      </a-button>
      <span class="nav-title">帖子详情</span>
      <div class="nav-right">
        <a-button type="text">
          <template #icon><icon-more :size="18" /></template>
        </a-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <a-spin size="large" />
    </div>

    <template v-else-if="post">
      <!-- 帖子头部 -->
      <div class="post-header">
        <h1 class="post-title">{{ post.title }}</h1>
        <div class="post-meta">
          <span class="tag category" :style="{ color: post.clubColor }" v-if="post.clubName">
            <span class="tag-dot" :style="{ background: post.clubColor }"></span>
            {{ post.clubName }}
          </span>
          <a-tag v-for="tag in post.tags" :key="tag" size="small">{{ tag }}</a-tag>
        </div>
      </div>

      <!-- 帖子内容区 -->
      <div class="post-content-wrapper">
        <!-- 主贴 -->
        <div class="main-post">
          <div class="post-author">
            <a-avatar :size="48">
              <img v-if="post.authorAvatar" :src="post.authorAvatar" />
              <span v-else>{{ (post.authorName || '匿').charAt(0) }}</span>
            </a-avatar>
            <div class="author-info">
              <span class="author-name">{{ post.authorName }}</span>
              <span v-if="post.isOwner" class="owner-badge">楼主</span>
              <span class="post-time">{{ post.createdAt }}</span>
            </div>
          </div>

          <!-- 帖子正文 -->
          <div class="post-body" v-html="renderedContent"></div>

          <!-- 帖子图片 -->
          <div class="post-images" v-if="post.images && post.images.length > 0">
            <a-image-preview-group>
              <a-image
                v-for="(img, index) in post.images"
                :key="index"
                :src="img"
                :width="200"
                fit="cover"
              />
            </a-image-preview-group>
          </div>

          <!-- 帖子操作栏 -->
          <div class="post-actions">
            <div class="action-left">
              <a-button type="text" @click="handleLike">
                <template #icon><Heart :size="16" :fill="post.isLiked ? '#f53f3f' : 'none'" :color="post.isLiked ? '#f53f3f' : '#86909c'" /></template>
                {{ post.likeCount }}
              </a-button>
              <a-button type="text">
                <template #icon><Link2 :size="16" /></template>
              </a-button>
              <a-button type="text">
                <template #icon><MoreHorizontal :size="16" /></template>
              </a-button>
            </div>
            <div class="action-right">
              <a-button type="text">
                <template #icon><Reply :size="16" /></template>
                回复
              </a-button>
            </div>
          </div>
        </div>

        <!-- 帖子统计 -->
        <div class="post-stats-bar">
          <span class="stat-item">{{ post.viewCount }} 浏览量</span>
          <span class="stat-item">{{ post.likeCount }} 赞</span>
          <span class="stat-item">{{ post.commentCount }} 回复</span>
          <div class="stat-avatars">
            <a-avatar-group :size="24" :max-count="6">
              <a-avatar v-for="(user, index) in post.participants" :key="index">
                <img v-if="user.avatar" :src="user.avatar" />
                <span v-else>{{ user.name?.charAt(0) }}</span>
              </a-avatar>
            </a-avatar-group>
          </div>
        </div>

        <!-- 评论列表 -->
        <div class="comments-section">
          <div
            v-for="(comment, index) in comments"
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-author">
              <a-avatar :size="40">
                <img v-if="comment.authorAvatar" :src="comment.authorAvatar" />
                <span v-else>{{ (comment.authorName || '匿').charAt(0) }}</span>
              </a-avatar>
              <div class="author-info">
                <div class="author-line">
                  <span class="author-name">{{ comment.authorName }}</span>
                  <span v-if="comment.isOwner" class="owner-badge">楼主</span>
                </div>
                <span class="comment-time">{{ comment.createdAt }}</span>
              </div>
              <span class="floor-number">#{{ index + 1 }}</span>
            </div>

            <!-- 评论内容 - 编辑模式 -->
            <div v-if="editingComment && editingComment.id === comment.id" class="comment-edit-box">
              <a-textarea
                v-model="editContent"
                :auto-size="{ minRows: 2, maxRows: 6 }"
                placeholder="编辑评论内容..."
              />
              <div class="edit-actions">
                <a-button size="small" @click="cancelEdit">取消</a-button>
                <a-button type="primary" size="small" :disabled="!editContent.trim()" @click="saveEditComment">保存</a-button>
              </div>
            </div>
            <!-- 评论内容 - 正常显示 -->
            <div v-else class="comment-body" v-html="renderContent(comment.content)"></div>

            <!-- 评论操作 -->
            <div class="comment-actions" v-if="!editingComment || editingComment.id !== comment.id">
              <a-button type="text" size="small" @click="handleCommentLike(comment)">
                <template #icon><Heart :size="14" :fill="comment.isLiked ? '#f53f3f' : 'none'" :color="comment.isLiked ? '#f53f3f' : '#86909c'" /></template>
                {{ comment.likeCount || '' }}
              </a-button>
              <a-button type="text" size="small">
                <template #icon><Link2 :size="14" /></template>
              </a-button>
              <!-- 更多操作菜单 -->
              <a-dropdown v-if="isMyComment(comment)" trigger="click">
                <a-button type="text" size="small">
                  <template #icon><MoreHorizontal :size="14" /></template>
                </a-button>
                <template #content>
                  <a-doption @click="startEditComment(comment)">
                    <template #icon><Pencil :size="14" /></template>
                    编辑
                  </a-doption>
                  <a-doption class="danger-option" @click="handleDeleteComment(comment)">
                    <template #icon><Trash2 :size="14" /></template>
                    删除
                  </a-doption>
                </template>
              </a-dropdown>
              <a-button v-else type="text" size="small">
                <template #icon><MoreHorizontal :size="14" /></template>
              </a-button>
              <a-button type="text" size="small" @click="replyTo(comment)">
                <template #icon><Reply :size="14" /></template>
                回复
              </a-button>
            </div>

            <!-- 子评论/回复 -->
            <div class="sub-comments" v-if="comment.replies && comment.replies.length > 0">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="sub-comment-item"
              >
                <a-avatar :size="28">
                  <img v-if="reply.authorAvatar" :src="reply.authorAvatar" />
                  <span v-else>{{ (reply.authorName || '匿').charAt(0) }}</span>
                </a-avatar>
                <div class="sub-comment-content">
                  <span class="author-name">{{ reply.authorName }}</span>
                  <span v-if="reply.replyTo" class="reply-to">回复 <span class="reply-name">@{{ reply.replyTo }}</span></span>
                  <!-- 子评论编辑模式 -->
                  <template v-if="editingComment && editingComment.id === reply.id">
                    <div class="sub-comment-edit-box">
                      <a-textarea
                        v-model="editContent"
                        :auto-size="{ minRows: 1, maxRows: 4 }"
                        size="small"
                      />
                      <div class="edit-actions">
                        <a-button size="mini" @click="cancelEdit">取消</a-button>
                        <a-button type="primary" size="mini" :disabled="!editContent.trim()" @click="saveEditComment">保存</a-button>
                      </div>
                    </div>
                  </template>
                  <span v-else class="reply-text">{{ reply.content }}</span>
                  <div class="sub-comment-meta" v-if="!editingComment || editingComment.id !== reply.id">
                    <span class="comment-time">{{ reply.createdAt }}</span>
                    <a-button type="text" size="mini" @click="replyTo(reply, comment)">回复</a-button>
                    <template v-if="isMyComment(reply)">
                      <a-button type="text" size="mini" @click="startEditComment(reply)">编辑</a-button>
                      <a-button type="text" size="mini" class="danger-btn" @click="handleDeleteComment(reply)">删除</a-button>
                    </template>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 空评论状态 -->
          <div v-if="comments.length === 0" class="empty-comments">
            <a-empty description="暂无评论，快来抢沙发吧！" />
          </div>
        </div>
      </div>

      <!-- 底部评论输入框 -->
      <div class="comment-input-bar">
        <a-avatar :size="36">
          <img v-if="userStore.avatar" :src="userStore.avatar" />
          <span v-else>{{ (userStore.nickname || 'U').charAt(0) }}</span>
        </a-avatar>
        <a-textarea
          v-model="commentContent"
          :placeholder="replyPlaceholder"
          :auto-size="{ minRows: 1, maxRows: 4 }"
          allow-clear
        />
        <a-button type="primary" :disabled="!commentContent.trim()" @click="submitComment">
          发送
        </a-button>
      </div>
    </template>

    <!-- 404状态 -->
    <div v-else class="not-found">
      <a-result status="404" title="帖子不存在" subtitle="该帖子可能已被删除或不存在">
        <template #extra>
          <a-button type="primary" @click="router.push('/')">返回首页</a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Heart, Link2, MoreHorizontal, Reply, Pencil, Trash2 } from 'lucide-vue-next'
import { IconLeft, IconMore } from '@arco-design/web-vue/es/icon'
import { Message, Modal } from '@arco-design/web-vue'
import { getPostDetail, getComments, createComment, updateComment, deleteComment } from '@/api/post'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const post = ref(null)
const comments = ref([])
const commentContent = ref('')
const replyTarget = ref(null)
const editingComment = ref(null) // 正在编辑的评论
const editContent = ref('')

const replyPlaceholder = computed(() => {
  if (replyTarget.value) {
    return `回复 @${replyTarget.value.authorName}`
  }
  return '写下你的评论...'
})

// 渲染帖子内容（支持代码块）
const renderedContent = computed(() => {
  if (!post.value?.content) return ''
  return renderContent(post.value.content)
})

// 渲染内容，支持代码块高亮
function renderContent(content) {
  if (!content) return ''

  // 处理代码块 ```language\ncode```
  let result = content.replace(/```(\w*)\n?([\s\S]*?)```/g, (match, lang, code) => {
    const language = lang || 'plaintext'
    return `<pre class="code-block"><code class="language-${language}">${escapeHtml(code.trim())}</code></pre>`
  })

  // 处理行内代码 `code`
  result = result.replace(/`([^`]+)`/g, '<code class="inline-code">$1</code>')

  // 处理换行
  result = result.replace(/\n/g, '<br>')

  return result
}

function escapeHtml(text) {
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 获取帖子详情
async function fetchPost() {
  const postId = route.params.id
  if (!postId) {
    loading.value = false
    return
  }

  try {
    const res = await getPostDetail(postId)
    post.value = {
      ...res.data,
      createdAt: formatTime(res.data.createdAt),
      isOwner: res.data.userId === userStore.userId,
      isLiked: false // TODO: 从API获取
    }

    // 获取评论列表
    await fetchComments(postId)
  } catch (error) {
    console.error('获取帖子详情失败:', error)
    post.value = null
  } finally {
    loading.value = false
  }
}

// 获取评论列表
async function fetchComments(postId) {
  try {
    const res = await getComments(postId)
    comments.value = (res.data || []).map(comment => ({
      ...comment,
      createdAt: comment.createdAtText || formatTime(comment.createdAt),
      replies: (comment.replies || []).map(reply => ({
        ...reply,
        createdAt: reply.createdAtText || formatTime(reply.createdAt),
        replyTo: reply.replyToName
      }))
    }))
  } catch (error) {
    console.error('获取评论列表失败:', error)
    comments.value = []
  }
}

function formatTime(timeStr) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 60000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (minutes < 1440) return `${Math.floor(minutes / 60)}小时前`
  if (minutes < 10080) return `${Math.floor(minutes / 1440)}天前`

  return date.toLocaleDateString()
}

function handleLike() {
  if (!userStore.isLoggedIn) {
    Message.warning('请先登录')
    return
  }
  post.value.isLiked = !post.value.isLiked
  post.value.likeCount += post.value.isLiked ? 1 : -1
}

function handleCommentLike(comment) {
  if (!userStore.isLoggedIn) {
    Message.warning('请先登录')
    return
  }
  comment.isLiked = !comment.isLiked
  comment.likeCount = (comment.likeCount || 0) + (comment.isLiked ? 1 : -1)
}

function replyTo(target, parent = null) {
  replyTarget.value = target
  editingComment.value = null
  editContent.value = ''
}

// 开始编辑评论
function startEditComment(comment) {
  editingComment.value = comment
  editContent.value = comment.content
  replyTarget.value = null
}

// 取消编辑
function cancelEdit() {
  editingComment.value = null
  editContent.value = ''
}

// 保存编辑
async function saveEditComment() {
  if (!editContent.value.trim()) return

  try {
    await updateComment(editingComment.value.id, {
      content: editContent.value.trim()
    })
    Message.success('评论修改成功')
    editingComment.value = null
    editContent.value = ''
    // 刷新评论列表
    await fetchComments(route.params.id)
  } catch (error) {
    console.error('修改评论失败:', error)
    Message.error(error.response?.data?.message || '修改失败，请重试')
  }
}

// 删除评论
function handleDeleteComment(comment) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条评论吗？删除后无法恢复。',
    okText: '删除',
    cancelText: '取消',
    okButtonProps: { status: 'danger' },
    async onOk() {
      try {
        await deleteComment(comment.id)
        Message.success('评论已删除')
        // 刷新评论列表
        await fetchComments(route.params.id)
      } catch (error) {
        console.error('删除评论失败:', error)
        Message.error(error.response?.data?.message || '删除失败，请重试')
      }
    }
  })
}

// 检查是否是自己的评论
function isMyComment(comment) {
  return userStore.isLoggedIn && comment.userId === userStore.userId
}

async function submitComment() {
  if (!userStore.isLoggedIn) {
    Message.warning('请先登录')
    return
  }
  if (!commentContent.value.trim()) return

  const postId = route.params.id
  try {
    await createComment(postId, {
      content: commentContent.value.trim(),
      parentId: replyTarget.value?.id || null
    })
    Message.success('评论发送成功')
    commentContent.value = ''
    replyTarget.value = null
    // 刷新评论列表
    await fetchComments(postId)
  } catch (error) {
    console.error('评论失败:', error)
    Message.error(error.response?.data?.message || '评论失败，请重试')
  }
}

function handleBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(() => {
  fetchPost()
})
</script>

<style scoped>
.post-detail-page {
  max-width: 900px;
  margin: 0 auto;
  padding-bottom: 80px;
}

/* 顶部导航栏 */
.top-nav-bar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #e5e6eb;
  position: sticky;
  top: 0;
  z-index: 100;
}

.back-btn {
  color: #4e5969;
  padding: 4px 8px;
}

.back-btn:hover {
  color: #165dff;
  background: #f2f3f5;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.nav-right {
  width: 40px;
  display: flex;
  justify-content: flex-end;
}

.nav-right :deep(.arco-btn) {
  color: #4e5969;
}

.loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 帖子头部 */
.post-header {
  background: #fff;
  padding: 20px 24px;
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #f2f3f5;
}

.post-title {
  font-size: 20px;
  font-weight: 600;
  color: #1d2129;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tag.category {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.tag-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* 帖子内容区 */
.post-content-wrapper {
  background: #fff;
  border-radius: 0 0 12px 12px;
}

/* 主贴 */
.main-post {
  padding: 20px 24px;
  border-bottom: 1px solid #f2f3f5;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
}

.owner-badge {
  background: #e8f3ff;
  color: #165dff;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
}

.post-time, .comment-time {
  font-size: 12px;
  color: #86909c;
}

/* 帖子正文 */
.post-body {
  font-size: 15px;
  line-height: 1.8;
  color: #1d2129;
  word-break: break-word;
}

.post-body :deep(.code-block) {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  margin: 12px 0;
  overflow-x: auto;
}

.post-body :deep(.code-block code) {
  color: #d4d4d4;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

.post-body :deep(.inline-code) {
  background: #f2f3f5;
  color: #c7254e;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 13px;
}

/* 帖子图片 */
.post-images {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 帖子操作栏 */
.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f2f3f5;
}

.action-left, .action-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 统计栏 */
.post-stats-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 24px;
  background: #fafbfc;
  border-bottom: 1px solid #f2f3f5;
  font-size: 13px;
  color: #86909c;
}

.stat-avatars {
  margin-left: auto;
}

/* 评论区 */
.comments-section {
  padding: 0 24px;
}

.comment-item {
  padding: 20px 0;
  border-bottom: 1px solid #f2f3f5;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-author {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.comment-author .author-info {
  flex: 1;
}

.floor-number {
  font-size: 12px;
  color: #c9cdd4;
}

.comment-body {
  font-size: 14px;
  line-height: 1.7;
  color: #1d2129;
  margin-left: 52px;
}

.comment-body :deep(.code-block) {
  background: #1e1e1e;
  border-radius: 6px;
  padding: 12px;
  margin: 8px 0;
  overflow-x: auto;
}

.comment-body :deep(.code-block code) {
  color: #d4d4d4;
  font-family: 'Fira Code', 'Monaco', 'Consolas', monospace;
  font-size: 12px;
}

.comment-body :deep(.inline-code) {
  background: #f2f3f5;
  color: #c7254e;
  padding: 1px 4px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 12px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 52px;
  margin-top: 8px;
}

/* 子评论 */
.sub-comments {
  margin-left: 52px;
  margin-top: 12px;
  padding: 12px;
  background: #fafbfc;
  border-radius: 8px;
}

.sub-comment-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
}

.sub-comment-item + .sub-comment-item {
  border-top: 1px solid #f2f3f5;
}

.sub-comment-content {
  flex: 1;
  font-size: 13px;
  line-height: 1.6;
}

.sub-comment-content .author-name {
  color: #165dff;
  font-size: 13px;
}

.reply-to {
  color: #86909c;
  margin: 0 4px;
}

.reply-name {
  color: #165dff;
}

.reply-text {
  color: #1d2129;
}

.sub-comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}

/* 编辑评论样式 */
.comment-edit-box {
  margin-left: 52px;
  margin-top: 8px;
}

.comment-edit-box :deep(.arco-textarea-wrapper) {
  margin-bottom: 8px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.sub-comment-edit-box {
  margin-top: 8px;
}

.sub-comment-edit-box :deep(.arco-textarea-wrapper) {
  margin-bottom: 6px;
}

.sub-comment-edit-box .edit-actions {
  gap: 6px;
}

/* 危险操作样式 */
.danger-option {
  color: #f53f3f !important;
}

.danger-option:hover {
  background: #ffece8 !important;
}

.danger-btn {
  color: #f53f3f !important;
}

.danger-btn:hover {
  color: #cb2634 !important;
}

/* 空评论 */
.empty-comments {
  padding: 40px 0;
}

/* 底部评论输入框 */
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 220px;
  right: 0;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 12px 24px;
  background: #fff;
  border-top: 1px solid #e5e6eb;
  z-index: 100;
}

.comment-input-bar :deep(.arco-textarea-wrapper) {
  flex: 1;
}

/* 404 */
.not-found {
  padding: 60px 0;
  background: #fff;
  border-radius: 12px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .post-detail-page {
    padding-bottom: 70px;
  }

  .top-nav-bar {
    padding: 10px 12px;
  }

  .nav-title {
    font-size: 15px;
  }

  .post-header {
    padding: 16px;
    border-radius: 0;
  }

  .post-title {
    font-size: 18px;
  }

  .main-post {
    padding: 16px;
  }

  .post-stats-bar {
    padding: 10px 16px;
    flex-wrap: wrap;
    gap: 12px;
  }

  .comments-section {
    padding: 0 16px;
  }

  .comment-body,
  .comment-actions,
  .sub-comments {
    margin-left: 0;
  }

  .comment-input-bar {
    left: 0;
    padding: 12px 16px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .comment-input-bar {
    left: 200px;
  }
}
</style>
