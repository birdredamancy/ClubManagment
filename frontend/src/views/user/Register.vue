<template>
  <div class="register-page">
    <div class="register-header">
      <a-button
        class="back-btn"
        type="text"
        @click="goBack"
      >
        <ArrowLeft :size="20" />
      </a-button>
      <h1 class="title">创建账号</h1>
      <p class="subtitle">加入校园社团，开启新体验</p>
    </div>

    <div class="register-form">
      <a-form
        :model="form"
        :rules="rules"
        @submit-success="handleSubmit"
        layout="vertical"
      >
        <a-form-item field="username" hide-label>
          <a-input
            v-model="form.username"
            placeholder="用户名（4-20位字母、数字、下划线）"
            size="large"
            allow-clear
          >
            <template #prefix>
              <UserIcon :size="18" color="#86909c" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="nickname" hide-label>
          <a-input
            v-model="form.nickname"
            placeholder="昵称"
            size="large"
            allow-clear
          >
            <template #prefix>
              <Smile :size="18" color="#86909c" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="password" hide-label>
          <a-input-password
            v-model="form.password"
            placeholder="密码（6-20位）"
            size="large"
            allow-clear
          >
            <template #prefix>
              <Lock :size="18" color="#86909c" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item field="confirmPassword" hide-label>
          <a-input-password
            v-model="form.confirmPassword"
            placeholder="确认密码"
            size="large"
            allow-clear
          >
            <template #prefix>
              <Lock :size="18" color="#86909c" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item field="email" hide-label>
          <a-input
            v-model="form.email"
            placeholder="邮箱（选填）"
            size="large"
            allow-clear
          >
            <template #prefix>
              <Mail :size="18" color="#86909c" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item hide-label>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            long
            :loading="loading"
          >
            注册
          </a-button>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        <span class="text">已有账号？</span>
        <a-link @click="goLogin">去登录</a-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import { register } from '@/api/user'
import { ArrowLeft, User as UserIcon, Lock, Mail, Smile } from 'lucide-vue-next'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名' },
    { minLength: 4, maxLength: 20, message: '用户名长度为4-20位' },
    { match: /^[a-zA-Z0-9_]+$/, message: '只允许字母、数字、下划线' }
  ],
  nickname: [
    { required: true, message: '请输入昵称' },
    { maxLength: 20, message: '昵称最长20个字符' }
  ],
  password: [
    { required: true, message: '请输入密码' },
    { minLength: 6, maxLength: 20, message: '密码长度为6-20位' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码' },
    {
      validator: (value, callback) => {
        if (value !== form.password) {
          callback('两次输入的密码不一致')
        } else {
          callback()
        }
      }
    }
  ],
  email: [
    { type: 'email', message: '邮箱格式不正确' }
  ]
}

async function handleSubmit() {
  loading.value = true
  try {
    const res = await register({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
      email: form.email || undefined
    })

    // 注册成功后自动登录
    userStore.login({
      username: form.username,
      password: form.password
    })

    Message.success('注册成功')
    router.replace('/')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function goBack() {
  router.back()
}

function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  max-width: 500px;
  margin: 0 auto;
  padding: 20px 24px 24px;
  background: linear-gradient(180deg, #e8f3ff 0%, #f5f7fa 100%);
}

.register-header {
  position: relative;
  text-align: center;
  margin-bottom: 32px;
  padding-top: 20px;
}

.back-btn {
  position: absolute;
  left: 0;
  top: 16px;
  color: #4e5969;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #86909c;
}

.register-form {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.register-footer {
  text-align: center;
  margin-top: 16px;
}

.text {
  color: #86909c;
  font-size: 14px;
}

:deep(.arco-input-wrapper) {
  border-radius: 8px;
}

:deep(.arco-btn) {
  border-radius: 8px;
  height: 44px;
}
</style>
