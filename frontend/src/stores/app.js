import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 全局加载状态
  const loading = ref(false)

  // 底部导航当前选中
  const activeTab = ref('home')

  // 设置加载状态
  function setLoading(value) {
    loading.value = value
  }

  // 设置当前选中的Tab
  function setActiveTab(tab) {
    activeTab.value = tab
  }

  return {
    loading,
    activeTab,
    setLoading,
    setActiveTab
  }
})
