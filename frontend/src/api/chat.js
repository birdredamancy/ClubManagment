import request from './request'

/**
 * 获取用户的频道列表
 */
export function getChannels() {
  return request.get('/chat/channels')
}

/**
 * 获取用户的私信列表
 */
export function getDirectMessages() {
  return request.get('/chat/direct')
}

/**
 * 获取聊天室消息
 * @param {number} roomId - 聊天室ID
 * @param {Object} params - 分页参数
 */
export function getChatMessages(roomId, params = {}) {
  return request.get(`/chat/room/${roomId}/messages`, { params })
}

/**
 * 发送消息
 * @param {number} roomId - 聊天室ID
 * @param {string} content - 消息内容
 */
export function sendMessage(roomId, content) {
  return request.post(`/chat/room/${roomId}/send`, { content })
}
