import { type APIRequestContext, expect } from '@playwright/test'

export const ADMIN_URL = process.env.E2E_ADMIN_URL || 'http://localhost:8081'
export const CLIENT_URL = process.env.E2E_CLIENT_URL || 'http://localhost:8082'

/** 后台登录，返回 JWT。 */
export async function apiLogin(request: APIRequestContext, username: string, password: string): Promise<string> {
  const resp = await request.post(`${ADMIN_URL}/admin/auth/login`, { data: { username, password } })
  const body = await resp.json()
  expect(body.code, JSON.stringify(body)).toBe(0)
  return body.data.token
}

function auth(token: string) {
  return { Authorization: `Bearer ${token}` }
}

export async function apiData(request: APIRequestContext, method: 'get' | 'post', url: string, token: string, data?: any) {
  const resp = await request[method](url, { headers: auth(token), data })
  const body = await resp.json()
  expect(body.code, `${url} -> ${JSON.stringify(body)}`).toBe(0)
  return body.data
}

/** 角色 userId → 账号映射（与种子一致）。 */
const ROLE_BY_APPROVER: Record<string, [string, string]> = {
  '2': ['pm', 'admin123'],
  '3': ['owner', 'admin123'],
}

/**
 * 驱动某业务的多级审批直至全部通过。
 * 反复读取审批记录，对最小层级的待审记录用对应审批人通过。
 */
export async function approveBiz(request: APIRequestContext, adminToken: string, bizType: string, bizId: string) {
  for (let i = 0; i < 20; i++) {
    const records: any[] = await apiData(request, 'get',
      `${ADMIN_URL}/admin/approval/records?bizType=${bizType}&bizId=${bizId}`, adminToken)
    const pending = records.filter((r) => r.result === 'PENDING')
      .sort((a, b) => a.nodeLevel - b.nodeLevel)
    if (pending.length === 0) return
    const rec = pending[0]
    const [u, p] = ROLE_BY_APPROVER[String(rec.approverId)] || ['admin', 'admin123']
    const token = await apiLogin(request, u, p)
    await apiData(request, 'post', `${ADMIN_URL}/admin/approval/${rec.id}`, token, { pass: true, comment: 'e2e' })
  }
  throw new Error('审批未在预期步数内完成')
}
