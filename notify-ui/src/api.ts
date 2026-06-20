import type { NotifierOptions, UpdateData } from './types'

function headers(opts: NotifierOptions): Record<string, string> {
  const h: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Product-Code': opts.productCode,
    'X-Identity': opts.clientId,
  }
  if (opts.token) h['Authorization'] = `Bearer ${opts.token}`
  if (opts.timestamp) h['X-Timestamp'] = opts.timestamp
  if (opts.sign) h['X-Sign'] = opts.sign
  return h
}

async function unwrap(resp: Response) {
  const json = await resp.json()
  if (json && typeof json.code !== 'undefined') {
    if (json.code !== 0) throw new Error(json.message || '请求失败')
    return json.data
  }
  return json
}

export async function fetchUnread(opts: NotifierOptions): Promise<UpdateData[]> {
  const resp = await fetch(`${opts.apiBase}/client/announcements/unread`, { headers: headers(opts) })
  const data = await unwrap(resp)
  return (data || []).map((d: any) => ({
    announcementId: d.announcementId,
    type: d.type,
    title: d.title,
    description: d.detail?.description,
    versionNo: d.detail?.versionNo,
    releaseTime: d.detail?.planReleaseTime,
    items: d.detail?.items || [],
    jumpUrl: d.jumpUrl,
    forceRead: d.forceRead,
  }))
}

export async function reportRead(opts: NotifierOptions, announcementId: number): Promise<void> {
  await fetch(`${opts.apiBase}/client/announcements/${announcementId}/read`, {
    method: 'POST',
    headers: headers(opts),
  })
}

export async function fetchChangelog(opts: NotifierOptions, page = 1, size = 10, category?: string) {
  const q = new URLSearchParams({ current: String(page), size: String(size) })
  if (category) q.set('category', category)
  const resp = await fetch(`${opts.apiBase}/client/changelog?${q.toString()}`, { headers: headers(opts) })
  return unwrap(resp)
}
