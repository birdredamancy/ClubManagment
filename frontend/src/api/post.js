import request from './request'

/**
 * 获取帖子列表
 * @param {Object} params - 查询参数
 * @param {string} params.tab - 筛选类型: latest, new, hot, my
 * @param {number} params.clubId - 社团ID
 * @param {number} params.page - 页码
 * @param {number} params.size - 每页数量
 */
export function getPostList(params) {
  return request.get('/post/list', { params })
}

/**
 * 获取帖子详情
 */
export function getPostDetail(postId) {
  return request.get(`/post/${postId}`)
}

/**
 * 创建帖子
 */
export function createPost(data) {
  return request.post('/post', data)
}

/**
 * 更新帖子
 */
export function updatePost(postId, data) {
  return request.put(`/post/${postId}`, data)
}

/**
 * 删除帖子
 */
export function deletePost(postId) {
  return request.delete(`/post/${postId}`)
}

/**
 * 获取帖子评论列表
 */
export function getComments(postId, params = {}) {
  return request.get(`/post/${postId}/comments`, { params })
}

/**
 * 发表评论
 */
export function createComment(postId, data) {
  return request.post(`/post/${postId}/comments`, data)
}

/**
 * 编辑评论
 */
export function updateComment(commentId, data) {
  return request.put(`/post/comments/${commentId}`, data)
}

/**
 * 删除评论
 */
export function deleteComment(commentId) {
  return request.delete(`/post/comments/${commentId}`)
}

/**
 * 点赞帖子
 */
export function likePost(postId) {
  return request.post(`/post/${postId}/like`)
}

/**
 * 取消点赞帖子
 */
export function unlikePost(postId) {
  return request.delete(`/post/${postId}/like`)
}
