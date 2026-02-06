<template>
  <div class="post-detail-page">
    <a-empty description="帖子详情页面开发中" />
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const postId = route.params.id
</script>

<style scoped>
.post-detail-page {
  padding: 16px;
}
</style>
