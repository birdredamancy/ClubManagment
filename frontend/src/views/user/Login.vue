<template>
  <div class="login-page">
    <div class="login-header">
      <div class="logo">
        <GraduationCap :size="48" color="#165dff" />
      </div>
      <h1 class="title">校园社团</h1>
      <p class="subtitle">连接校园，分享生活</p>
    </div>

    <div class="login-form">
      <a-form
        :model="form"
        :rules="rules"
        @submit-success="handleSubmit"
        layout="vertical"
      >
        <a-form-item field="username" hide-label>
          <a-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            allow-clear
          >
            <template #prefix>
              <UserIcon :size="18" color="#86909c" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item field="password" hide-label>
          <a-input-password
            v-model="form.password"
            placeholder="请输入密码"
            size="large"
            allow-clear
          >
            <template #prefix>
              <Lock :size="18" color="#86909c" />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item hide-label>
          <a-button
            type="primary"
            html-type="submit"
            size="large"
            long
            :loading="loading"
          >
            登录
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <span class="text">还没有账号？</span>
        <a-link @click="goRegister">立即注册</a-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import { GraduationCap, User as UserIcon, Lock } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }]
}

async function handleSubmit() {
  loading.value = true
  try {
    await userStore.login(form)
    Message.success('登录成功')

    // 跳转到之前的页面或首页
    const redirect = route.query.redirect || '/'
    router.replace(redirect)
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}

function goRegister() {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  max-width: 500px;
  margin: 0 auto;
  padding: 60px 24px 24px;
  background: linear-gradient(180deg, #e8f3ff 0%, #f5f7fa 100%);
}

.login-header {
  text-align: center;
  margin-bottom: 48px;
}

.logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 16px;
  background: #fff;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #86909c;
}

.login-form {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.login-footer {
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
