import axios from 'axios'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 业务状态码判断
    if (res.code !== 200) {
      Message.error(res.message || '请求失败')

      // 401 未登录或 Token 失效
      if (res.code === 401) {
        const userStore = useUserStore()
        userStore.logout()

        // 根据当前路径决定跳转哪个登录页
        const currentPath = router.currentRoute.value.path
        if (currentPath.startsWith('/admin')) {
          router.push({ name: 'AdminLogin' })
        } else {
          router.push({ name: 'Login' })
        }
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res
  },
  (error) => {
    // 网络错误处理
    if (error.response) {
      switch (error.response.status) {
        case 401:
          Message.error('登录已过期，请重新登录')
          const userStore = useUserStore()
          userStore.logout()
          router.push({ name: 'Login' })
          break
        case 403:
          Message.error('没有权限访问')
          break
        case 404:
          Message.error('请求的资源不存在')
          break
        case 500:
          Message.error('服务器错误')
          break
        default:
          Message.error(error.message || '网络错误')
      }
    } else if (error.code === 'ECONNABORTED') {
      Message.error('请求超时')
    } else {
      Message.error('网络连接失败')
    }

    return Promise.reject(error)
  }
)

export default request
