import request from './request'

/**
 * 用户登录
 */
export function login(data) {
  return request.post('/auth/login', data)
}

/**
 * 用户注册
 */
export function register(data) {
  return request.post('/auth/register', data)
}

/**
 * 用户登出
 */
export function logout() {
  return request.post('/user/logout')
}

/**
 * 获取当前用户信息
 */
export function getUserProfile() {
  return request.get('/user/profile')
}

/**
 * 修改用户信息
 */
export function updateUserProfile(data) {
  return request.put('/user/profile', data)
}

/**
 * 获取其他用户公开信息
 */
export function getPublicUserProfile(userId) {
  return request.get(`/user/${userId}`)
}

/**
 * 更新通知设置
 */
export function updateNotificationSettings(data) {
  return request.put('/user/profile', {
    notifyPostReply: data.postReply,
    notifyCommentReply: data.commentReply,
    notifyMention: data.mention,
    notifyClubActivity: data.clubActivity,
    notifySystem: data.system
  })
}

/**
 * 更新邮件设置
 */
export function updateEmailSettings(data) {
  return request.put('/user/profile', {
    emailSystemNotify: data.systemNotify,
    emailActivityRemind: data.activityRemind,
    emailWeeklyDigest: data.weeklyDigest
  })
}

/**
 * 更新隐私设置
 */
export function updatePrivacySettings(data) {
  return request.put('/user/profile', {
    publicProfile: data.publicProfile,
    showOnline: data.showOnline,
    allowMessage: data.allowMessage
  })
}

/**
 * 更新界面设置
 */
export function updateUISettings(data) {
  return request.put('/user/profile', {
    darkMode: data.darkMode,
    compactMode: data.compactMode
  })
}
