import request from './request'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post('/admin/auth/login', data),
}

export const productApi = {
  list: () => request.get('/admin/product'),
  create: (data: any) => request.post('/admin/product', data),
  update: (data: any) => request.put('/admin/product', data),
  remove: (id: number) => request.delete(`/admin/product/${id}`),
}

export const serverApi = {
  list: () => request.get('/admin/server'),
  products: (id: number) => request.get(`/admin/server/${id}/products`),
  create: (data: any) => request.post('/admin/server', data),
  update: (data: any) => request.put('/admin/server', data),
}

export const versionApi = {
  page: (params: any) => request.get('/admin/version', { params }),
  save: (data: any) => request.post('/admin/version', data),
  items: (id: number) => request.get(`/admin/version/${id}/items`),
  preview: (id: number) => request.get(`/admin/version/${id}/preview`),
  submit: (id: number) => request.post(`/admin/version/${id}/submit`),
}

export const customerApi = {
  page: (params: any) => request.get('/admin/customer', { params }),
  create: (data: any) => request.post('/admin/customer', data),
  update: (data: any) => request.put('/admin/customer', data),
  products: (id: string | number) => request.get(`/admin/customer/${id}/products`),
  upsertBinding: (data: any) => request.post('/admin/customer/binding', data),
}

export const audienceApi = {
  preview: (data: any) => request.post('/admin/audience/preview', data),
  save: (data: any) => request.post('/admin/audience', data),
  list: () => request.get('/admin/audience'),
}

export const releaseApi = {
  list: (params?: any) => request.get('/admin/release-plan', { params }),
  create: (data: any) => request.post('/admin/release-plan', data),
  submit: (id: number) => request.post(`/admin/release-plan/${id}/submit`),
  publish: (id: number) => request.post(`/admin/release-plan/${id}/publish`),
  revoke: (id: number) => request.post(`/admin/release-plan/${id}/revoke`),
}

export const approvalApi = {
  todo: () => request.get('/admin/approval/todo'),
  records: (params: any) => request.get('/admin/approval/records', { params }),
  approve: (recordId: number, data: any) => request.post(`/admin/approval/${recordId}`, data),
  flows: () => request.get('/admin/approval/flow'),
  nodes: (flowId: number) => request.get(`/admin/approval/flow/${flowId}/nodes`),
}

export const dashboardApi = {
  overview: (versionId: number) => request.get('/admin/dashboard/overview', { params: { versionId } }),
  funnel: (versionId: number) => request.get('/admin/dashboard/funnel', { params: { versionId } }),
  channel: (versionId: number) => request.get('/admin/dashboard/channel', { params: { versionId } }),
  serverUpgrade: (versionId: number) => request.get('/admin/dashboard/server-upgrade', { params: { versionId } }),
}
