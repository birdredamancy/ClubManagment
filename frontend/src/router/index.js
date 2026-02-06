import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 用户端路由
const userRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue'),
    meta: { requiresAuth: false, title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/user/Home.vue'),
        meta: { title: '话题' }
      },
      {
        path: 'my-posts',
        name: 'MyPosts',
        component: () => import('@/views/user/MyPosts.vue'),
        meta: { title: '我的帖子' }
      },
      {
        path: 'post/:id',
        name: 'PostDetail',
        component: () => import('@/views/user/PostDetail.vue'),
        meta: { title: '帖子详情' }
      },
      {
        path: 'post/create',
        name: 'PostCreate',
        component: () => import('@/views/user/PostCreate.vue'),
        meta: { title: '发布话题' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/Profile.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'user/:userId',
        name: 'UserDetail',
        component: () => import('@/views/user/UserDetail.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'notifications',
        name: 'Notifications',
        component: () => import('@/views/user/Notifications.vue'),
        meta: { title: '我的消息' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/user/Search.vue'),
        meta: { title: '搜索' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: userRoutes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园社团` : '校园社团'

  const userStore = useUserStore()

  // 需要登录
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录访问登录页，重定向到首页
  if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
    next({ name: 'Home' })
    return
  }

  next()
})

export default router
