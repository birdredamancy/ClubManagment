import request from './request'

/**
 * 获取所有社团列表
 */
export function getClubList() {
  return request.get('/club/list')
}

/**
 * 获取社团详情
 */
export function getClubDetail(clubId) {
  return request.get(`/club/${clubId}`)
}
