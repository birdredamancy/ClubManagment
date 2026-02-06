import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserProfile } from '@/api/user'
import { useNotificationStore } from './notification'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const userId = computed(() => userInfo.value?.userId)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username)
  const avatar = computed(() => userInfo.value?.avatar)

  // 登录
  async function login(credentials) {
    const res = await loginApi(credentials)
    token.value = res.data.token
    userInfo.value = res.data

    // 持久化存储
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))

    // 检查是否需要推送今日AI总结
    checkAndPushAiSummary()

    return res
  }

  // 检查并推送AI总结（首次登录时）
  function checkAndPushAiSummary() {
    const notificationStore = useNotificationStore()

    if (notificationStore.shouldShowAiSummary()) {
      // TODO: 后端实现后，这里调用API获取今日AI总结
      // 目前使用模拟数据演示
      notificationStore.addAiSummaryNotification({
        content: '今日社团动态：\n\n1. 篮球社发布了新的训练计划，本周六下午3点在体育馆集合\n\n2. 摄影社正在招募新成员，有兴趣的同学可以关注\n\n3. 读书会本周推荐书目《人类简史》已发布\n\n祝你有愉快的一天！'
      })
      notificationStore.markAiSummaryShown()
    }
  }

  // 登出
  async function logout() {
    try {
      if (token.value) {
        await logoutApi()
      }
    } catch (e) {
      // 忽略登出接口错误
    } finally {
      // 清除本地状态
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return null

    try {
      const res = await getUserProfile()
      userInfo.value = res.data
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      return res.data
    } catch (e) {
      // Token 无效，清除登录状态
      await logout()
      return null
    }
  }

  // 更新用户信息（本地）
  function updateLocalUserInfo(data) {
    userInfo.value = { ...userInfo.value, ...data }
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    userId,
    nickname,
    avatar,
    login,
    logout,
    fetchUserInfo,
    updateLocalUserInfo
  }
})
